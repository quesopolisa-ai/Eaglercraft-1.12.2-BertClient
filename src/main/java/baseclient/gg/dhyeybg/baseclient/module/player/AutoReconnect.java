package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

public class AutoReconnect extends Module {
    private Setting delay;
    private Setting maxAttempts;
    private long nextAttemptTime;
    private int attempts;

    public AutoReconnect() {
        super("AutoReconnect", "Retries a multiplayer connection after disconnecting", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        delay = new Setting("Retry Delay", this, 5.0D, 1.0D, 60.0D, true);
        maxAttempts = new Setting("Maximum Attempts", this, 3.0D, 1.0D, 10.0D, true);
        manager.rSetting(delay);
        manager.rSetting(maxAttempts);
    }

    @Override
    public void onEnable() {
        attempts = 0;
        nextAttemptTime = 0L;
    }

    public boolean reconnectFrom(GuiScreen disconnectedScreen) {
        if (!isToggled() || mc == null || mc.isSingleplayer() || mc.world != null) {
            return false;
        }
        int limit = maxAttempts == null ? 3 : (int) maxAttempts.getValDouble();
        if (attempts >= limit) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (nextAttemptTime == 0L) {
            int delaySeconds = delay == null ? 5 : (int) delay.getValDouble();
            nextAttemptTime = now + delaySeconds * 1000L;
            return false;
        }
        if (now < nextAttemptTime) {
            return false;
        }
        ServerData server = mc.getCurrentServerData();
        if (server == null) {
            return false;
        }

        attempts++;
        nextAttemptTime = now + 5000L;
        mc.displayGuiScreen(new GuiConnecting(disconnectedScreen, mc, server));
        return true;
    }

    public void resetAttempts() {
        attempts = 0;
        nextAttemptTime = 0L;
    }
}
