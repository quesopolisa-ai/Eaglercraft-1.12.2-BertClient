package baseclient.gg.dhyeybg.baseclient.module.render;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class CameraEffects extends Module {
    private Setting disableBobbing;
    private Setting disableHurtCamera;
    private boolean previousBobbing;

    public CameraEffects() {
        super("Camera Effects", "Controls view bobbing and hurt-camera shake", Category.RENDER);
    }

    public void registerSettings(SettingsManager manager) {
        disableBobbing = new Setting("Disable View Bobbing", this, false);
        disableHurtCamera = new Setting("Disable Hurt Camera", this, true);
        manager.rSetting(disableBobbing);
        manager.rSetting(disableHurtCamera);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.gameSettings != null) {
            previousBobbing = mc.gameSettings.viewBobbing;
        }
        updateBobbing();
    }

    @Override
    public void onUpdate() {
        updateBobbing();
    }

    private void updateBobbing() {
        if (mc != null && mc.gameSettings != null && disableBobbing != null
                && disableBobbing.getValBoolean()) {
            mc.gameSettings.viewBobbing = false;
        }
    }

    public static boolean shouldDisableHurtCamera() {
        BaseClient client = BaseClient.instance;
        if (client == null || !client.isModuleEnabled("Camera Effects")) {
            return false;
        }
        Module module = client.moduleManager.getModule("Camera Effects");
        return module instanceof CameraEffects && ((CameraEffects) module).disableHurtCamera != null
                && ((CameraEffects) module).disableHurtCamera.getValBoolean();
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.gameSettings != null) {
            mc.gameSettings.viewBobbing = previousBobbing;
        }
    }
}
