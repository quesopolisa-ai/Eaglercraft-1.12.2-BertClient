package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class NoSlow extends Module {
	private Setting slowdown;

	public NoSlow() {
		super("No Slow", "Prevents item use from slowing movement", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		slowdown = new Setting("Slowdown", this, 0.0D, 0.0D, 1.0D, false);
		manager.rSetting(slowdown);
	}

	public float getSlowdown() {
		return slowdown == null ? 0.0F : (float) slowdown.getValDouble();
	}
}
