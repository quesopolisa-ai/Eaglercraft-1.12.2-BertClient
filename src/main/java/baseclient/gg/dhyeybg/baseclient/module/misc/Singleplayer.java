package baseclient.gg.dhyeybg.baseclient.module.misc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.EaglerZLIB;
import net.lax1dude.eaglercraft.KeyboardConstants;
import net.lax1dude.eaglercraft.internal.FileChooserResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiAutoBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;

public final class Singleplayer {
	public static class AutoBuilder extends Module {
		private static final int MAX_FILE_BYTES = 8 * 1024 * 1024;
		private static final int MAX_NBT_BYTES = 16 * 1024 * 1024;
		private static final int MAX_BLOCKS = 65536;
		private static final int MAX_SCHEMATIC_BLOCKS = 65536;
		private static final double PLACE_REACH_SQUARED = 20.25D;

		private Setting buildDelay;
		private final List<PlannedBlock> blocks = new ArrayList<PlannedBlock>();
		private final Map<String, Integer> materials = new TreeMap<String, Integer>();
		private BlockPos anchor;
		private BlockPos schematicCornerOne;
		private BlockPos schematicCornerTwo;
		private int sizeX;
		private int sizeY;
		private int sizeZ;
		private String blueprintName = "";
		private String status = "Aim at a block and press B to set the build anchor.";
		private boolean building;
		private int nextBlock;
		private int ticksUntilNextBlock;
		private int currentLayer;
		private int pendingSchematicCorner;
		private int originalHotbarSlot = -1;

		public AutoBuilder() {
			super("Auto Builder", "Place an imported structure from your hotbar", Category.MISC);
			setKey(KeyboardConstants.KEY_B);
		}

		public void registerSettings(SettingsManager manager) {
			buildDelay = new Setting("Block Delay", this, 4.0D, 1.0D, 20.0D, true);
			manager.rSetting(buildDelay);
		}

		@Override
		public void onEnable() {
			pendingSchematicCorner = 0;
			if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
					|| !SingleplayerWorld.isModuleAllowed(this)) {
				notifyPlayer("Auto Builder is not allowed in this world.");
				setToggled(false);
				return;
			}

