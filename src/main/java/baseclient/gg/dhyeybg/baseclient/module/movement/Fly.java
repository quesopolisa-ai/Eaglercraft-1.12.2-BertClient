package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Fly extends Module {
	private Setting speed;
	private Setting horizontalSpeed;

	public Fly() {
		super("Fly", "Moves vertically using jump and sneak", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		speed = new Setting("Vertical Speed", this, 0.8D, 0.1D, 2.0D, false);
		manager.rSetting(speed);
		horizontalSpeed = new Setting("Horizontal Speed", this, 1.0D, 0.1D, 2.5D, false);
		manager.rSetting(horizontalSpeed);
	}

	@Override
	public void onUpdate() {
		if (mc.player == null) return;
		double verticalSpeed = speed == null ? 0.8D : speed.getValDouble();
		if (mc.gameSettings.keyBindJump.isKeyDown()) {
			mc.player.motionY = verticalSpeed;
		} else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
			mc.player.motionY = -verticalSpeed;
		} else {
			mc.player.motionY = 0.0D;
		}
		mc.player.fallDistance = 0.0F;
		if (mc.player.movementInput == null) return;
		double forward = mc.player.movementInput.field_192832_b;
		double strafe = mc.player.movementInput.moveStrafe;
		double magnitude = Math.sqrt(forward * forward + strafe * strafe);
		if (magnitude > 1.0E-4D) {
			forward /= magnitude;
			strafe /= magnitude;
			double yaw = Math.toRadians(mc.player.rotationYaw);
			double movementSpeed = horizontalSpeed == null ? 1.0D : horizontalSpeed.getValDouble();
			mc.player.motionX = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * movementSpeed;
			mc.player.motionZ = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * movementSpeed;
		} else {
			mc.player.motionX = 0.0D;
			mc.player.motionZ = 0.0D;
		}
	}

	@Override
	public void onDisable() {
		if (mc.player != null && !mc.player.onGround) mc.player.motionY = 0.0D;
	}
}
