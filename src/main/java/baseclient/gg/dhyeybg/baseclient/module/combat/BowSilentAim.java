package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.MathHelper;

public class BowSilentAim extends Module {
    private Setting range;
    private Setting players;
    private Setting hostileMobs;

    public BowSilentAim() {
        super("Bow Silent Aim", "Aims bow movement packets at a nearby target", Category.COMBAT);
    }

    public void registerSettings(SettingsManager manager) {
        range = new Setting("Range", this, 24.0D, 1.0D, 64.0D, false);
        players = new Setting("Players", this, true);
        hostileMobs = new Setting("Hostile Mobs", this, true);
        manager.rSetting(range);
        manager.rSetting(players);
        manager.rSetting(hostileMobs);
    }

    public float[] getSilentRotation() {
        if (mc == null || mc.player == null || mc.world == null || !mc.player.isHandActive()
                || mc.player.isRiding()
                || mc.player.getActiveItemStack().getItem() != Items.BOW) {
            return null;
        }

        int charge = mc.player.getActiveItemStack().getMaxItemUseDuration() - mc.player.getItemInUseCount();
        float arrowVelocity = ItemBow.getArrowVelocity(charge) * 3.0F;
        if (arrowVelocity < 0.3F) {
            return null;
        }

        EntityLivingBase target = findTarget();
        if (target == null) {
            return null;
        }

        double dx = target.posX + target.motionX * 0.5D - mc.player.posX;
        double dy = target.posY + target.getEyeHeight() * 0.85D + target.motionY * 0.5D
                - (mc.player.posY + mc.player.getEyeHeight());
        double dz = target.posZ + target.motionZ * 0.5D - mc.player.posZ;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double speedSquared = (double) arrowVelocity * arrowVelocity;
        double discriminant = speedSquared * speedSquared
                - 0.05D * (0.05D * horizontalDistance * horizontalDistance + 2.0D * dy * speedSquared);
        double verticalAim = dy;
        if (horizontalDistance > 0.0D && discriminant >= 0.0D) {
            verticalAim = (speedSquared - Math.sqrt(discriminant)) / (0.05D * horizontalDistance)
                    * horizontalDistance;
        }
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(verticalAim, horizontalDistance) * 180.0D / Math.PI);
        return new float[] { MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0F, 90.0F) };
    }

    private EntityLivingBase findTarget() {
        EntityLivingBase nearest = null;
        double configuredRange = range == null ? 24.0D : range.getValDouble();
        double nearestDistanceSq = configuredRange * configuredRange;

        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.player || !entity.isEntityAlive()) {
                continue;
            }
            if (entity instanceof EntityPlayer) {
                if (players != null && !players.getValBoolean()) {
                    continue;
                }
            } else if (entity instanceof IMob) {
                if (hostileMobs != null && !hostileMobs.getValBoolean()) {
                    continue;
                }
            } else {
                continue;
            }

            double distanceSq = mc.player.getDistanceSqToEntity(entity);
            if (distanceSq < nearestDistanceSq) {
                nearest = (EntityLivingBase) entity;
                nearestDistanceSq = distanceSq;
            }
        }
        return nearest;
    }
}
