package baseclient.gg.dhyeybg.baseclient.module.misc;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class SellCactus extends Module {

    private final Setting delaySeconds = Setting.textBox("Delay (seconds)", this, 2.0D, 0.25D, 3600.0D);
    private long nextSendAt;

    public SellCactus() {
        super("Sell Cactus", "Automatically runs /sell cactus at a configurable interval", Category.MISC);
    }

    public void registerSettings(SettingsManager settingsManager) {
        settingsManager.rSetting(delaySeconds);
    }

    @Override
    public void onEnable() {
        nextSendAt = System.currentTimeMillis() + getDelayMillis();
    }

    @Override
    public void onDisable() {
        nextSendAt = 0L;
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.player.connection == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextSendAt) {
            return;
        }

        mc.player.sendChatMessage("/sell cactus");
        nextSendAt = now + getDelayMillis();
    }

    private long getDelayMillis() {
        double seconds = delaySeconds.getValDouble();
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) {
            seconds = 2.0D;
        }
        seconds = Math.max(0.25D, Math.min(3600.0D, seconds));
        return (long) (seconds * 1000.0D);
    }
}
