package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.util.ScreenShotHelper;

public class AutoScreenshot extends Module {
    private Setting onDeath;
    private Setting healthThreshold;
    private boolean wasDead;
    private boolean lowHealthCaptured;

    public AutoScreenshot() {
        super("AutoScreenshot", "Captures screenshots on death or low health", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        onDeath = new Setting("On Death", this, true);
        healthThreshold = new Setting("Low Health", this, 0.0D, 0.0D, 20.0D, true);
        manager.rSetting(onDeath);
        manager.rSetting(healthThreshold);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null) {
            return;
        }
        boolean dead = mc.player.isDead || mc.player.getHealth() <= 0.0F;
        if (dead && !wasDead && onDeath != null && onDeath.getValBoolean()) {
            capture();
        }
        wasDead = dead;

        double threshold = healthThreshold == null ? 0.0D : healthThreshold.getValDouble();
        if (threshold > 0.0D && mc.player.getHealth() > threshold) {
            lowHealthCaptured = false;
        } else if (threshold > 0.0D && mc.player.getHealth() > 0.0F && !lowHealthCaptured) {
            capture();
            lowHealthCaptured = true;
        }
    }

    private void capture() {
        if (mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null) {
            mc.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot());
        }
    }
}
