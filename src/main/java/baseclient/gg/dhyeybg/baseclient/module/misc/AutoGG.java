package baseclient.gg.dhyeybg.baseclient.module.misc;

import java.util.ArrayList;
import java.util.Locale;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.util.text.ITextComponent;

public class AutoGG extends Module {
    private static long nextMessageTime;
    private Setting message;

    public AutoGG() {
        super("Auto GG", "Sends one preset message after common match-end notices", Category.MISC);
    }

    public void registerSettings(SettingsManager manager) {
        ArrayList<String> options = new ArrayList<String>();
        options.add("gg");
        options.add("GG");
        options.add("Good game!");
        message = new Setting("Message", this, "gg", options);
        manager.rSetting(message);
    }

    public static void inspectChat(ITextComponent component) {
        BaseClient client = BaseClient.instance;
        if (client == null || !client.isModuleEnabled("Auto GG") || mc == null || mc.player == null
                || component == null || System.currentTimeMillis() < nextMessageTime) {
            return;
        }
        String text = component.getUnformattedText().toLowerCase(Locale.ROOT);
        if (!(text.contains("won the game") || text.contains("wins the game") || text.contains("has won")
                || text.contains("victory") || text.contains("match ended") || text.contains("game over"))) {
            return;
        }
        Module module = client.moduleManager.getModule("Auto GG");
        if (module instanceof AutoGG) {
            AutoGG autoGG = (AutoGG) module;
            String outgoing = autoGG.message == null ? "gg" : autoGG.message.getValString();
            mc.player.sendChatMessage(outgoing);
            nextMessageTime = System.currentTimeMillis() + 30000L;
        }
    }
}
