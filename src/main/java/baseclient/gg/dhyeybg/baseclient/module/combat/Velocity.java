package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Velocity extends Module {

	private Setting horizontal;
	private Setting vertical;

	public Velocity() {
		super("Velocity", "Cancels knockback", Category.COMBAT);
	}

	public void registerSettings(SettingsManager manager) {
		horizontal = new Setting("Horizontal %", this, 0.0D, 0.0D, 100.0D, true);
		vertical = new Setting("Vertical %", this, 0.0D, 0.0D, 100.0D, true);
		manager.rSetting(horizontal);
		manager.rSetting(vertical);
	}

	public double getHorizontalScale() {
		return (horizontal == null ? 0.0D : horizontal.getValDouble()) / 100.0D;
	}

	public double getVerticalScale() {
		return (vertical == null ? 0.0D : vertical.getValDouble()) / 100.0D;
	}

	@Override
	public boolean onVelocity(int entityId) {
		return getHorizontalScale() == 0.0D && getVerticalScale() == 0.0D;
	}
}