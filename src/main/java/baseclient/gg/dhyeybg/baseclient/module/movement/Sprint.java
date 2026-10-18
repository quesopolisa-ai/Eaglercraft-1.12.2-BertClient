package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Sprint extends Module {

	private Setting omniDirectional;

	public Sprint() {
		super("Sprint", "Sprints while moving forward", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		omniDirectional = new Setting("Omni-Directional", this, false);
		manager.rSetting(omniDirectional);
	}

	@Override
	public void onUpdate() {
		if (mc.player != null) {
			boolean moving = mc.player.movementInput.forwardKeyDown;
			if (omniDirectional != null && omniDirectional.getValBoolean()) {
				moving = moving || mc.player.movementInput.backKeyDown
						|| mc.player.movementInput.leftKeyDown || mc.player.movementInput.rightKeyDown;
			}
			mc.player.setSprinting(moving && !mc.player.isSneaking());
		}
	}

	@Override
	public void onDisable() {
		if (mc.player != null) {
			mc.player.setSprinting(false);
		}
	}
}