package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Spider extends Module {
	private Setting climbSpeed;

	public Spider() {
		super("Spider", "Climbs walls while moving into them", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		climbSpeed = new Setting("Climb Speed", this, 0.2D, 0.05D, 0.5D, false);
		manager.rSetting(climbSpeed);
	}

	@Override
	public void onUpdate() {
		if (mc.player != null && mc.player.movementInput != null && mc.player.isCollidedHorizontally
				&& (mc.player.movementInput.field_192832_b != 0.0F || mc.player.movementInput.moveStrafe != 0.0F)) {
			mc.player.motionY = climbSpeed == null ? 0.2D : climbSpeed.getValDouble();
		}
	}
}
