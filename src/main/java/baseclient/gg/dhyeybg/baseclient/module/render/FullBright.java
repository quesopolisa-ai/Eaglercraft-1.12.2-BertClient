package baseclient.gg.dhyeybg.baseclient.module.render;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class FullBright extends Module {

	private float previousGamma;
	private Setting gamma;

	public FullBright() {
		super("FullBright", "Increases client brightness", Category.RENDER);
	}

	public void registerSettings(SettingsManager manager) {
		gamma = new Setting("Gamma", this, 16.0D, 1.0D, 32.0D, false);
		manager.rSetting(gamma);
	}

	@Override
	public void onEnable() {
		this.previousGamma = mc.gameSettings.gammaSetting;
		mc.gameSettings.gammaSetting = (float) (gamma == null ? 16.0D : gamma.getValDouble());
	}

	@Override
	public void onUpdate() {
		if (mc != null && mc.gameSettings != null) {
			mc.gameSettings.gammaSetting = (float) (gamma == null ? 16.0D : gamma.getValDouble());
		}
	}

	@Override
	public void onDisable() {
		mc.gameSettings.gammaSetting = this.previousGamma;
	}
}