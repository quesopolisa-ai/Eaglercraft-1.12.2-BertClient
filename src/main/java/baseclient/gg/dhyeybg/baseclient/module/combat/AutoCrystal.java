package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoCrystal extends Module {
    private Setting targetRange;
    private Setting placeRange;
    private Setting delay;
    private Setting selfDistance;
    private Setting autoPlace;
    private EntityPlayer target;
    private long nextActionTime;

    public AutoCrystal() {
        super("Auto Crystal", "Places and detonates End Crystals near nearby players", Category.COMBAT);
    }

    public void registerSettings(SettingsManager manager) {
        targetRange = new Setting("Target Range", this, 6.0D, 1.0D, 12.0D, false);
        placeRange = new Setting("Place Range", this, 4.5D, 1.0D, 6.0D, false);
        delay = new Setting("Action Delay MS", this, 500.0D, 100.0D, 2000.0D, true);
        selfDistance = new Setting("Min Self Distance", this, 3.5D, 0.0D, 8.0D, false);
        autoPlace = new Setting("Auto Place", this, true);
        manager.rSetting(targetRange);
        manager.rSetting(placeRange);
        manager.rSetting(delay);
        manager.rSetting(selfDistance);
        manager.rSetting(autoPlace);
    }

    @Override
    public void onEnable() {
        nextActionTime = 0L;
        target = null;
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
                || mc.currentScreen != null || !mc.player.isEntityAlive() || mc.player.isHandActive()) {
            return;
        }

        if (target != null) {
            if (!target.isEntityAlive() || target.isDead) {
                setToggled(false);
                return;
            }
        } else {
            target = findTarget();
            if (target == null) {
                return;
            }
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) {
            return;
        }

        if (attackCrystalNear(target)) {
            scheduleNextAction(now);
        } else if ((autoPlace == null || autoPlace.getValBoolean()) && placeCrystalNear(target)) {
            scheduleNextAction(now);
        }
    }

    private EntityPlayer findTarget() {
        EntityPlayer nearest = null;
        double range = targetRange == null ? 6.0D : targetRange.getValDouble();
        double nearestDistanceSq = range * range;

        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityPlayer) || entity == mc.player || !entity.isEntityAlive()) {
                continue;
            }

            double distanceSq = mc.player.getDistanceSqToEntity(entity);
            if (distanceSq < nearestDistanceSq) {
                nearest = (EntityPlayer) entity;
                nearestDistanceSq = distanceSq;
            }
        }
        return nearest;
    }

    private boolean attackCrystalNear(EntityPlayer target) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityEnderCrystal) || !entity.isEntityAlive()) {
                continue;
            }
            if (entity.getDistanceSqToEntity(target) > 4.5D * 4.5D
                    || mc.player.getDistanceSqToEntity(entity) > 4.5D * 4.5D) {
                continue;
            }

            mc.playerController.attackEntity(mc.player, entity);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            return true;
        }
        return false;
    }

    private boolean placeCrystalNear(EntityPlayer target) {
        int crystalSlot = findCrystalSlot();
        if (crystalSlot < 0) {
            return false;
        }

        BlockPos targetPos = new BlockPos(target.posX, target.posY, target.posZ);
        int previousSlot = mc.player.inventory.currentItem;
        double placeDistance = placeRange == null ? 4.5D : placeRange.getValDouble();
        double minimumSelfDistance = selfDistance == null ? 3.5D : selfDistance.getValDouble();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos base = targetPos.add(x, y, z);
                    if (!isValidBase(base)) {
                        continue;
                    }

                    double crystalX = base.getX() + 0.5D;
                    double crystalY = base.getY() + 1.0D;
                    double crystalZ = base.getZ() + 0.5D;
                    if (mc.player.getDistanceSq(crystalX, crystalY, crystalZ)
                            < minimumSelfDistance * minimumSelfDistance
                            || mc.player.getDistanceSq(crystalX, crystalY, crystalZ)
                                    > placeDistance * placeDistance
                            || target.getDistanceSq(crystalX, crystalY, crystalZ) > 4.5D * 4.5D) {
                        continue;
                    }

                    mc.player.inventory.currentItem = crystalSlot;
                    mc.playerController.updateController();
                    EnumActionResult result;
                    try {
                        Vec3d hitVec = new Vec3d(base.getX() + 0.5D, base.getY() + 1.0D,
                                base.getZ() + 0.5D);
                        result = mc.playerController.processRightClickBlock(mc.player, mc.world, base,
                                EnumFacing.UP, hitVec, EnumHand.MAIN_HAND);
                    } finally {
                        mc.player.inventory.currentItem = previousSlot;
                        mc.playerController.updateController();
                    }

                    if (result == EnumActionResult.SUCCESS) {
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int findCrystalSlot() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (stack != null && !stack.func_190926_b() && stack.getItem() == Items.END_CRYSTAL) {
                return slot;
            }
        }
        return -1;
    }

    private boolean isValidBase(BlockPos base) {
        if (!mc.world.isBlockLoaded(base) || !mc.world.isBlockLoaded(base.up(2))
                || !mc.world.getWorldBorder().contains(base)
                || !mc.world.getWorldBorder().contains(base.up(2))
                || mc.player.getDistanceSqToCenter(base) > (placeRange == null ? 4.5D : placeRange.getValDouble())
                        * (placeRange == null ? 4.5D : placeRange.getValDouble())) {
            return false;
        }

        Block block = mc.world.getBlockState(base).getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
            return false;
        }

        BlockPos above = base.up();
        BlockPos aboveTwo = above.up();
        if (!mc.world.isAirBlock(above) || !mc.world.isAirBlock(aboveTwo)) {
            return false;
        }

        AxisAlignedBB crystalSpace = new AxisAlignedBB(above.getX(), above.getY(), above.getZ(),
                above.getX() + 1.0D, above.getY() + 2.0D, above.getZ() + 1.0D);
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, crystalSpace).isEmpty();
    }

    private void scheduleNextAction(long now) {
        long actionDelay = delay == null ? 500L : (long) delay.getValDouble();
        nextActionTime = now + Math.max(100L, actionDelay);
    }
}