			RayTraceResult hit = mc.objectMouseOver;
			if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || hit.sideHit == null) {
				notifyPlayer("Aim at a block face before opening Auto Builder.");
				setToggled(false);
				return;
			}
			anchor = hit.getBlockPos().offset(hit.sideHit);
			building = false;
			currentLayer = 0;
			status = "Anchor: " + anchor.getX() + ", " + anchor.getY() + ", " + anchor.getZ()
					+ " - import a .nbt or classic .schematic blueprint.";
			mc.displayGuiScreen(new GuiAutoBuilder(this));
		}

		@Override
		public void onDisable() {
			building = false;
			restoreHotbarSlot();
			if (mc != null && mc.currentScreen instanceof GuiAutoBuilder) {
				mc.displayGuiScreen(null);
			}
		}

		@Override
		public void onUpdate() {
			if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
					|| !SingleplayerWorld.isModuleAllowed(this)) {
				if (building) {
					building = false;
					status = "Build stopped: this world is no longer available.";
				}
				restoreHotbarSlot();
				return;
			}

			if (EagRuntime.fileChooserHasResult()) {
				FileChooserResult result = EagRuntime.getFileChooserResult();
				EagRuntime.clearFileChooserResult();
				if (result == null || result.fileData == null) {
					status = "No structure file was selected.";
				} else if (result.fileData.length > MAX_FILE_BYTES) {
					status = "Import failed: schematic file exceeds the 8 MiB limit.";
				} else {
					try {
						NBTTagCompound root = readCompressedNbt(result.fileData);
						if (root.hasKey("Blocks", 7)) {
							loadLegacySchematic(result.fileName, root);
						} else {
							loadStructureNbt(result.fileName, root);
						}
					} catch (IOException | IllegalArgumentException e) {
						status = "Import failed: " + e.getMessage();
					}
				}
			}

			if (!building) {
				return;
			}
			if (ticksUntilNextBlock > 0) {
				--ticksUntilNextBlock;
				return;
			}

			placeNextBlock();
		}

		public void importStructure() {
			if (!isWorldAllowed()) {
				status = "Import is not allowed in this world.";
				return;
			}
			EagRuntime.displayFileChooser(null, "nbt,schematic");
			status = "Choose a vanilla structure .nbt or classic 1.12 .schematic file.";
		}

		public void startBuild() {
			if (!isWorldAllowed()) {
				status = "Building is not allowed in this world.";
				return;
			}
			if (blocks.isEmpty() || anchor == null) {
				status = "Load a non-empty structure and set an anchor first.";
				return;
			}
			if (anchor.getY() < 0 || anchor.getY() + sizeY > 256) {
				status = "Build stopped: the structure would extend outside the world height.";
				return;
			}
			for (PlannedBlock block : blocks) {
				BlockPos target = anchor.add(block.pos);
				if (!mc.world.isBlockLoaded(target) || !mc.world.getWorldBorder().contains(target)) {
					status = "Build stopped: load the destination chunks and keep the structure inside the world border.";
					return;
				}
			}
			nextBlock = 0;
			ticksUntilNextBlock = 0;
			originalHotbarSlot = mc.player.inventory.currentItem;
			building = true;
			status = "Building from hotbar blocks. Keep the next block within reach.";
		}

		public void stopBuild() {
			building = false;
			restoreHotbarSlot();
			status = "Build stopped.";
		}

		public void changeLayer(int amount) {
			if (sizeY > 0) {
				currentLayer = Math.max(0, Math.min(sizeY - 1, currentLayer + amount));
			}
		}

		public void markSchematicCorner(int corner) {
			if (!isWorldAllowed()) {
				status = "Schematic selection is not allowed in this world.";
				return;
			}
			RayTraceResult hit = mc.objectMouseOver;
			if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
				status = "Aim at a block to mark the schematic selection.";
				return;
			}
			if (corner == 1) {
				schematicCornerOne = hit.getBlockPos();
				status = "Schematic corner 1: " + formatPosition(schematicCornerOne);
			} else {
				schematicCornerTwo = hit.getBlockPos();
				status = "Schematic corner 2: " + formatPosition(schematicCornerTwo);
			}
		}

		public void beginMarkSchematicCorner(int corner) {
			if (!isWorldAllowed()) {
				status = "Schematic selection is not allowed in this world.";
				return;
			}
			pendingSchematicCorner = corner;
			status = "Aim at a block and press " + (corner == 1 ? "C" : "V") + " to mark corner " + corner + ".";
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}

		public boolean handleSelectionKey(int key) {
			if (pendingSchematicCorner == 0) {
				return false;
			}
			int corner = pendingSchematicCorner;
			if ((corner == 1 && key != KeyboardConstants.KEY_C)
					|| (corner == 2 && key != KeyboardConstants.KEY_V)) {
				return false;
			}
			pendingSchematicCorner = 0;
			markSchematicCorner(corner);
			if (isWorldAllowed()) {
				mc.displayGuiScreen(new GuiAutoBuilder(this));
			}
			return true;
		}

		public void exportSchematic() {
			if (!isWorldAllowed()) {
				status = "Schematic export is not allowed in this world.";
				return;
			}
			if (schematicCornerOne == null || schematicCornerTwo == null) {
				status = "Mark both schematic corners before exporting.";
				return;
			}

			int minX = Math.min(schematicCornerOne.getX(), schematicCornerTwo.getX());
			int minY = Math.min(schematicCornerOne.getY(), schematicCornerTwo.getY());
			int minZ = Math.min(schematicCornerOne.getZ(), schematicCornerTwo.getZ());
			int width = Math.abs(schematicCornerOne.getX() - schematicCornerTwo.getX()) + 1;
			int height = Math.abs(schematicCornerOne.getY() - schematicCornerTwo.getY()) + 1;
			int length = Math.abs(schematicCornerOne.getZ() - schematicCornerTwo.getZ()) + 1;
			long volume = (long) width * height * length;
			if (width > Short.MAX_VALUE || height > Short.MAX_VALUE || length > Short.MAX_VALUE
					|| volume > MAX_SCHEMATIC_BLOCKS) {
				status = "Export failed: selection exceeds 65,536 blocks or the schematic dimension limit.";
				return;
			}

			byte[] blockIds = new byte[(int) volume];
			byte[] blockData = new byte[(int) volume / 2 + ((int) volume % 2 == 0 ? 0 : 1)];
			byte[] addBlocks = new byte[blockData.length];
			boolean hasExtendedIds = false;
			NBTTagList tileEntities = new NBTTagList();
			int index = 0;

			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					for (int x = 0; x < width; ++x) {
						BlockPos worldPos = new BlockPos(minX + x, minY + y, minZ + z);
						if (!mc.world.isBlockLoaded(worldPos) || !mc.world.getWorldBorder().contains(worldPos)) {
							status = "Export failed: load the entire selected region and keep it inside the world border.";
							return;
						}
						IBlockState state = mc.world.getBlockState(worldPos);
						int blockId = Block.getIdFromBlock(state.getBlock());
						int metadata = state.getBlock().getMetaFromState(state);
						if (blockId < 0 || blockId > 4095 || metadata < 0 || metadata > 15) {
							status = "Export failed: a block has an ID or metadata value unsupported by the schematic format.";
							return;
						}
						blockIds[index] = (byte) (blockId & 255);
						int nibbleIndex = index >> 1;
						if ((index & 1) == 0) {
							blockData[nibbleIndex] = (byte) (metadata & 15);
							addBlocks[nibbleIndex] = (byte) ((blockId >> 8) & 15);
						} else {
							blockData[nibbleIndex] |= (byte) ((metadata & 15) << 4);
							addBlocks[nibbleIndex] |= (byte) (((blockId >> 8) & 15) << 4);
						}
						if (blockId > 255) {
							hasExtendedIds = true;
						}

						TileEntity tileEntity = mc.world.getTileEntity(worldPos);
						if (tileEntity != null) {
							NBTTagCompound tileTag = tileEntity.writeToNBT(new NBTTagCompound());
							tileTag.setInteger("x", x);
							tileTag.setInteger("y", y);
							tileTag.setInteger("z", z);
							tileEntities.appendTag(tileTag);
						}
						++index;
					}
				}
			}

			NBTTagCompound schematic = new NBTTagCompound();
			schematic.setShort("Width", (short) width);
			schematic.setShort("Height", (short) height);
			schematic.setShort("Length", (short) length);
			schematic.setString("Materials", "Alpha");
			schematic.setByteArray("Blocks", blockIds);
			schematic.setByteArray("Data", blockData);
			schematic.setTag("Entities", new NBTTagList());
			if (hasExtendedIds) {
				schematic.setByteArray("AddBlocks", addBlocks);
			}
			schematic.setTag("TileEntities", tileEntities);

			try {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				CompressedStreamTools.writeCompressed(schematic, output);
				String fileName = "eaglercraft-" + width + "x" + height + "x" + length + ".schematic";
				EagRuntime.downloadFileWithName(fileName, output.toByteArray());
				status = "Downloaded " + fileName + " (" + volume + " blocks).";
			} catch (IOException e) {
				status = "Export failed while compressing the schematic: " + e.getMessage();
			}
		}

		private String formatPosition(BlockPos pos) {
			return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
		}

		private boolean isWorldAllowed() {
			return mc != null && mc.player != null && mc.world != null && mc.playerController != null
					&& SingleplayerWorld.isModuleAllowed(this);
		}

		private void placeNextBlock() {
			while (nextBlock < blocks.size()) {
				PlannedBlock planned = blocks.get(nextBlock);
				BlockPos target = anchor.add(planned.pos);
				if (!mc.world.isBlockLoaded(target) || !mc.world.getWorldBorder().contains(target)) {
					stopBuildWithStatus("Build stopped: a target block is no longer loaded or is outside the world border.");
					return;
				}

				IBlockState existing = mc.world.getBlockState(target);
				if (existing.equals(planned.state)) {
					++nextBlock;
					continue;
				}
				if (!existing.getMaterial().isReplaceable()) {
					stopBuildWithStatus("Build stopped: a target position is occupied by a different block.");
					return;
				}

				int slot = findHotbarSlot(planned);
				if (slot < 0) {
					stopBuildWithStatus("Build stopped: the required block and variant are not in your hotbar.");
					return;
				}
				mc.player.inventory.currentItem = slot;
				ItemStack held = mc.player.getHeldItemMainhand();
				boolean attemptedPlacement = false;
				for (EnumFacing direction : EnumFacing.values()) {
					BlockPos support = target.offset(direction.getOpposite());
					if (!mc.world.isBlockLoaded(support)
							|| !mc.world.getBlockState(support).getMaterial().isSolid()
							|| mc.player.getDistanceSqToCenter(support) > PLACE_REACH_SQUARED
							|| !((ItemBlock) held.getItem()).canPlaceBlockOnSide(
									mc.world, support, direction, mc.player, held)) {
						continue;
					}

					Vec3d hitVec = new Vec3d(
							support.getX() + 0.5D + direction.getFrontOffsetX() * 0.5D,
							support.getY() + 0.5D + direction.getFrontOffsetY() * 0.5D,
							support.getZ() + 0.5D + direction.getFrontOffsetZ() * 0.5D);
					boolean wasSneaking = mc.player.isSneaking();
					mc.player.setSneaking(true);
					EnumActionResult result;
					try {
						result = mc.playerController.processRightClickBlock(mc.player, mc.world, support, direction,
								hitVec, EnumHand.MAIN_HAND);
					} finally {
						mc.player.setSneaking(wasSneaking);
					}
					if (result == EnumActionResult.SUCCESS) {
						attemptedPlacement = true;
						break;
					}
				}

				if (!attemptedPlacement) {
					stopBuildWithStatus("Build stopped: no reachable solid support face can place the next block.");
					return;
				}
				ticksUntilNextBlock = buildDelay == null ? 4 : (int) buildDelay.getValDouble();
				status = "Placement " + (nextBlock + 1) + " / " + blocks.size()
						+ " attempted. Keep the next block within reach.";
				return;
			}

			building = false;
			restoreHotbarSlot();
			status = "Finished placing " + blocks.size() + " blueprint blocks.";
		}

		private int findHotbarSlot(PlannedBlock planned) {
			Block block = planned.state.getBlock();
			int metadata = block.getMetaFromState(planned.state);
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = mc.player.inventory.getStackInSlot(slot);
				if (stack == null || stack.func_190926_b() || !(stack.getItem() instanceof ItemBlock)) {
					continue;
				}
				ItemBlock item = (ItemBlock) stack.getItem();
				if (item.getBlock() == block && item.getMetadata(stack.getMetadata()) == metadata) {
					return slot;
				}
			}
			return -1;
		}

		private void stopBuildWithStatus(String message) {
			building = false;
			restoreHotbarSlot();
			status = message;
		}

		private void restoreHotbarSlot() {
			if (originalHotbarSlot >= 0 && mc != null && mc.player != null) {
				mc.player.inventory.currentItem = originalHotbarSlot;
			}
			originalHotbarSlot = -1;
		}

		private NBTTagCompound readCompressedNbt(byte[] data) throws IOException {
			try (DataInputStream input = new DataInputStream(new BufferedInputStream(
					EaglerZLIB.newGZIPInputStream(new ByteArrayInputStream(data))))) {
				try {
					return CompressedStreamTools.read(input, new NBTSizeTracker(MAX_NBT_BYTES));
				} catch (RuntimeException e) {
					throw new IOException("invalid or oversized structure NBT", e);
				}
			}
		}

		private void loadStructureNbt(String fileName, NBTTagCompound root) throws IOException {
			if (!root.hasKey("size", 9) || !root.hasKey("palette", 9) || !root.hasKey("blocks", 9)) {
				throw new IOException("expected a vanilla structure .nbt (size, palette, and blocks tags)");
			}
			if (root.hasKey("entities", 9) && root.getTagList("entities", 10).tagCount() > 0) {
				throw new IOException("structures containing entities are not supported");
			}

			NBTTagList size = root.getTagList("size", 3);
			if (size.tagCount() != 3) {
				throw new IOException("invalid structure dimensions");
			}
			int loadedSizeX = size.getIntAt(0);
			int loadedSizeY = size.getIntAt(1);
			int loadedSizeZ = size.getIntAt(2);
			long volume = (long) loadedSizeX * loadedSizeY * loadedSizeZ;
			if (loadedSizeX < 1 || loadedSizeY < 1 || loadedSizeZ < 1 || loadedSizeX > 128
					|| loadedSizeY > 128 || loadedSizeZ > 128 || volume > 131072L) {
				throw new IOException("structure dimensions are outside the supported limits");
			}

			NBTTagList palette = root.getTagList("palette", 10);
			NBTTagList sourceBlocks = root.getTagList("blocks", 10);
			if (palette.tagCount() < 1 || palette.tagCount() > 4096 || sourceBlocks.tagCount() > MAX_BLOCKS) {
				throw new IOException("palette or block count is outside the supported limits");
			}
			List<IBlockState> states = new ArrayList<IBlockState>(palette.tagCount());
			for (int i = 0; i < palette.tagCount(); ++i) {
				NBTTagCompound stateTag = palette.getCompoundTagAt(i);
				String name = stateTag.getString("Name");
				if (name.isEmpty()) {
					throw new IOException("palette contains a block without a name");
				}
				ResourceLocation blockId = new ResourceLocation(name);
				Block block = Block.REGISTRY.getObject(blockId);
				if (block == null || (block == Blocks.AIR && !"minecraft:air".equals(name))) {
					throw new IOException("unknown block in palette: " + name);
				}
				try {
					states.add(NBTUtil.readBlockState(stateTag));
				} catch (IllegalArgumentException e) {
					throw new IOException("invalid state for " + name, e);
				}
			}

			List<PlannedBlock> loadedBlocks = new ArrayList<PlannedBlock>();
			Set<BlockPos> occupied = new HashSet<BlockPos>();
			Map<String, Integer> loadedMaterials = new TreeMap<String, Integer>();
			for (int i = 0; i < sourceBlocks.tagCount(); ++i) {
				NBTTagCompound entry = sourceBlocks.getCompoundTagAt(i);
				if (entry.hasKey("nbt", 10)) {
					throw new IOException("structures containing block entities are not supported");
				}
				NBTTagList position = entry.getTagList("pos", 3);
				if (position.tagCount() != 3) {
					throw new IOException("block has an invalid position");
				}
				BlockPos pos = new BlockPos(position.getIntAt(0), position.getIntAt(1), position.getIntAt(2));
				if (pos.getX() < 0 || pos.getX() >= loadedSizeX || pos.getY() < 0 || pos.getY() >= loadedSizeY
						|| pos.getZ() < 0 || pos.getZ() >= loadedSizeZ || !occupied.add(pos)) {
					throw new IOException("block position is outside the structure or duplicated");
				}
				int stateIndex = entry.getInteger("state");
				if (stateIndex < 0 || stateIndex >= states.size()) {
					throw new IOException("block references an invalid palette entry");
				}
				IBlockState state = states.get(stateIndex);
				if (state.getBlock() == Blocks.AIR) {
					continue;
				}
				loadedBlocks.add(new PlannedBlock(pos, state));
				String key = getMaterialKey(state);
				Integer count = loadedMaterials.get(key);
				loadedMaterials.put(key, count == null ? 1 : count + 1);
			}
			if (loadedBlocks.isEmpty()) {
				throw new IOException("structure contains no placeable blocks");
			}

			Collections.sort(loadedBlocks, new Comparator<PlannedBlock>() {
				@Override
				public int compare(PlannedBlock first, PlannedBlock second) {
					int yCompare = Integer.compare(first.pos.getY(), second.pos.getY());
					if (yCompare != 0) {
						return yCompare;
					}
					int zCompare = Integer.compare(first.pos.getZ(), second.pos.getZ());
					return zCompare != 0 ? zCompare : Integer.compare(first.pos.getX(), second.pos.getX());
				}
			});
			blocks.clear();
			blocks.addAll(loadedBlocks);
			materials.clear();
			materials.putAll(loadedMaterials);
			sizeX = loadedSizeX;
			sizeY = loadedSizeY;
			sizeZ = loadedSizeZ;
			currentLayer = 0;
			nextBlock = 0;
			building = false;
			blueprintName = fileName;
			status = "Loaded " + fileName + " (" + sizeX + " x " + sizeY + " x " + sizeZ + ").";
		}

		private void loadLegacySchematic(String fileName, NBTTagCompound root) throws IOException {
			if (!root.hasKey("Width", 99) || !root.hasKey("Height", 99) || !root.hasKey("Length", 99)
					|| !root.hasKey("Blocks", 7) || !root.hasKey("Data", 7)) {
				throw new IOException("expected classic MCEdit .schematic tags (Width, Height, Length, Blocks, Data)");
			}

			int loadedSizeX = root.getShort("Width") & 0xFFFF;
			int loadedSizeY = root.getShort("Height") & 0xFFFF;
			int loadedSizeZ = root.getShort("Length") & 0xFFFF;
			long volume = (long) loadedSizeX * loadedSizeY * loadedSizeZ;
			if (loadedSizeX < 1 || loadedSizeY < 1 || loadedSizeZ < 1 || volume > MAX_SCHEMATIC_BLOCKS) {
				throw new IOException("schematic dimensions are outside the supported limits");
			}

			byte[] blockIds = root.getByteArray("Blocks");
			byte[] blockData = root.getByteArray("Data");
			byte[] extendedIds = root.hasKey("AddBlocks", 7) ? root.getByteArray("AddBlocks") : new byte[0];
			int expectedNibbles = (int) ((volume + 1L) / 2L);
			if (blockIds.length != (int) volume || blockData.length < expectedNibbles
					|| (extendedIds.length != 0 && extendedIds.length < expectedNibbles)) {
				throw new IOException("schematic block arrays do not match the declared dimensions");
			}

			List<PlannedBlock> loadedBlocks = new ArrayList<PlannedBlock>();
			Map<String, Integer> loadedMaterials = new TreeMap<String, Integer>();
			for (int y = 0; y < loadedSizeY; ++y) {
				for (int z = 0; z < loadedSizeZ; ++z) {
					for (int x = 0; x < loadedSizeX; ++x) {
						int index = x + z * loadedSizeX + y * loadedSizeX * loadedSizeZ;
						int blockId = blockIds[index] & 255;
						if (extendedIds.length != 0) {
							int packedId = extendedIds[index >> 1] & 255;
							blockId |= ((index & 1) == 0 ? packedId & 15 : packedId >> 4 & 15) << 8;
						}
						int packedData = blockData[index >> 1] & 255;
						int metadata = (index & 1) == 0 ? packedData & 15 : packedData >> 4 & 15;
						Block block = Block.getBlockById(blockId);
						if (block == null) {
							throw new IOException("schematic contains unknown legacy block ID " + blockId);
						}
						if (block == Blocks.AIR) {
							continue;
						}

						IBlockState state = block.getStateFromMeta(metadata);
						BlockPos pos = new BlockPos(x, y, z);
						loadedBlocks.add(new PlannedBlock(pos, state));
						String key = getMaterialKey(state);
						Integer count = loadedMaterials.get(key);
						loadedMaterials.put(key, count == null ? 1 : count + 1);
					}
				}
			}
			if (loadedBlocks.isEmpty()) {
				throw new IOException("schematic contains no placeable blocks");
			}
			if (root.hasKey("TileEntities", 9) && root.getTagList("TileEntities", 10).tagCount() > 0) {
				status = "Imported blocks from " + fileName
						+ "; tile-entity data is not applied by the auto-builder.";
			} else {
				status = "Imported " + fileName + " (" + loadedSizeX + " x " + loadedSizeY + " x " + loadedSizeZ
						+ ").";
			}

			blocks.clear();
			blocks.addAll(loadedBlocks);
			materials.clear();
			materials.putAll(loadedMaterials);
			sizeX = loadedSizeX;
			sizeY = loadedSizeY;
			sizeZ = loadedSizeZ;
			currentLayer = 0;
			nextBlock = 0;
			building = false;
			blueprintName = fileName;
		}

		private String getMaterialKey(IBlockState state) {
			ResourceLocation name = Block.REGISTRY.getNameForObject(state.getBlock());
			return name + ":" + state.getBlock().getMetaFromState(state);
		}

		private void notifyPlayer(String message) {
			if (mc != null && mc.player != null) {
				mc.player.addChatMessage(new TextComponentString(message));
			}
		}

		public List<PlannedBlock> getBlocks() {
			return Collections.unmodifiableList(blocks);
		}

		public Map<String, Integer> getMaterials() {
			return Collections.unmodifiableMap(materials);
		}

		public int getSizeX() {
			return sizeX;
		}

		public int getSizeY() {
			return sizeY;
		}

		public int getSizeZ() {
			return sizeZ;
		}

		public int getCurrentLayer() {
			return currentLayer;
		}

		public int getBlockCount() {
			return blocks.size();
		}

		public String getBlueprintName() {
			return blueprintName;
		}

		public String getStatus() {
			return status;
		}

		public BlockPos getAnchor() {
			return anchor;
		}

		public BlockPos getSchematicCornerOne() {
			return schematicCornerOne;
		}

		public BlockPos getSchematicCornerTwo() {
			return schematicCornerTwo;
		}

		public boolean isBuilding() {
			return building;
		}

		public static final class PlannedBlock {
			public final BlockPos pos;
			public final IBlockState state;

			private PlannedBlock(BlockPos pos, IBlockState state) {
				this.pos = pos;
				this.state = state;
			}
		}
	}

	public static class AutoMine extends Module {
		private static final int DROP_SEARCH_TICKS = 40;
		private static final double MINING_REACH_SQUARED = 12.25D;
		private static final double PICKUP_REACH_SQUARED = 0.81D;

		private Setting searchRange;
		private Setting searchInterval;
		private BlockPos oreTarget;
		private BlockPos minedOrePosition;
		private EntityItem dropTarget;
		private int ticksUntilSearch;
		private int ticksWaitingForDrop;
		private int originalHotbarSlot = -1;
		private boolean miningStarted;
		private boolean steering;
		private boolean previousForward;
		private boolean previousJump;
		private int nextNoToolMessageTick;

		public AutoMine() {
			super("Auto Mine", "Mine nearby ores and walk to their drops", Category.MISC);
		}

		public void registerSettings(SettingsManager manager) {
			searchRange = new Setting("Search Range", this, 10.0D, 2.0D, 16.0D, true);
			searchInterval = new Setting("Search Delay", this, 20.0D, 1.0D, 100.0D, true);
			manager.rSetting(searchRange);
			manager.rSetting(searchInterval);
		}

		private int getSearchRange() {
			return searchRange == null ? 10 : (int) searchRange.getValDouble();
		}

		private int getSearchInterval() {
			return searchInterval == null ? 20 : (int) searchInterval.getValDouble();
		}

		@Override
		public void onEnable() {
			if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
					|| !SingleplayerWorld.isModuleAllowed(this)) {
				if (mc != null && mc.player != null) {
					mc.player.addChatMessage(new TextComponentString("Auto Mine is not allowed in this world."));
				}
				setToggled(false);
				return;
			}
			resetTargets();
		}

		@Override
		public void onDisable() {
			stopMining();
			restoreMovement();
			restoreHotbarSlot();
			resetTargets();
		}

		@Override
		public void onUpdate() {
			if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
					|| !SingleplayerWorld.isModuleAllowed(this)) {
				if (isToggled()) {
					setToggled(false);
				}
				return;
			}
			if (mc.currentScreen != null || !mc.player.isEntityAlive()) {
				stopMining();
				restoreMovement();
				restoreHotbarSlot();
				return;
			}

			if (oreTarget != null) {
				updateOreTarget();
				return;
			}
			if (minedOrePosition != null) {
				updateDropTarget();
				return;
			}
			if (ticksUntilSearch > 0) {
				--ticksUntilSearch;
				return;
			}

			oreTarget = findNearestOre();
			ticksUntilSearch = getSearchInterval();
			if (oreTarget != null) {
				Block block = mc.world.getBlockState(oreTarget).getBlock();
				if (findPickaxe(mc.world.getBlockState(oreTarget)) < 0) {
					notifyMissingPickaxe();
					oreTarget = null;
					return;
				}
				mc.player.addChatMessage(new TextComponentString(
						"Auto Mine: ore found within " + getSearchRange() + " blocks."));
				moveOrMineOre(block);
			} else {
				restoreMovement();
			}
		}

		private void updateOreTarget() {
			Block block = mc.world.getBlockState(oreTarget).getBlock();
			if (!isOre(block)) {
				minedOrePosition = oreTarget;
				oreTarget = null;
				ticksWaitingForDrop = DROP_SEARCH_TICKS;
				stopMining();
				restoreMovement();
				restoreHotbarSlot();
				return;
			}
			moveOrMineOre(block);
		}

		private void moveOrMineOre(Block block) {
			BlockPos target = oreTarget;
			double distanceSq = mc.player.getDistanceSqToCenter(target);
			if (distanceSq > MINING_REACH_SQUARED) {
				stopMining();
				restoreHotbarSlot();
				steerTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
				return;
			}

			restoreMovement();
			int toolSlot = findPickaxe(mc.world.getBlockState(target));
			if (toolSlot < 0) {
				stopMining();
				restoreHotbarSlot();
				notifyMissingPickaxe();
				oreTarget = null;
				ticksUntilSearch = getSearchInterval();
				return;
			}
			if (originalHotbarSlot < 0) {
				originalHotbarSlot = mc.player.inventory.currentItem;
			}
			mc.player.inventory.currentItem = toolSlot;
			mc.playerController.onPlayerDamageBlock(target, EnumFacing.UP);
			miningStarted = true;
		}

		private void updateDropTarget() {
			if (--ticksWaitingForDrop <= 0) {
				dropTarget = null;
				minedOrePosition = null;
				restoreMovement();
				ticksUntilSearch = 0;
				return;
			}
			if (dropTarget == null || dropTarget.isDead || dropTarget.getEntityItem().func_190926_b()) {
				dropTarget = findOreDropNear(minedOrePosition);
			}
			if (dropTarget != null) {
				double distanceSq = mc.player.getDistanceSqToEntity(dropTarget);
				if (distanceSq <= PICKUP_REACH_SQUARED) {
					restoreMovement();
					if (dropTarget.isDead || dropTarget.getEntityItem().func_190926_b()) {
						dropTarget = null;
						minedOrePosition = null;
					}
				} else {
					steerTo(dropTarget.posX, dropTarget.posY, dropTarget.posZ);
				}
				return;
			}

			restoreMovement();
		}

		private BlockPos findNearestOre() {
			BlockPos center = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
			BlockPos nearest = null;
			int search = getSearchRange();
			double nearestDistanceSq = search * search;
			for (int y = Math.max(0, center.getY() - search);
					y <= Math.min(255, center.getY() + search); ++y) {
				for (int z = center.getZ() - search; z <= center.getZ() + search; ++z) {
					for (int x = center.getX() - search; x <= center.getX() + search; ++x) {
						BlockPos pos = new BlockPos(x, y, z);
						if (!mc.world.isBlockLoaded(pos) || !isOre(mc.world.getBlockState(pos).getBlock())) {
							continue;
						}
						double distanceSq = mc.player.getDistanceSqToCenter(pos);
						if (distanceSq <= nearestDistanceSq) {
							nearest = pos;
							nearestDistanceSq = distanceSq;
						}
					}
				}
			}
			return nearest;
		}

		private EntityItem findOreDropNear(BlockPos origin) {
			EntityItem nearest = null;
			double nearestDistanceSq = 16.0D;
			for (Entity entity : mc.world.loadedEntityList) {
				if (!(entity instanceof EntityItem) || entity.isDead) {
					continue;
				}
				EntityItem item = (EntityItem) entity;
				if (!isOreDrop(item.getEntityItem())) {
					continue;
				}
				double dx = item.posX - (origin.getX() + 0.5D);
				double dy = item.posY - (origin.getY() + 0.5D);
				double dz = item.posZ - (origin.getZ() + 0.5D);
				double distanceSq = dx * dx + dy * dy + dz * dz;
				if (distanceSq < nearestDistanceSq) {
					nearest = item;
					nearestDistanceSq = distanceSq;
				}
			}
			return nearest;
		}

		private boolean isOreDrop(ItemStack stack) {
			if (stack == null || stack.func_190926_b()) {
				return false;
			}
			Item item = stack.getItem();
			return item == Items.COAL || item == Items.DIAMOND || item == Items.EMERALD || item == Items.REDSTONE
					|| item == Items.QUARTZ || item == Items.IRON_INGOT || item == Items.GOLD_INGOT
					|| item == Items.DYE && stack.getMetadata() == 4
					|| item == Item.getItemFromBlock(Blocks.IRON_ORE)
					|| item == Item.getItemFromBlock(Blocks.GOLD_ORE);
		}

		private int findPickaxe(net.minecraft.block.state.IBlockState state) {
			int bestSlot = -1;
			float bestSpeed = 0.0F;
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = mc.player.inventory.getStackInSlot(slot);
				if (stack == null || stack.func_190926_b() || !(stack.getItem() instanceof ItemPickaxe)
						|| !stack.canHarvestBlock(state)) {
					continue;
				}
				float speed = stack.getStrVsBlock(state);
				if (speed > bestSpeed) {
					bestSlot = slot;
					bestSpeed = speed;
				}
			}
			return bestSlot;
		}

		private boolean isOre(Block block) {
			return block == Blocks.COAL_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE
					|| block == Blocks.LAPIS_ORE || block == Blocks.DIAMOND_ORE || block == Blocks.EMERALD_ORE
					|| block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE || block == Blocks.QUARTZ_ORE;
		}

		private void steerTo(double x, double y, double z) {
			if (!steering) {
				previousForward = mc.gameSettings.keyBindForward.isKeyDown();
				previousJump = mc.gameSettings.keyBindJump.isKeyDown();
				steering = true;
			}
			double dx = x - mc.player.posX;
			double dz = z - mc.player.posZ;
			double horizontalDistanceSq = dx * dx + dz * dz;
			boolean moveForward = horizontalDistanceSq > 0.36D;
			boolean jump = mc.player.onGround
					&& (mc.player.isCollidedHorizontally || y > mc.player.posY + 1.0D);
			if (moveForward) {
				mc.player.rotationYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
			}
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), moveForward);
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), jump);
		}

		private void stopMining() {
			if (miningStarted && mc != null && mc.playerController != null) {
				mc.playerController.resetBlockRemoving();
			}
			miningStarted = false;
		}

		private void restoreMovement() {
			if (!steering || mc == null || mc.gameSettings == null) {
				return;
			}
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), previousForward);
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), previousJump);
			steering = false;
		}

		private void restoreHotbarSlot() {
			if (originalHotbarSlot >= 0 && mc != null && mc.player != null) {
				mc.player.inventory.currentItem = originalHotbarSlot;
			}
			originalHotbarSlot = -1;
		}

		private void notifyMissingPickaxe() {
			if (mc.player.ticksExisted >= nextNoToolMessageTick) {
				mc.player.addChatMessage(new TextComponentString("Auto Mine: you need a suitable pickaxe in your hotbar."));
				nextNoToolMessageTick = mc.player.ticksExisted + 100;
			}
		}

		private void resetTargets() {
			oreTarget = null;
			minedOrePosition = null;
			dropTarget = null;
			ticksUntilSearch = 0;
			ticksWaitingForDrop = 0;
			nextNoToolMessageTick = 0;
		}
	}
}
