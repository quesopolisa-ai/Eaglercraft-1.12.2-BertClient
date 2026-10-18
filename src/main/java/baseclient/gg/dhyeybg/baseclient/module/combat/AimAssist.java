package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class AimAssist extends Module {
	private Setting players;
	private Setting mobs;
	private Setting range;
	private Setting smoothing;

	public AimAssist() {
		super("AimAssist", "Gradually aims at nearby entities", Category.COMBAT);
	}

	public void registerSettings(SettingsManager manager) {
		players = new Setting("Players", this, true);
		mobs = new Setting("Mobs", this, true);
		range = new Setting("Range", this, 4.0D, 1.0D, 8.0D, false);
		smoothing = new Setting("Smoothing", this, 5.0D, 1.0D, 20.0D, false);
		manager.rSetting(players);
		manager.rSetting(mobs);
		manager.rSetting(range);
		manager.rSetting(smoothing);
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.world == null) return;
		EntityLivingBase target = null;
		double closestDistance = getRange() * getRange();
		for (Entity entity : mc.world.loadedEntityList) {
			if (!(entity instanceof EntityLivingBase) || entity == mc.player || !entity.isEntityAlive()) continue;
			if (entity instanceof EntityPlayer ? players != null && !players.getValBoolean()
					: mobs != null && !mobs.getValBoolean()) continue;
			double distance = mc.player.getDistanceSqToEntity(entity);
			if (distance < closestDistance) {
				closestDistance = distance;
				target = (EntityLivingBase) entity;
			}
		}
		if (target == null) return;

		double dx = target.posX - mc.player.posX;
		double dz = target.posZ - mc.player.posZ;
		double dy = target.posY + target.getEyeHeight() * 0.75D
				- (mc.player.posY + mc.player.getEyeHeight());
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
		float factor = (float) (1.0D / Math.max(1.0D, smoothing == null ? 5.0D : smoothing.getValDouble()));
		mc.player.rotationYaw += MathHelper.wrapDegrees(yaw - mc.player.rotationYaw) * factor;
		mc.player.rotationPitch += MathHelper.wrapDegrees(pitch - mc.player.rotationPitch) * factor;
		mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90.0F, 90.0F);
	}

	private double getRange() {
		return range == null ? 4.0D : range.getValDouble();
	}
}
