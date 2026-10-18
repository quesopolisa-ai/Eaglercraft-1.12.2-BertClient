package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.player.EntityPlayer;

public class HighJump extends Module {
	private Setting height;

	public HighJump() {
		super("HighJump", "Boosts grounded jumps", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		height = new Setting("Height", this, 0.65D, 0.42D, 1.5D, false);
		manager.rSetting(height);
	}

	public void applyJump(EntityPlayer player) {
		if (isToggled()) player.motionY = height == null ? 0.65D : height.getValDouble();
	}
}
