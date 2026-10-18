package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public class AutoClicker extends Module {
	private Setting cps;
	private Setting targetPlayers;
	private Setting targetMobs;
	private long nextClickTime;

	public AutoClicker() {
		super("AutoClicker", "Automatically attacks the living entity under the crosshair", Category.COMBAT);
	}

	public void registerSettings(SettingsManager manager) {
		cps = new Setting("CPS", this, 8.0D, 1.0D, 20.0D, true);
		targetPlayers = new Setting("Target Players", this, true);
		targetMobs = new Setting("Target Mobs", this, true);
		manager.rSetting(cps);
		manager.rSetting(targetPlayers);
		manager.rSetting(targetMobs);
	}

	@Override
	public void onEnable() {
		nextClickTime = 0L;
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.world == null || mc.playerController == null) return;
		long now = System.currentTimeMillis();
		if (now < nextClickTime) return;
		RayTraceResult hit = mc.objectMouseOver;
		if (hit == null || hit.typeOfHit != RayTraceResult.Type.ENTITY
				|| !(hit.entityHit instanceof EntityLivingBase)) return;
		Entity target = hit.entityHit;
		if (target == mc.player || !target.isEntityAlive()) return;
		if (target instanceof EntityPlayer ? targetPlayers != null && !targetPlayers.getValBoolean()
				: targetMobs != null && !targetMobs.getValBoolean()) return;

		mc.playerController.attackEntity(mc.player, target);
		mc.player.swingArm(EnumHand.MAIN_HAND);
		double clicksPerSecond = cps == null ? 8.0D : cps.getValDouble();
		nextClickTime = now + Math.max(50L, (long) (1000.0D / clicksPerSecond));
	}
}
