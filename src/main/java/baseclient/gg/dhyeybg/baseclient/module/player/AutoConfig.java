package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class AutoConfig extends Module {

	private Setting restoreModuleStates;

	public AutoConfig() {
		super("Auto Config", "Automatically saves module settings when leaving a world", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		restoreModuleStates = new Setting("Restore Module Toggles", this, true);
		manager.rSetting(restoreModuleStates);
	}

	public boolean shouldSaveOnDisconnect() {
		return isToggled();
	}

	public boolean shouldRestoreModuleStates() {
		return restoreModuleStates == null || restoreModuleStates.getValBoolean();
	}

	@Override
	public void onDisable() {
		if (BaseClient.instance != null && !BaseClient.instance.isLoadingConfig()) {
			BaseClient.instance.saveConfig();
		}
	}
}
