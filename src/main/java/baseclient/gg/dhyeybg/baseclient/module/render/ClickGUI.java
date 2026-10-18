package baseclient.gg.dhyeybg.baseclient.module.render;

import baseclient.gg.dhyeybg.baseclient.Clickgui.ClickGui;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class ClickGUI extends Module {
	private Setting size;

	public ClickGUI() {
		super("ClickGUI", "Opens the module menu", Category.RENDER);
	}

	public void registerSettings(SettingsManager manager) {
		size = Setting.textBox("Size", this, 1.0D, 0.5D, 1.5D);
		manager.rSetting(size);
	}

	public float getSize() {
		return size == null ? 1.0F : (float) size.getValDouble();
	}

	@Override
	public void onEnable() {
		mc.displayGuiScreen(new ClickGui());
	}

	@Override
	public void onDisable() {
		if (mc.currentScreen instanceof ClickGui) {
			mc.displayGuiScreen(null);
		}
	}
}