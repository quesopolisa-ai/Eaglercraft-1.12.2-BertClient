package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

/**
 * Configurable single-player combat helper.
 *
 * Settings below can be changed in source. This module fails closed outside
 * an integrated single-player world and uses the normal Minecraft attack
 * cooldown. Through-wall targeting is intentionally available only offline.
 */
public class KillAura extends Module {

    private Setting range;
    private Setting players;
    private Setting hostileMobs;
    private Setting passiveMobs;
    private Setting throughWalls;
    private Setting respectCooldown;

    public KillAura() {
        super("KillAura", "Configurable single-player auto-attack", Category.COMBAT);
    }

    public void registerSettings(SettingsManager manager) {
        range = new Setting("Range", this, 6.0D, 1.0D, 6.0D, false);
        players = new Setting("Players", this, true);
        hostileMobs = new Setting("Hostile Mobs", this, true);
        passiveMobs = new Setting("Passive Mobs", this, false);
        throughWalls = new Setting("Through Walls", this, false);
        respectCooldown = new Setting("Respect Cooldown", this, true);
        manager.rSetting(range);
        manager.rSetting(players);
        manager.rSetting(hostileMobs);
        manager.rSetting(passiveMobs);
        manager.rSetting(throughWalls);
        manager.rSetting(respectCooldown);
    }

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.playerController == null) {
            return;
        }

        if (!SingleplayerWorld.isModuleAllowed(this)) {
            setToggled(false);
            return;
        }

        if (mc.currentScreen != null || mc.player.isHandActive()) {
            return;
        }

        if ((respectCooldown == null || respectCooldown.getValBoolean())
                && mc.player.getCooledAttackStrength(0.0F) < 1.0F) {
            return;
        }

        EntityLivingBase target = findNearestTarget();
        if (target != null) {
            mc.playerController.attackEntity(mc.player, target);
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    private EntityLivingBase findNearestTarget() {
        EntityLivingBase bestTarget = null;
        double configuredRange = range == null ? 6.0D : range.getValDouble();
        double bestDistanceSq = configuredRange * configuredRange;

        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) {
                continue;
            }

            EntityLivingBase living = (EntityLivingBase) entity;
            if (living == mc.player || !living.isEntityAlive() || living.isDead) {
                continue;
            }

            if (!isTargetEnabled(living)) {
                continue;
            }

            double distanceSq = mc.player.getDistanceSqToEntity(living);
            if (distanceSq > bestDistanceSq) {
                continue;
            }

            if ((throughWalls == null || !throughWalls.getValBoolean()) && !mc.player.canEntityBeSeen(living)) {
                continue;
            }

            bestDistanceSq = distanceSq;
            bestTarget = living;
        }

        return bestTarget;
    }

    private boolean isTargetEnabled(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return players == null || players.getValBoolean();
        }
        if (entity instanceof IMob) {
            return hostileMobs == null || hostileMobs.getValBoolean();
        }
        if (entity instanceof EntityAnimal || entity instanceof EntityAmbientCreature
                || entity instanceof EntityWaterMob) {
            return passiveMobs != null && passiveMobs.getValBoolean();
        }
        return passiveMobs != null && passiveMobs.getValBoolean();
    }
}
