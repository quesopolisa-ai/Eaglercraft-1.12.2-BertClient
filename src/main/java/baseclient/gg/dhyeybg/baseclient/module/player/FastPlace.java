package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class FastPlace extends Module {
	private Setting delay;

	public FastPlace() {
		super("FastPlace", "Removes the client delay between item uses", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		delay = new Setting("Delay Ticks", this, 0.0D, 0.0D, 4.0D, true);
		manager.rSetting(delay);
	}

	public int getDelayTicks() {
		return delay == null ? 0 : (int) delay.getValDouble();
	}
}
