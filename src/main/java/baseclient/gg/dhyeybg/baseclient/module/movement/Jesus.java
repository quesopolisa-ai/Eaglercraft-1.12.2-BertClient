package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Jesus extends Module {
	private Setting upwardSpeed;

	public Jesus() {
		super("Jesus", "Keeps the player afloat while moving in water", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		upwardSpeed = new Setting("Surface Speed", this, 0.12D, 0.03D, 0.4D, false);
		manager.rSetting(upwardSpeed);
	}

	@Override
	public void onUpdate() {
		if (mc.player != null && mc.player.isInWater() && !mc.player.isSneaking()) {
			double speed = upwardSpeed == null ? 0.12D : upwardSpeed.getValDouble();
			mc.player.motionY = Math.max(mc.player.motionY, speed);
		}
	}

	public double getSurfaceSpeed() {
		return upwardSpeed == null ? 0.12D : upwardSpeed.getValDouble();
	}
}
