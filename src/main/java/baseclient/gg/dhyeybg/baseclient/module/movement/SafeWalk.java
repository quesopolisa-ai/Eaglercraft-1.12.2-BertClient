package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class SafeWalk extends Module {
	private Setting edgeStep;

	public SafeWalk() {
		super("Safe Walk", "Prevents walking off block edges", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		edgeStep = new Setting("Edge Step", this, 0.05D, 0.01D, 0.2D, false);
		manager.rSetting(edgeStep);
	}

	public double getEdgeStep() {
		return edgeStep == null ? 0.05D : edgeStep.getValDouble();
	}
}
