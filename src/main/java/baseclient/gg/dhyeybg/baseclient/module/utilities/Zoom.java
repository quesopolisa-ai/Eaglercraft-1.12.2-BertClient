package baseclient.gg.dhyeybg.baseclient.module.utilities;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class Zoom extends Module {
    private Setting magnification;
    private float originalFov;

    public Zoom() {
        super("Zoom", "Reduces the field of view while enabled", Category.UTILITIES);
    }

    public void registerSettings(SettingsManager manager) {
        magnification = new Setting("Magnification", this, 3.0D, 1.5D, 8.0D, false);
        manager.rSetting(magnification);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.gameSettings != null) {
            originalFov = mc.gameSettings.fovSetting;
            updateFov();
        }
    }

    @Override
    public void onUpdate() {
        updateFov();
    }

    private void updateFov() {
        if (mc != null && mc.gameSettings != null) {
            double factor = magnification == null ? 3.0D : magnification.getValDouble();
            mc.gameSettings.fovSetting = (float) Math.max(10.0D, originalFov / factor);
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.gameSettings != null) {
            mc.gameSettings.fovSetting = originalFov;
        }
    }
}
