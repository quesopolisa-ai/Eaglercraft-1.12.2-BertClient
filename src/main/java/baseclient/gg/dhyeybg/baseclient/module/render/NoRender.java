package baseclient.gg.dhyeybg.baseclient.module.render;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class NoRender extends Module {
    private Setting effect;

    public NoRender() {
        super("NoRender", "Suppresses selected visual effects", Category.RENDER);
    }

    public void registerSettings(SettingsManager manager) {
        ArrayList<String> options = new ArrayList<String>();
        options.add("Rain");
        options.add("BossBar");
        options.add("HurtCam");
        options.add("Armor");
        options.add("FireOverlay");
        effect = new Setting("Effect", this, "Rain", options);
        manager.rSetting(effect);
    }

    public static boolean isEffectDisabled(String name) {
        if (BaseClient.instance == null || BaseClient.instance.moduleManager == null
                || !BaseClient.instance.isModuleEnabled("NoRender")) {
            return false;
        }
        Module module = BaseClient.instance.moduleManager.getModule("NoRender");
        if (!(module instanceof NoRender)) {
            return false;
        }
        NoRender noRender = (NoRender) module;
        return noRender.effect != null && name.equalsIgnoreCase(noRender.effect.getValString());
    }
}
