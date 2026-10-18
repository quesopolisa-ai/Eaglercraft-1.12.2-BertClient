package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Surround extends Module {
    private Setting height;

    public Surround() {
        super("Surround", "Places obsidian around your feet", Category.MOVEMENT);
    }

    public void registerSettings(SettingsManager manager) {
        height = new Setting("Height", this, 1.0D, 1.0D, 2.0D, true);
        manager.rSetting(height);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
                || mc.currentScreen != null || !mc.player.isEntityAlive()) {
            return;
        }

        int obsidianSlot = findObsidian();
        if (obsidianSlot < 0) {
            return;
        }

        BlockPos feet = new BlockPos(mc.player.posX, mc.player.getEntityBoundingBox().minY + 0.01D,
                mc.player.posZ);
        BlockPos[] surrounding = new BlockPos[] {
                feet.north(), feet.south(), feet.east(), feet.west()
        };
        int layers = height == null ? 1 : (int) height.getValDouble();
        int previousSlot = mc.player.inventory.currentItem;

        for (int layer = 0; layer < layers; layer++) {
            for (BlockPos position : surrounding) {
                BlockPos target = position.up(layer);
                if (!mc.world.isBlockLoaded(target) || !mc.world.getWorldBorder().contains(target)
                        || !mc.world.getBlockState(target).getMaterial().isReplaceable()) {
                    continue;
                }

                if (placeObsidian(target, obsidianSlot, previousSlot)) {
                    return;
                }
            }
        }
    }

    private int findObsidian() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (stack != null && !stack.func_190926_b() && stack.getItem() instanceof ItemBlock
                    && ((ItemBlock) stack.getItem()).getBlock() == Blocks.OBSIDIAN) {
                return slot;
            }
        }
        return -1;
    }

    private boolean placeObsidian(BlockPos target, int obsidianSlot, int previousSlot) {
        double reach = mc.playerController.getBlockReachDistance();
        if (mc.player.getDistanceSqToCenter(target) > reach * reach) {
            return false;
        }

        for (EnumFacing side : EnumFacing.values()) {
            BlockPos support = target.offset(side);
            if (!mc.world.isBlockLoaded(support) || !mc.world.getWorldBorder().contains(support)
                    || !mc.world.getBlockState(support).getMaterial().isSolid()) {
                continue;
            }

            EnumFacing clickedFace = side.getOpposite();
            mc.player.inventory.currentItem = obsidianSlot;
            mc.playerController.updateController();
            Vec3d hitVec = new Vec3d(support.getX() + 0.5D + clickedFace.getFrontOffsetX() * 0.5D,
                    support.getY() + 0.5D + clickedFace.getFrontOffsetY() * 0.5D,
                    support.getZ() + 0.5D + clickedFace.getFrontOffsetZ() * 0.5D);
            boolean wasSneaking = mc.player.isSneaking();
            mc.player.setSneaking(true);
            EnumActionResult result;
            try {
                result = mc.playerController.processRightClickBlock(mc.player, mc.world, support, clickedFace,
                        hitVec, EnumHand.MAIN_HAND);
            } finally {
                mc.player.setSneaking(wasSneaking);
                mc.player.inventory.currentItem = previousSlot;
                mc.playerController.updateController();
            }

            if (result == EnumActionResult.SUCCESS) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }
}
