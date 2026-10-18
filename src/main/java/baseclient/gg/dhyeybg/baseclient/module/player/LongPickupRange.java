package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class LongPickupRange extends Module {
    private Setting extraRange;

    public LongPickupRange() {
        super("Long Pickup Range", "Collects nearby items from farther away in singleplayer", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        extraRange = new Setting("Extra Range", this, 3.0D, 1.0D, 8.0D, false);
        manager.rSetting(extraRange);
    }

    public static double getExtraRange() {
        BaseClient client = BaseClient.instance;
        if (client == null || client.moduleManager == null) {
            return 0.0D;
        }

        Module module = client.moduleManager.getModule("Long Pickup Range");
        if (!(module instanceof LongPickupRange) || !module.isToggled()
                || !SingleplayerWorld.isModuleAllowed(module)) {
            return 0.0D;
        }

        LongPickupRange pickupRange = (LongPickupRange) module;
        return pickupRange.extraRange == null ? 3.0D : pickupRange.extraRange.getValDouble();
    }
}
