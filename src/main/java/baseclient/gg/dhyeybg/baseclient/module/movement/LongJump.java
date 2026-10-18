package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.player.EntityPlayer;

public class LongJump extends Module {
	private Setting boost;

	public LongJump() {
		super("LongJump", "Adds a directional boost to grounded jumps", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		boost = new Setting("Boost", this, 0.35D, 0.05D, 1.0D, false);
		manager.rSetting(boost);
	}

	public void applyJump(EntityPlayer player) {
		if (!isToggled() || player != mc.player || mc.player.movementInput == null) return;
		double forward = mc.player.movementInput.field_192832_b;
		double strafe = mc.player.movementInput.moveStrafe;
		double magnitude = Math.sqrt(forward * forward + strafe * strafe);
		if (magnitude <= 1.0E-4D) return;
		forward /= magnitude;
		strafe /= magnitude;
		double yaw = Math.toRadians(player.rotationYaw);
		double amount = boost == null ? 0.35D : boost.getValDouble();
		player.motionX += (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * amount;
		player.motionZ += (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * amount;
	}
}
