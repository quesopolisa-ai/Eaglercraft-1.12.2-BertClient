package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Timer extends Module {
	private Setting speed;
	private float originalTickLength = 50.0F;

	public Timer() {
		super("Timer", "Adjusts local tick speed", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		speed = new Setting("Tick Speed", this, 1.5D, 0.5D, 3.0D, false);
		manager.rSetting(speed);
	}

	@Override
	public void onEnable() {
		originalTickLength = mc.getClientTickLength();
	}

	@Override
	public void onUpdate() {
		double multiplier = speed == null ? 1.5D : speed.getValDouble();
		mc.setClientTickLength((float) (originalTickLength / multiplier));
	}

	@Override
	public void onDisable() {
		mc.setClientTickLength(originalTickLength);
	}
}
