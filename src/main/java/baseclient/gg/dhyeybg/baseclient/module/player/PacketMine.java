package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class PacketMine extends Module {

	private Setting speed;

	public PacketMine() {
		super("Packet Mine", "Speeds up client-side block breaking while retaining normal dig packets", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		speed = new Setting("Break Speed", this, 2.0D, 1.0D, 5.0D, false);
		manager.rSetting(speed);
	}

	public float getBreakSpeed() {
		return (float) (speed == null ? 2.0D : speed.getValDouble());
	}
}
