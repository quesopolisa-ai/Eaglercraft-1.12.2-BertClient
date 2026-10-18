package baseclient.gg.dhyeybg.baseclient.module.misc;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
    private Setting placementRange;

    private int previousSlot = -1;
    private boolean previousSneak;
    private boolean controllingSneak;

    public Scaffold() {
        super("Scaffold", "Places blocks under and ahead of you", Category.MISC);
    }

    public void registerSettings(SettingsManager manager) {
        placementRange = new Setting("Placement Range", this, 4.5D, 1.0D, 8.0D, false);
        manager.rSetting(placementRange);
    }

    @Override
    public void onDisable() {
        restoreControls();
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
                || mc.currentScreen != null || !mc.player.isEntityAlive()) {
            restoreControls();
            return;
        }

        int slot = findBlockSlot();
        if (slot < 0) {
            restoreControls();
            return;
        }
        if (previousSlot < 0) {
            previousSlot = mc.player.inventory.currentItem;
        }
        mc.player.inventory.currentItem = slot;

        int blockY = MathHelper.floor(mc.player.getEntityBoundingBox().minY - 0.01D);
        double[] direction = getMovementDirection();
        BlockPos underPlayer = new BlockPos(mc.player.posX, blockY, mc.player.posZ);
        BlockPos firstAhead = new BlockPos(mc.player.posX + direction[0] * 0.55D,
                blockY, mc.player.posZ + direction[1] * 0.55D);
        BlockPos fartherAhead = new BlockPos(mc.player.posX + direction[0] * 1.05D,
                blockY, mc.player.posZ + direction[1] * 1.05D);

        boolean placed = tryPlace(underPlayer);
        if (!placed && !firstAhead.equals(underPlayer)) {
            placed = tryPlace(firstAhead);
        }
        if (!placed && !fartherAhead.equals(firstAhead) && !fartherAhead.equals(underPlayer)) {
            placed = tryPlace(fartherAhead);
        }

        updateEdgeSneak(direction, blockY);
    }

    private boolean tryPlace(BlockPos target) {
        if (!mc.world.isBlockLoaded(target) || !mc.world.getWorldBorder().contains(target)
                || !mc.world.getBlockState(target).getMaterial().isReplaceable()) {
            return false;
        }

        ItemStack held = mc.player.getHeldItemMainhand();
        if (held == null || held.func_190926_b() || !(held.getItem() instanceof ItemBlock)) {
            return false;
        }
        ItemBlock itemBlock = (ItemBlock) held.getItem();

        for (EnumFacing side : EnumFacing.values()) {
            BlockPos support = target.offset(side);
            if (!mc.world.isBlockLoaded(support) || !mc.world.getWorldBorder().contains(support)
                    || !mc.world.getBlockState(support).getMaterial().isSolid()) {
                continue;
            }

            EnumFacing clickedFace = side.getOpposite();
            double range = placementRange == null ? 4.5D : placementRange.getValDouble();
            if (mc.player.getDistanceSqToCenter(support) > range * range
                    || !itemBlock.canPlaceBlockOnSide(mc.world, support, clickedFace, mc.player, held)) {
                continue;
            }

            Vec3d hitVec = new Vec3d(
                    support.getX() + 0.5D + clickedFace.getFrontOffsetX() * 0.5D,
                    support.getY() + 0.5D + clickedFace.getFrontOffsetY() * 0.5D,
                    support.getZ() + 0.5D + clickedFace.getFrontOffsetZ() * 0.5D);
            boolean wasSneaking = mc.player.isSneaking();
            mc.player.setSneaking(true);
            EnumActionResult result;
            try {
                result = mc.playerController.processRightClickBlock(mc.player, mc.world, support,
                        clickedFace, hitVec, EnumHand.MAIN_HAND);
            } finally {
                mc.player.setSneaking(wasSneaking);
            }
            if (result == EnumActionResult.SUCCESS) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }

    private double[] getMovementDirection() {
        if (mc.player.movementInput != null) {
            double forward = mc.player.movementInput.field_192832_b;
            double strafe = mc.player.movementInput.moveStrafe;
            double inputLength = Math.sqrt(forward * forward + strafe * strafe);
            if (inputLength > 0.01D) {
                forward /= inputLength;
                strafe /= inputLength;
                double yaw = Math.toRadians(mc.player.rotationYaw);
                return new double[] {
                        -Math.sin(yaw) * forward + Math.cos(yaw) * strafe,
                        Math.cos(yaw) * forward + Math.sin(yaw) * strafe
                };
            }
        }

        double motionLength = Math.sqrt(mc.player.motionX * mc.player.motionX
                + mc.player.motionZ * mc.player.motionZ);
        if (motionLength > 0.03D) {
            return new double[] { mc.player.motionX / motionLength, mc.player.motionZ / motionLength };
        }
        return new double[] { 0.0D, 0.0D };
    }

    private void updateEdgeSneak(double[] direction, int blockY) {
        if (direction[0] == 0.0D && direction[1] == 0.0D) {
            restoreSneak();
            return;
        }
        BlockPos ahead = new BlockPos(mc.player.posX + direction[0] * 0.8D,
                blockY, mc.player.posZ + direction[1] * 0.8D);
        boolean edgeAhead = mc.player.onGround && mc.world.isBlockLoaded(ahead)
                && mc.world.getBlockState(ahead).getMaterial().isReplaceable();
        if (!edgeAhead) {
            restoreSneak();
            return;
        }

        if (!controllingSneak) {
            previousSneak = mc.gameSettings.keyBindSneak.isKeyDown();
            controllingSneak = true;
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
    }

    private void restoreControls() {
        restoreSneak();
        if (previousSlot >= 0 && mc != null && mc.player != null) {
            mc.player.inventory.currentItem = previousSlot;
        }
        previousSlot = -1;
    }

    private void restoreSneak() {
        if (controllingSneak && mc != null && mc.gameSettings != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), previousSneak);
            controllingSneak = false;
        }
    }

    private int findBlockSlot() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (stack == null || stack.func_190926_b() || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            IBlockState state = block.getDefaultState();
            if (block instanceof BlockFalling || block instanceof ITileEntityProvider || !state.isFullCube()) {
                continue;
            }
            return slot;
        }
        return -1;
    }
}
