package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.entity.EntityPlayerSP;

public class Step extends Module {
	private Setting height;
	private EntityPlayerSP steppedPlayer;
	private float originalStepHeight = 0.6F;

	public Step() {
		super("Step", "Raises the player's automatic step height", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		height = new Setting("Height", this, 1.0D, 0.6D, 2.5D, false);
		manager.rSetting(height);
	}

	@Override
	public void onEnable() {
		capturePlayer();
	}

	@Override
	public void onUpdate() {
		if (mc.player == null) return;
		if (steppedPlayer != mc.player) {
			restorePlayer();
			capturePlayer();
		}
		mc.player.stepHeight = (float) (height == null ? 1.0D : height.getValDouble());
	}

	@Override
	public void onDisable() {
		restorePlayer();
	}

	private void capturePlayer() {
		if (mc.player != null) {
			steppedPlayer = mc.player;
			originalStepHeight = steppedPlayer.stepHeight;
			steppedPlayer.stepHeight = (float) (height == null ? 1.0D : height.getValDouble());
		}
	}

	private void restorePlayer() {
		if (steppedPlayer != null) steppedPlayer.stepHeight = originalStepHeight;
		steppedPlayer = null;
	}
}
