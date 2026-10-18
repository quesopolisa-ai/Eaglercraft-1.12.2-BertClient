package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Reach extends Module {
	private Setting distance;

	public Reach() {
		super("Reach", "Extends block and entity targeting reach", Category.COMBAT);
	}

	public void registerSettings(SettingsManager manager) {
		distance = new Setting("Distance", this, 5.0D, 3.0D, 6.0D, false);
		manager.rSetting(distance);
	}

	public float getDistance() {
		return distance == null ? 4.5F : (float) distance.getValDouble();
	}
}
