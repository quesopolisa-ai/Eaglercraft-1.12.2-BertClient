package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.entity.EntityPlayerSP;

public class NoFall extends Module {
	private Setting fallThreshold;

	public NoFall() {
		super("NoFall", "Prevents fall damage by reporting grounded after a configured fall", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		fallThreshold = new Setting("Packet Threshold", this, 3.0D, 0.0D, 20.0D, false);
		manager.rSetting(fallThreshold);
	}

	public boolean shouldReportGrounded(EntityPlayerSP player) {
		if (!isToggled()
				|| player.fallDistance < (fallThreshold == null ? 3.0D : fallThreshold.getValDouble())) {
			return false;
		}
		player.fallDistance = 0.0F;
		return true;
	}

	@Override
	public void onUpdate() {
		if (mc.player != null && mc.player.onGround) mc.player.fallDistance = 0.0F;
	}
}
