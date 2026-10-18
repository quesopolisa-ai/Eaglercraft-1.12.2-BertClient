package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Speed extends Module {
	private Setting multiplier;

	public Speed() {
		super("Speed", "Increases movement speed while moving", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		multiplier = new Setting("Multiplier", this, 1.35D, 1.05D, 2.5D, false);
		manager.rSetting(multiplier);
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.player.movementInput == null) return;
		double forward = mc.player.movementInput.field_192832_b;
		double strafe = mc.player.movementInput.moveStrafe;
		double magnitude = Math.sqrt(forward * forward + strafe * strafe);
		if (magnitude < 1.0E-4D) return;
		forward /= magnitude;
		strafe /= magnitude;
		double yaw = Math.toRadians(mc.player.rotationYaw);
		double targetSpeed = 0.1D * (multiplier == null ? 1.35D : multiplier.getValDouble());
		double x = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * targetSpeed;
		double z = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * targetSpeed;
		mc.player.motionX = x;
		mc.player.motionZ = z;
	}
}
