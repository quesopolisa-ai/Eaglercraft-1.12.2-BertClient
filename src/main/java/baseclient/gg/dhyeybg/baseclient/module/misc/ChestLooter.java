package baseclient.gg.dhyeybg.baseclient.module.misc;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.lax1dude.eaglercraft.KeyboardConstants;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

public class ChestLooter extends Module {
	private static final double OPEN_RANGE_SQUARED = 20.25D;
	private ContainerChest activeContainer;
	private int nextChestSlot;
	private Setting clickDelay;
	private int nextClickTick;

	public ChestLooter() {
		super("Chest Looter", "Quick-moves chest contents in single-player", Category.MISC);
		setKey(KeyboardConstants.KEY_L);
	}

	public void registerSettings(SettingsManager manager) {
		clickDelay = new Setting("Click Delay", this, 2.0D, 0.0D, 10.0D, true);
		manager.rSetting(clickDelay);
	}

	@Override
	public void onEnable() {
		resetContainer();
		nextClickTick = 0;
		if (mc == null || !SingleplayerWorld.isModuleAllowed(this)) {
			if (mc != null && mc.player != null) {
				mc.player.addChatMessage(
						new TextComponentString("Chest Looter is not allowed in this world."));
			}
			setToggled(false);
		}
	}

	@Override
	public void onDisable() {
		resetContainer();
	}

	@Override
	public void onUpdate() {
		if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
				|| !SingleplayerWorld.isModuleAllowed(this)) {
			resetContainer();
			if (isToggled()) {
				setToggled(false);
			}
			return;
		}
		if (mc.currentScreen instanceof GuiChest && mc.player.openContainer instanceof ContainerChest) {
			lootOpenChest((ContainerChest) mc.player.openContainer);
			return;
		}
		resetContainer();
		if (mc.currentScreen != null) {
			return;
		}

		BlockPos target = findVisibleChest();
		if (target == null) {
			return;
		}
		Vec3d hitVec = new Vec3d(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
		EnumActionResult result = mc.playerController.processRightClickBlock(mc.player, mc.world, target,
				EnumFacing.UP, hitVec, EnumHand.MAIN_HAND);
		if (result == EnumActionResult.SUCCESS) {
			nextClickTick = 0;
		}
	}

	private void lootOpenChest(ContainerChest chest) {
		if (chest != activeContainer) {
			activeContainer = chest;
			nextChestSlot = 0;
		}
		if (!hasStorageSpace()) {
			mc.player.closeScreen();
			resetContainer();
			return;
		}
		if (nextClickTick > 0) {
			nextClickTick--;
			return;
		}

		int chestSize = chest.getLowerChestInventory().getSizeInventory();
		while (nextChestSlot < chestSize) {
			int slot = nextChestSlot++;
			ItemStack stack = chest.getSlot(slot).getStack();
			if (stack != null && !stack.func_190926_b()) {
				if (!canStore(stack)) {
					continue;
				}
				mc.playerController.windowClick(chest.windowId, slot, 0, ClickType.QUICK_MOVE, mc.player);
				nextClickTick = clickDelay == null ? 2 : (int) clickDelay.getValDouble();
				return;
			}
		}
		mc.player.closeScreen();
		resetContainer();
	}

	private BlockPos findVisibleChest() {
		Vec3d eyes = mc.player.getPositionEyes(1.0F);
		BlockPos center = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
		BlockPos closest = null;
		double closestDistance = OPEN_RANGE_SQUARED;
		for (int y = Math.max(0, center.getY() - 3); y <= Math.min(255, center.getY() + 3); ++y) {
			for (int z = center.getZ() - 4; z <= center.getZ() + 4; ++z) {
				for (int x = center.getX() - 4; x <= center.getX() + 4; ++x) {
					BlockPos pos = new BlockPos(x, y, z);
					if (!mc.world.isBlockLoaded(pos)) {
						continue;
					}
					Block block = mc.world.getBlockState(pos).getBlock();
					if (block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST) {
						continue;
					}
					if (!hasLootThatFits(pos)) continue;
					Vec3d target = new Vec3d(x + 0.5D, y + 0.5D, z + 0.5D);
					double dx = target.xCoord - eyes.xCoord;
					double dy = target.yCoord - eyes.yCoord;
					double dz = target.zCoord - eyes.zCoord;
					double distance = dx * dx + dy * dy + dz * dz;
					if (distance >= closestDistance) {
						continue;
					}
					RayTraceResult sight = mc.world.rayTraceBlocks(eyes, target);
					if (sight != null && !pos.equals(sight.getBlockPos())) {
						continue;
					}
					closest = pos;
					closestDistance = distance;
				}
			}
		}
		return closest;
	}

	private boolean hasLootThatFits(BlockPos pos) {
		TileEntity tile = mc.world.getTileEntity(pos);
		if (!(tile instanceof IInventory)) return false;
		if (tile instanceof TileEntityLockableLoot
				&& ((TileEntityLockableLoot) tile).getLootTable() != null) {
			return hasStorageSpace();
		}
		IInventory inventory = (IInventory) tile;
		for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (!stack.func_190926_b() && canStore(stack)) return true;
		}
		return false;
	}

	private boolean hasStorageSpace() {
		if (mc.player.inventory.getFirstEmptyStack() >= 0) return true;
		for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
			ItemStack stack = mc.player.inventory.mainInventory.get(i);
			if (!stack.func_190926_b() && stack.getMaxStackSize() > stack.func_190916_E()) return true;
		}
		return false;
	}

	private boolean canStore(ItemStack incoming) {
		for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
			ItemStack existing = mc.player.inventory.mainInventory.get(i);
			if (existing.func_190926_b()) return true;
			if (ItemStack.areItemsEqual(existing, incoming) && ItemStack.areItemStackTagsEqual(existing, incoming)
					&& existing.func_190916_E() < Math.min(existing.getMaxStackSize(), mc.player.inventory.getInventoryStackLimit())) {
				return true;
			}
		}
		return false;
	}

	private void resetContainer() {
		activeContainer = null;
		nextChestSlot = 0;
	}
}
