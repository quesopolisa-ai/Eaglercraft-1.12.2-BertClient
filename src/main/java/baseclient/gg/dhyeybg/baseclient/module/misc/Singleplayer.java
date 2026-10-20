package baseclient.gg.dhyeybg.baseclient.module.misc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.lax1dude.eaglercraft.opengl.WorldRenderer;
import net.lax1dude.eaglercraft.internal.FileChooserResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiAutoBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
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
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
		private static final int MAX_PREVIEW_BLOCKS = 8192;
		private static final int MAX_PREVIEW_OUTLINES = 2048;
		private static final double PLACE_REACH_SQUARED = 20.25D;

		private Setting placeSpeed;
		private Setting showPreview;
		private Setting previewRange;
		private Setting buildOnTop;
		private final List<PlannedBlock> blocks = new ArrayList<PlannedBlock>();
		private final Map<Long, Integer> plannedBlockIndex = new HashMap<Long, Integer>();
		private final List<Integer> nearbyBlockIndices = new ArrayList<Integer>();
		private final Map<String, Integer> materials = new TreeMap<String, Integer>();
		private BlockPos anchor;
		private BlockPos schematicCornerOne;
		private BlockPos schematicCornerTwo;
		private int sizeX;
		private int sizeY;
		private int sizeZ;
		private String blueprintName = "";
		private String status = "Set build coordinates, import a blueprint, then preview and build.";
		private boolean building;
		private boolean[] completedBlocks;
		private int[] pendingUntilTick;
		private boolean anchorSettled;
		private int completedBlockCount;
		private double placementBudget;
		private int rotationDegrees;
		private int currentLayer;
		private int pendingSchematicCorner;
		private int originalHotbarSlot = -1;

		public AutoBuilder() {
			super("Auto Builder", "Place an imported structure from your hotbar", Category.MISC);
			setKey(KeyboardConstants.KEY_B);
		}

		public void registerSettings(SettingsManager manager) {
			placeSpeed = new Setting("Place Speed", this, 6.0D, 1.0D, 30.0D, true);
			showPreview = new Setting("World Preview", this, true);
			previewRange = Setting.textBox("Preview Range", this, 64.0D, 8.0D, 128.0D);
			buildOnTop = new Setting("Build On Top", this, true);
			manager.rSetting(placeSpeed);
			manager.rSetting(showPreview);
			manager.rSetting(previewRange);
			manager.rSetting(buildOnTop);
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

			if (anchor == null) {
				anchor = new BlockPos(MathHelper.floor(mc.player.posX), MathHelper.floor(mc.player.posY),
						MathHelper.floor(mc.player.posZ));
			}
			building = false;
			currentLayer = 0;
			status = "Anchor kept at " + formatPosition(anchor) + ". Set coordinates below to move it.";
			mc.displayGuiScreen(new GuiAutoBuilder(this));
		}

		@Override
		public void onDisable() {
			building = false;
			placementBudget = 0.0D;
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
			placeNextBlock();
		}

		@Override
		public void onRender3D(float partialTicks) {
			if (mc == null || mc.world == null) {
				return;
			}
			Entity view = mc.getRenderViewEntity();
			if (view == null) {
				return;
			}
			double viewerX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
			double viewerY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
			double viewerZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;
			boolean drawPreview = (showPreview == null || showPreview.getValBoolean())
					&& anchor != null && !blocks.isEmpty();
			boolean drawMarkers = anchor != null || schematicCornerOne != null || schematicCornerTwo != null;
			if (!drawPreview && !drawMarkers) {
				return;
			}

			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer buffer = tessellator.getBuffer();
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.disableCull();
			buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

			if (drawPreview) {
				double range = previewRange == null ? 64.0D : previewRange.getValDouble();
				double rangeSq = range * range;
				int rendered = 0;
				for (PlannedBlock planned : blocks) {
					if (rendered >= MAX_PREVIEW_BLOCKS) {
						break;
					}
					int worldX = anchor.getX() + planned.pos.getX();
					int worldY = anchor.getY() + planned.pos.getY();
					int worldZ = anchor.getZ() + planned.pos.getZ();
					double dx = worldX + 0.5D - viewerX;
					double dy = worldY + 0.5D - viewerY;
					double dz = worldZ + 0.5D - viewerZ;
					if (dx * dx + dy * dy + dz * dz > rangeSq) {
						continue;
					}
					BlockPos pos = new BlockPos(worldX, worldY, worldZ);
					int color = getPreviewColor(planned.state, pos);
					float alpha = 0.19F;
					if (mc.world.isBlockLoaded(pos)) {
						IBlockState existing = mc.world.getBlockState(pos);
						if (matchesPlannedState(existing, planned.state)) {
							color = 0xFF56D6A5;
							alpha = 0.12F;
						} else if (!existing.getMaterial().isReplaceable()) {
							color = 0xFFFF626E;
							alpha = 0.28F;
						}
					}
					addGhostCube(buffer, pos.getX() - viewerX, pos.getY() - viewerY, pos.getZ() - viewerZ,
							color, alpha);
					rendered++;
				}
			}

			if (anchor != null) {
				addGhostCube(buffer, anchor.getX() - viewerX, anchor.getY() - viewerY, anchor.getZ() - viewerZ,
						0xFF5DC8E8, 0.40F);
			}
			if (schematicCornerOne != null) {
				addGhostCube(buffer, schematicCornerOne.getX() - viewerX, schematicCornerOne.getY() - viewerY,
						schematicCornerOne.getZ() - viewerZ, 0xFF64D6A6, 0.44F);
			}
			if (schematicCornerTwo != null) {
				addGhostCube(buffer, schematicCornerTwo.getX() - viewerX, schematicCornerTwo.getY() - viewerY,
						schematicCornerTwo.getZ() - viewerZ, 0xFFFF9B62, 0.44F);
			}

			tessellator.draw();
			buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
			if (drawPreview) {
				double range = previewRange == null ? 64.0D : previewRange.getValDouble();
				double rangeSq = range * range;
				int outlined = 0;
				for (PlannedBlock planned : blocks) {
					if (outlined >= MAX_PREVIEW_OUTLINES) {
						break;
					}
					int worldX = anchor.getX() + planned.pos.getX();
					int worldY = anchor.getY() + planned.pos.getY();
					int worldZ = anchor.getZ() + planned.pos.getZ();
					double dx = worldX + 0.5D - viewerX;
					double dy = worldY + 0.5D - viewerY;
					double dz = worldZ + 0.5D - viewerZ;
					if (dx * dx + dy * dy + dz * dz > rangeSq) {
						continue;
					}
					BlockPos pos = new BlockPos(worldX, worldY, worldZ);
					int edgeColor = 0xFFBCE8F5;
					float edgeAlpha = 0.72F;
					if (mc.world.isBlockLoaded(pos)) {
						IBlockState existing = mc.world.getBlockState(pos);
						if (matchesPlannedState(existing, planned.state)) {
							edgeColor = 0xFF56D6A5;
							edgeAlpha = 0.78F;
						} else if (!existing.getMaterial().isReplaceable()) {
							edgeColor = 0xFFFF626E;
							edgeAlpha = 0.84F;
						}
					}
					addCubeOutline(buffer, pos.getX() - viewerX, pos.getY() - viewerY, pos.getZ() - viewerZ,
							edgeColor, edgeAlpha);
					outlined++;
				}
			}
			if (anchor != null) {
				addCubeOutline(buffer, anchor.getX() - viewerX, anchor.getY() - viewerY, anchor.getZ() - viewerZ,
						0xFF5DC8E8, 0.95F);
			}
			if (schematicCornerOne != null) {
				addCubeOutline(buffer, schematicCornerOne.getX() - viewerX, schematicCornerOne.getY() - viewerY,
						schematicCornerOne.getZ() - viewerZ, 0xFF64D6A6, 0.95F);
			}
			if (schematicCornerTwo != null) {
				addCubeOutline(buffer, schematicCornerTwo.getX() - viewerX, schematicCornerTwo.getY() - viewerY,
						schematicCornerTwo.getZ() - viewerZ, 0xFFFF9B62, 0.95F);
			}
			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.enableTexture2D();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
		}

		private int getPreviewColor(IBlockState state, BlockPos pos) {
			MapColor mapColor = mc.world.isBlockLoaded(pos) ? state.getMapColor(mc.world, pos) : null;
			if (mapColor != null && mapColor.colorValue != 0) {
				return mapColor.colorValue;
			}
			ResourceLocation name = Block.REGISTRY.getNameForObject(state.getBlock());
			int hash = name == null ? state.getBlock().hashCode() : name.toString().hashCode();
			int red = 80 + (hash & 0x7F);
			int green = 80 + ((hash >>> 8) & 0x7F);
			int blue = 80 + ((hash >>> 16) & 0x7F);
			return red << 16 | green << 8 | blue;
		}

		private boolean matchesPlannedState(IBlockState existing, IBlockState planned) {
			if (existing.getBlock() != planned.getBlock()) {
				return false;
			}
			for (Map.Entry<IProperty<?>, Comparable<?>> property : planned.getProperties().entrySet()) {
				if (isDynamicPreviewProperty(property.getKey().getName())) {
					continue;
				}
				Comparable<?> existingValue = existing.getProperties().get(property.getKey());
				if (!property.getValue().equals(existingValue)) {
					return false;
				}
			}
			return true;
		}

		private boolean isDynamicPreviewProperty(String name) {
			return "age".equals(name) || "shape".equals(name) || "power".equals(name)
					|| "powered".equals(name) || "attached".equals(name) || "disarmed".equals(name)
					|| "north".equals(name) || "east".equals(name) || "south".equals(name)
					|| "west".equals(name) || "up".equals(name) || "down".equals(name)
					|| "decayable".equals(name) || "check_decay".equals(name) || "open".equals(name)
					|| "lit".equals(name) || "triggered".equals(name) || "extended".equals(name);
		}

		private static void addGhostCube(WorldRenderer buffer, double x, double y, double z, int color, float alpha) {
			float red = (color >> 16 & 255) / 255.0F;
			float green = (color >> 8 & 255) / 255.0F;
			float blue = (color & 255) / 255.0F;
			double x2 = x + 1.0D;
			double y2 = y + 1.0D;
			double z2 = z + 1.0D;
			ghostQuad(buffer, x, y2, z, x2, y2, z, x2, y2, z2, x, y2, z2, red, green, blue, alpha);
			ghostQuad(buffer, x, y, z2, x2, y, z2, x2, y, z, x, y, z, red, green, blue, alpha);
			ghostQuad(buffer, x, y, z, x, y2, z, x2, y2, z, x2, y, z, red, green, blue, alpha);
			ghostQuad(buffer, x2, y, z2, x2, y2, z2, x, y2, z2, x, y, z2, red, green, blue, alpha);
			ghostQuad(buffer, x, y, z2, x, y2, z2, x, y2, z, x, y, z, red, green, blue, alpha);
			ghostQuad(buffer, x2, y, z, x2, y2, z, x2, y2, z2, x2, y, z2, red, green, blue, alpha);
		}

		private static void ghostQuad(WorldRenderer buffer, double x1, double y1, double z1,
				double x2, double y2, double z2, double x3, double y3, double z3,
				double x4, double y4, double z4, float red, float green, float blue, float alpha) {
			buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
			buffer.pos(x3, y3, z3).color(red, green, blue, alpha).endVertex();
			buffer.pos(x4, y4, z4).color(red, green, blue, alpha).endVertex();
		}

		private static void addCubeOutline(WorldRenderer buffer, double x, double y, double z, int color,
				float alpha) {
			double x2 = x + 1.0D;
			double y2 = y + 1.0D;
			double z2 = z + 1.0D;
			float red = (color >> 16 & 255) / 255.0F;
			float green = (color >> 8 & 255) / 255.0F;
			float blue = (color & 255) / 255.0F;
			outlineEdge(buffer, x, y, z, x2, y, z, red, green, blue, alpha);
			outlineEdge(buffer, x2, y, z, x2, y, z2, red, green, blue, alpha);
			outlineEdge(buffer, x2, y, z2, x, y, z2, red, green, blue, alpha);
			outlineEdge(buffer, x, y, z2, x, y, z, red, green, blue, alpha);
			outlineEdge(buffer, x, y2, z, x2, y2, z, red, green, blue, alpha);
			outlineEdge(buffer, x2, y2, z, x2, y2, z2, red, green, blue, alpha);
			outlineEdge(buffer, x2, y2, z2, x, y2, z2, red, green, blue, alpha);
			outlineEdge(buffer, x, y2, z2, x, y2, z, red, green, blue, alpha);
			outlineEdge(buffer, x, y, z, x, y2, z, red, green, blue, alpha);
			outlineEdge(buffer, x2, y, z, x2, y2, z, red, green, blue, alpha);
			outlineEdge(buffer, x2, y, z2, x2, y2, z2, red, green, blue, alpha);
			outlineEdge(buffer, x, y, z2, x, y2, z2, red, green, blue, alpha);
		}

		private static void outlineEdge(WorldRenderer buffer, double x1, double y1, double z1, double x2, double y2,
				double z2, float red, float green, float blue, float alpha) {
			buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
			buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
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
			if (buildOnTop != null && buildOnTop.getValBoolean() && !anchorSettled) {
				settleAnchorOnTop();
			}
			anchorSettled = true;
			if (anchor.getY() < 0 || anchor.getY() + sizeY > 256) {
				status = "Build stopped: the structure would extend outside the world height.";
				return;
			}
			for (PlannedBlock block : blocks) {
				BlockPos target = anchor.add(block.pos);
				if (!mc.world.getWorldBorder().contains(target)) {
					status = "Build stopped: the structure extends outside the world border.";
					return;
				}
			}
			completedBlocks = new boolean[blocks.size()];
			pendingUntilTick = new int[blocks.size()];
			completedBlockCount = 0;
			plannedBlockIndex.clear();
			for (int i = 0; i < blocks.size(); i++) {
				BlockPos local = blocks.get(i).pos;
				plannedBlockIndex.put(packPosition(local.getX(), local.getY(), local.getZ()), i);
			}
			originalHotbarSlot = mc.player.inventory.currentItem;
			placementBudget = 0.0D;
			building = true;
			status = "Building at " + getPlaceSpeed() + " blocks/sec; move near the preview to continue.";
		}

		/**
		 * Raises the anchor so the blueprint's base layer sits on top of whatever solid
		 * blocks already exist under its footprint, instead of being buried in terrain
		 * (buried positions are "blocked" and the build never finishes).
		 */
		private void settleAnchorOnTop() {
			int highestSolid = -1;
			int scanTop = Math.min(255, anchor.getY() + sizeY + 8);
			int scanBottom = Math.max(0, anchor.getY() - 24);
			for (PlannedBlock block : blocks) {
				if (block.pos.getY() != 0) {
					continue;
				}
				int worldX = anchor.getX() + block.pos.getX();
				int worldZ = anchor.getZ() + block.pos.getZ();
				if (!mc.world.isBlockLoaded(new BlockPos(worldX, anchor.getY(), worldZ))) {
					continue;
				}
				for (int y = scanTop; y >= scanBottom; y--) {
					if (y <= highestSolid) {
						break;
					}
					if (!mc.world.getBlockState(new BlockPos(worldX, y, worldZ)).getMaterial().isReplaceable()) {
						highestSolid = y;
						break;
					}
				}
			}
			int settledY = highestSolid + 1;
			if (highestSolid >= 0 && settledY != anchor.getY() && settledY + sizeY <= 256) {
				anchor = new BlockPos(anchor.getX(), settledY, anchor.getZ());
			}
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

		public void setAnchorFromText(String xText, String yText, String zText) {
			try {
				setAnchor(Integer.parseInt(xText.trim()), Integer.parseInt(yText.trim()),
						Integer.parseInt(zText.trim()));
			} catch (NumberFormatException e) {
				status = "Anchor coordinates must be whole numbers.";
			}
		}

		public void usePlayerPositionAsAnchor() {
			if (!isWorldAllowed()) {
				status = "Cannot set the anchor without an active world.";
				return;
			}
			setAnchor(MathHelper.floor(mc.player.posX), MathHelper.floor(mc.player.posY),
					MathHelper.floor(mc.player.posZ));
		}

		private void setAnchor(int x, int y, int z) {
			if (building) {
				status = "Stop the current build before moving its anchor.";
				return;
			}
			if (y < 0 || y >= 256 || (sizeY > 0 && y + sizeY > 256)) {
				status = "Anchor is outside the valid build height.";
				return;
			}
			BlockPos candidate = new BlockPos(x, y, z);
			if (!mc.world.getWorldBorder().contains(candidate)) {
				status = "Anchor is outside the world border.";
				return;
			}
			if (sizeX > 0 && sizeY > 0 && sizeZ > 0) {
				for (PlannedBlock block : blocks) {
					if (!mc.world.getWorldBorder().contains(candidate.add(block.pos))) {
						status = "The selected anchor would place part of the blueprint outside the world border.";
						return;
					}
				}
			}
			anchor = candidate;
			anchorSettled = false;
			status = "Build anchor set to " + formatPosition(anchor) + ".";
		}

		public void rotateClockwise() {
			if (building) {
				status = "Stop the current build before rotating the blueprint.";
				return;
			}
			if (blocks.isEmpty()) {
				status = "Import a blueprint before rotating it.";
				return;
			}
			List<PlannedBlock> rotated = new ArrayList<PlannedBlock>(blocks.size());
			for (PlannedBlock block : blocks) {
				BlockPos pos = new BlockPos(sizeZ - 1 - block.pos.getZ(), block.pos.getY(), block.pos.getX());
				rotated.add(new PlannedBlock(pos, block.state.withRotation(Rotation.CLOCKWISE_90)));
			}
			blocks.clear();
			blocks.addAll(rotated);
			int previousSizeX = sizeX;
			sizeX = sizeZ;
			sizeZ = previousSizeX;
			rotationDegrees = (rotationDegrees + 90) % 360;
			materials.clear();
			for (PlannedBlock block : blocks) {
				String key = getMaterialKey(block.state);
				Integer count = materials.get(key);
				materials.put(key, count == null ? 1 : count + 1);
			}
			completedBlocks = null;
			pendingUntilTick = null;
			completedBlockCount = 0;
			plannedBlockIndex.clear();
			status = "Blueprint rotated to " + rotationDegrees + " degrees clockwise.";
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
			if (completedBlocks == null || completedBlocks.length != blocks.size()) {
				completedBlocks = new boolean[blocks.size()];
				pendingUntilTick = new int[blocks.size()];
				completedBlockCount = 0;
			}
			if (plannedBlockIndex.size() != blocks.size()) {
				plannedBlockIndex.clear();
				for (int i = 0; i < blocks.size(); i++) {
					BlockPos local = blocks.get(i).pos;
					plannedBlockIndex.put(packPosition(local.getX(), local.getY(), local.getZ()), i);
				}
			}
			int placedThisTick = 0;
			placementBudget = Math.min(2.0D, placementBudget + getPlaceSpeed() / 20.0D);
			String waitingStatus = null;
			nearbyBlockIndices.clear();
			int localCenterX = MathHelper.floor(mc.player.posX) - anchor.getX();
			int localCenterY = MathHelper.floor(mc.player.posY) - anchor.getY();
			int localCenterZ = MathHelper.floor(mc.player.posZ) - anchor.getZ();
			for (int y = localCenterY - 6; y <= localCenterY + 8; y++) {
				for (int z = localCenterZ - 6; z <= localCenterZ + 6; z++) {
					for (int x = localCenterX - 6; x <= localCenterX + 6; x++) {
						Integer index = plannedBlockIndex.get(packPosition(x, y, z));
						if (index != null && !completedBlocks[index]) {
							nearbyBlockIndices.add(index);
						}
					}
				}
			}
			Collections.sort(nearbyBlockIndices, new Comparator<Integer>() {
				@Override
				public int compare(Integer first, Integer second) {
					BlockPos firstPos = anchor.add(blocks.get(first).pos);
					BlockPos secondPos = anchor.add(blocks.get(second).pos);
					int byLayer = Integer.compare(firstPos.getY(), secondPos.getY());
					if (byLayer != 0) {
						return byLayer;
					}
					return Double.compare(mc.player.getDistanceSqToCenter(firstPos),
							mc.player.getDistanceSqToCenter(secondPos));
				}
			});
			for (Integer candidate : nearbyBlockIndices) {
				if (placedThisTick >= 2) break;
				int index = candidate.intValue();
				if (completedBlocks[index]) {
					continue;
				}
				PlannedBlock planned = blocks.get(index);
				int targetX = anchor.getX() + planned.pos.getX();
				int targetY = anchor.getY() + planned.pos.getY();
				int targetZ = anchor.getZ() + planned.pos.getZ();
				double dx = targetX + 0.5D - mc.player.posX;
				double dy = targetY + 0.5D - (mc.player.posY + mc.player.getEyeHeight());
				double dz = targetZ + 0.5D - mc.player.posZ;
				if (dx * dx + dy * dy + dz * dz > 36.0D) {
					continue;
				}
				BlockPos target = new BlockPos(targetX, targetY, targetZ);
				if (!mc.world.getWorldBorder().contains(target)) {
					stopBuildWithStatus("Build stopped: a target block is outside the world border.");
					return;
				}
				if (!mc.world.isBlockLoaded(target)) {
					waitingStatus = "Waiting for destination chunks to load.";
					continue;
				}

				IBlockState existing = mc.world.getBlockState(target);
				int now = mc.player.ticksExisted;
				if (matchesPlannedState(existing, planned.state)
						|| (pendingUntilTick[index] != 0 && existing.getBlock() == planned.state.getBlock())) {
					completedBlocks[index] = true;
					completedBlockCount++;
					continue;
				}
				if (pendingUntilTick[index] > now) {
					waitingStatus = "Waiting for the world to confirm a placed block.";
					continue;
				}
				if (!existing.getMaterial().isReplaceable()) {
					waitingStatus = "A nearby blueprint position is blocked by another block.";
					continue;
				}

				int slot = findHotbarSlot(planned);
				if (slot < 0) {
					waitingStatus = "Missing a required block or variant in your hotbar.";
					continue;
				}
				if (placementBudget < 1.0D) {
					continue;
				}
				EnumHand hand = EnumHand.MAIN_HAND;
				ItemStack held;
				if (slot == OFFHAND_SLOT) {
					hand = EnumHand.OFF_HAND;
					held = mc.player.getHeldItemOffhand();
				} else {
					mc.player.inventory.currentItem = slot;
					held = mc.player.getHeldItemMainhand();
				}
				boolean attemptedPlacement = false;
				placeFailReason = "";
				EnumFacing preferredFace = getPreferredPlacementFace(planned.state);
				EnumFacing plannedFacing = getStateFacing(planned.state);
				float originalYaw = mc.player.rotationYaw;
				if (planned.state.getBlock() instanceof BlockStairs && plannedFacing != null) {
					mc.player.rotationYaw = plannedFacing.getHorizontalAngle();
				}
				try {
					if (preferredFace != null) {
						attemptedPlacement = tryPlaceOnFace(target, preferredFace, held, hand);
					}
					if (!attemptedPlacement) {
						for (EnumFacing direction : EnumFacing.values()) {
							if (direction != preferredFace && tryPlaceOnFace(target, direction, held, hand)) {
								attemptedPlacement = true;
								break;
							}
						}
					}
				} finally {
					mc.player.rotationYaw = originalYaw;
				}

				if (!attemptedPlacement) {
					String what = Block.REGISTRY.getNameForObject(planned.state.getBlock()).getResourcePath();
					waitingStatus = "Can't place " + what + " @" + targetX + "," + targetY + "," + targetZ + ": "
							+ (placeFailReason.isEmpty() ? "no solid block next to it to place against"
									: placeFailReason);
					continue;
				}
				// not done until the world actually shows the block; a rejected click is retried
				pendingUntilTick[index] = Math.max(1, now + 10);
				placedThisTick++;
				placementBudget -= 1.0D;
			}

			if (completedBlockCount >= blocks.size()) {
				building = false;
				restoreHotbarSlot();
				status = "Finished placing " + blocks.size() + " blueprint blocks.";
			} else if (placedThisTick > 0) {
				status = "Placed " + completedBlockCount + " / " + blocks.size()
						+ " at " + getPlaceSpeed() + " blocks/sec.";
			} else {
				status = waitingStatus == null
						? "Move near unbuilt preview blocks to continue automatically."
						: waitingStatus;
			}
		}

		private static long packPosition(int x, int y, int z) {
			return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
		}

		private String placeFailReason = "";

		private void noteFail(String reason) {
			if (placeFailReason.isEmpty()) {
				placeFailReason = reason;
			}
		}

		private String blockLabel(BlockPos pos) {
			ResourceLocation name = Block.REGISTRY.getNameForObject(mc.world.getBlockState(pos).getBlock());
			return (name == null ? "?" : name.getResourcePath()) + " @" + pos.getX() + "," + pos.getY() + ","
					+ pos.getZ();
		}

		private boolean tryPlaceOnFace(BlockPos target, EnumFacing direction, ItemStack held, EnumHand hand) {
			BlockPos support = target.offset(direction.getOpposite());
			if (!mc.world.isBlockLoaded(support) || mc.world.getBlockState(support).getMaterial().isReplaceable()) {
				return false;
			}
			// from here on the support block is a real block, so a failure is worth reporting
			if (mc.player.getDistanceSqToCenter(support) > PLACE_REACH_SQUARED) {
				noteFail("support " + blockLabel(support) + " is out of reach, walk closer");
				return false;
			}
			if (held.getItem() instanceof ItemBlock && !((ItemBlock) held.getItem()).canPlaceBlockOnSide(mc.world,
					support, direction, mc.player, held)) {
				noteFail("game refuses the block on " + blockLabel(support)
						+ " (an entity or you are standing in the spot?)");
				return false;
			}

			Vec3d hitVec = new Vec3d(
					support.getX() + 0.5D + direction.getFrontOffsetX() * 0.5D,
					support.getY() + 0.5D + direction.getFrontOffsetY() * 0.5D,
					support.getZ() + 0.5D + direction.getFrontOffsetZ() * 0.5D);
			boolean wasSneaking = mc.player.isSneaking();
			mc.player.setSneaking(true);
			try {
				EnumActionResult result = mc.playerController.processRightClickBlock(mc.player, mc.world, support,
						direction, hitVec, hand);
				if (result != EnumActionResult.SUCCESS) {
					noteFail("right-click on " + blockLabel(support) + " returned " + result);
				}
				return result == EnumActionResult.SUCCESS;
			} finally {
				mc.player.setSneaking(wasSneaking);
			}
		}

		private EnumFacing getPreferredPlacementFace(IBlockState state) {
			if (state.getBlock() instanceof BlockStairs) {
				return state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP
						? EnumFacing.DOWN : EnumFacing.UP;
			}
			for (Map.Entry<IProperty<?>, Comparable<?>> property : state.getProperties().entrySet()) {
				Object value = property.getValue();
				if ("facing".equals(property.getKey().getName()) && value instanceof EnumFacing) {
					return (EnumFacing) value;
				}
				if ("axis".equals(property.getKey().getName()) && value instanceof EnumFacing.Axis) {
					EnumFacing.Axis axis = (EnumFacing.Axis) value;
					if (axis == EnumFacing.Axis.X) return EnumFacing.EAST;
					if (axis == EnumFacing.Axis.Y) return EnumFacing.UP;
					if (axis == EnumFacing.Axis.Z) return EnumFacing.SOUTH;
				}
			}
			return null;
		}

		private EnumFacing getStateFacing(IBlockState state) {
			for (Map.Entry<IProperty<?>, Comparable<?>> property : state.getProperties().entrySet()) {
				if ("facing".equals(property.getKey().getName()) && property.getValue() instanceof EnumFacing) {
					return (EnumFacing) property.getValue();
				}
			}
			return null;
		}

		private static final int OFFHAND_SLOT = 9;

		/** Blocks whose placing item is not the plain ItemBlock (string -> tripwire, etc). */
		public static Item getPlacementItem(Block block) {
			if (block == Blocks.TRIPWIRE) {
				return Items.STRING;
			}
			if (block == Blocks.REDSTONE_WIRE) {
				return Items.REDSTONE;
			}
			if (block == Blocks.REEDS) {
				return Items.REEDS;
			}
			if (block == Blocks.CAULDRON) {
				return Items.CAULDRON;
			}
			return Item.getItemFromBlock(block);
		}

		private boolean stackMatches(ItemStack stack, Item requiredItem, PlannedBlock planned) {
			if (stack == null || stack.func_190926_b() || stack.getItem() != requiredItem) {
				return false;
			}
			return !(requiredItem instanceof ItemBlock && requiredItem.getHasSubtypes()
					&& !matchesItemVariant((ItemBlock) requiredItem, stack, planned.state));
		}

		/** Returns hotbar slot 0-8, OFFHAND_SLOT for the off hand, or -1 if the item is missing. */
		private int findHotbarSlot(PlannedBlock planned) {
			Item requiredItem = getPlacementItem(planned.state.getBlock());
			if (requiredItem == null || requiredItem == Items.AIR) {
				return -1;
			}
			for (int slot = 0; slot < 9; ++slot) {
				if (stackMatches(mc.player.inventory.getStackInSlot(slot), requiredItem, planned)) {
					return slot;
				}
			}
			if (stackMatches(mc.player.getHeldItemOffhand(), requiredItem, planned)) {
				return OFFHAND_SLOT;
			}
			return -1;
		}

		private boolean matchesItemVariant(ItemBlock item, ItemStack stack, IBlockState planned) {
			Block block = planned.getBlock();
			IBlockState itemState = block.getStateFromMeta(item.getMetadata(stack.getMetadata()));
			for (Map.Entry<IProperty<?>, Comparable<?>> property : planned.getProperties().entrySet()) {
				String name = property.getKey().getName();
				if ("facing".equals(name) || "half".equals(name) || "axis".equals(name)
						|| "rotation".equals(name) || isDynamicPreviewProperty(name)) {
					continue;
				}
				Comparable<?> itemValue = itemState.getProperties().get(property.getKey());
				if (!property.getValue().equals(itemValue)) {
					return false;
				}
			}
			return true;
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
			anchorSettled = false;
			blocks.addAll(loadedBlocks);
			materials.clear();
			materials.putAll(loadedMaterials);
			sizeX = loadedSizeX;
			sizeY = loadedSizeY;
			sizeZ = loadedSizeZ;
			currentLayer = 0;
			building = false;
			rotationDegrees = 0;
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
			anchorSettled = false;
			blocks.addAll(loadedBlocks);
			materials.clear();
			materials.putAll(loadedMaterials);
			sizeX = loadedSizeX;
			sizeY = loadedSizeY;
			sizeZ = loadedSizeZ;
			currentLayer = 0;
			building = false;
			rotationDegrees = 0;
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

		public int getPlacedBlockCount() {
			return completedBlockCount;
		}

		public int getPlaceSpeed() {
			return placeSpeed == null ? 6 : (int) placeSpeed.getValDouble();
		}

		public int getRotationDegrees() {
			return rotationDegrees;
		}

		public void adjustPlaceSpeed(int amount) {
			if (placeSpeed != null) {
				placeSpeed.setValDouble(placeSpeed.getValDouble() + amount);
			}
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
		private Setting miningSpeed;
		private BlockPos oreTarget;
		private BlockPos miningBlock;
		private BlockPos minedOrePosition;
		private EntityItem dropTarget;
		private int ticksUntilSearch;
		private int ticksWaitingForDrop;
		private int searchRangeSnapshot;
		private int searchCenterX;
		private int searchCenterY;
		private int searchCenterZ;
		private int searchIndex;
		private double nearestSearchDistanceSq;
		private BlockPos nearestSearchOre;
		private boolean searchActive;
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
			searchRange = new Setting("Search Range", this, 12.0D, 2.0D, 24.0D, true);
			searchInterval = new Setting("Search Delay", this, 1.0D, 1.0D, 100.0D, true);
			miningSpeed = new Setting("Mining Speed", this, 2.0D, 1.0D, 4.0D, true);
			manager.rSetting(searchRange);
			manager.rSetting(searchInterval);
			manager.rSetting(miningSpeed);
		}

		private int getSearchRange() {
			return searchRange == null ? 12 : (int) searchRange.getValDouble();
		}

		private int getSearchInterval() {
			return searchInterval == null ? 1 : (int) searchInterval.getValDouble();
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
			if (!searchActive && ticksUntilSearch > 0) {
				--ticksUntilSearch;
				if (ticksUntilSearch > 0) return;
			}
			if (!searchActive) beginOreSearch();
			continueOreSearch();
		}

		private void updateOreTarget() {
			if (!isOre(mc.world.getBlockState(oreTarget).getBlock())) {
				minedOrePosition = oreTarget;
				oreTarget = null;
				ticksWaitingForDrop = DROP_SEARCH_TICKS;
				stopMining();
				restoreMovement();
				restoreHotbarSlot();
				return;
			}
			moveOrMineOre();
		}

		private void moveOrMineOre() {
			BlockPos target = oreTarget;
			BlockPos blockToMine = target;
			EnumFacing hitSide = EnumFacing.UP;
			if (mc.player.getDistanceSqToCenter(target) > MINING_REACH_SQUARED) {
				Vec3d eyes = mc.player.getPositionEyes(1.0F);
				Vec3d oreCenter = new Vec3d(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
				RayTraceResult obstruction = mc.world.rayTraceBlocks(eyes, oreCenter);
				if (obstruction == null || obstruction.typeOfHit != RayTraceResult.Type.BLOCK
						|| target.equals(obstruction.getBlockPos())) {
					stopMining();
					restoreHotbarSlot();
					steerTo(oreCenter.xCoord, oreCenter.yCoord, oreCenter.zCoord);
					return;
				}
				blockToMine = obstruction.getBlockPos();
				if (mc.player.getDistanceSqToCenter(blockToMine) > MINING_REACH_SQUARED) {
					stopMining();
					restoreHotbarSlot();
					steerTo(blockToMine.getX() + 0.5D, blockToMine.getY() + 0.5D,
							blockToMine.getZ() + 0.5D);
					return;
				}
				if (obstruction.sideHit != null) {
					hitSide = obstruction.sideHit;
				}
			}

			restoreMovement();
			int toolSlot = findPickaxe(mc.world.getBlockState(blockToMine));
			if (toolSlot < 0) {
				stopMining();
				restoreHotbarSlot();
				if (!blockToMine.equals(target)) {
					steerTo(blockToMine.getX() + 0.5D, blockToMine.getY() + 0.5D,
							blockToMine.getZ() + 0.5D);
					return;
				}
				notifyMissingPickaxe();
				oreTarget = null;
				ticksUntilSearch = getSearchInterval();
				return;
			}
			if (!blockToMine.equals(miningBlock)) {
				stopMining();
			}
			if (originalHotbarSlot < 0) {
				originalHotbarSlot = mc.player.inventory.currentItem;
			}
			mc.player.inventory.currentItem = toolSlot;
			int damageSteps = SingleplayerWorld.isSingleplayer() && miningSpeed != null
					? (int) miningSpeed.getValDouble() : 1;
			for (int step = 0; step < damageSteps; step++) {
				if (!mc.playerController.onPlayerDamageBlock(blockToMine, hitSide)) break;
				if (mc.world.getBlockState(blockToMine).getBlock() == Blocks.AIR) break;
			}
			miningBlock = blockToMine;
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

		private void beginOreSearch() {
			searchRangeSnapshot = getSearchRange();
			searchCenterX = MathHelper.floor(mc.player.posX);
			searchCenterY = MathHelper.floor(mc.player.posY);
			searchCenterZ = MathHelper.floor(mc.player.posZ);
			searchIndex = 0;
			nearestSearchDistanceSq = (double) searchRangeSnapshot * searchRangeSnapshot;
			nearestSearchOre = null;
			searchActive = true;
		}

		private void continueOreSearch() {
			int movementThreshold = Math.max(3, searchRangeSnapshot / 2);
			if (Math.abs(MathHelper.floor(mc.player.posX) - searchCenterX) > movementThreshold
					|| Math.abs(MathHelper.floor(mc.player.posZ) - searchCenterZ) > movementThreshold) {
				searchActive = false;
				ticksUntilSearch = 0;
				return;
			}
			final int blockBudget = 4096;
			int width = searchRangeSnapshot * 2 + 1;
			int total = width * width * width;
			int scanned = 0;
			while (searchIndex < total && scanned < blockBudget) {
				int index = searchIndex++;
				scanned++;
				int dx = index % width - searchRangeSnapshot;
				int dz = index / width % width - searchRangeSnapshot;
				int dy = index / (width * width) - searchRangeSnapshot;
				int distanceGridSq = dx * dx + dy * dy + dz * dz;
				if (distanceGridSq > searchRangeSnapshot * searchRangeSnapshot) continue;
				int y = searchCenterY + dy;
				if (y < 0 || y >= 256) continue;
				BlockPos pos = new BlockPos(searchCenterX + dx, y, searchCenterZ + dz);
				if (!mc.world.isBlockLoaded(pos)) continue;
				if (!isOre(mc.world.getBlockState(pos).getBlock())) continue;
				double distanceSq = mc.player.getDistanceSqToCenter(pos);
				if (distanceSq <= nearestSearchDistanceSq) {
					nearestSearchOre = pos;
					nearestSearchDistanceSq = distanceSq;
				}
			}

			if (searchIndex < total) return;
			searchActive = false;
			ticksUntilSearch = getSearchInterval();
			oreTarget = nearestSearchOre;
			if (oreTarget == null) {
				restoreMovement();
				return;
			}
			if (findPickaxe(mc.world.getBlockState(oreTarget)) < 0) {
				notifyMissingPickaxe();
				oreTarget = null;
				return;
			}
			mc.player.addChatMessage(new TextComponentString(
					"Auto Mine: ore found within " + searchRangeSnapshot + " blocks."));
			moveOrMineOre();
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
			miningBlock = null;
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
			miningBlock = null;
			searchActive = false;
			searchIndex = 0;
			nearestSearchOre = null;
			minedOrePosition = null;
			dropTarget = null;
			ticksUntilSearch = 0;
			ticksWaitingForDrop = 0;
			nextNoToolMessageTick = 0;
		}
	}
}