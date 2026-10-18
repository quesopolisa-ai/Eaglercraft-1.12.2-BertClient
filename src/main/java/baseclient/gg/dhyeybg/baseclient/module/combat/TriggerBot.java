package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public class TriggerBot extends Module {
	private Setting delay;
	private Setting targetPlayers;
	private Setting targetMobs;
	private long nextAttackTime;

	public TriggerBot() {
		super("TriggerBot", "Attacks a living entity already under the crosshair", Category.COMBAT);
	}

	public void registerSettings(SettingsManager manager) {
		delay = new Setting("Delay MS", this, 250.0D, 50.0D, 1000.0D, true);
		targetPlayers = new Setting("Target Players", this, true);
		targetMobs = new Setting("Target Mobs", this, true);
		manager.rSetting(delay);
		manager.rSetting(targetPlayers);
		manager.rSetting(targetMobs);
	}

	@Override
	public void onEnable() {
		nextAttackTime = 0L;
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.world == null || mc.playerController == null) return;
		long now = System.currentTimeMillis();
		if (now < nextAttackTime) return;
		RayTraceResult hit = mc.objectMouseOver;
		if (hit == null || hit.typeOfHit != RayTraceResult.Type.ENTITY) return;
		Entity target = hit.entityHit;
		if (!(target instanceof EntityLivingBase) || target == mc.player || !target.isEntityAlive()) return;
		if (target instanceof EntityPlayer ? targetPlayers != null && !targetPlayers.getValBoolean()
				: targetMobs != null && !targetMobs.getValBoolean()) return;

		mc.playerController.attackEntity(mc.player, target);
		mc.player.swingArm(EnumHand.MAIN_HAND);
		nextAttackTime = now + (long) (delay == null ? 250.0D : delay.getValDouble());
	}
}
