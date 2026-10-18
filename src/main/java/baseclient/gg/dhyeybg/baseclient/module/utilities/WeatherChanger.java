package baseclient.gg.dhyeybg.baseclient.module.utilities;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.world.World;

public class WeatherChanger extends Module {
    private Setting weather;
    private float originalRain;
    private float originalThunder;
    private World originalWorld;

    public WeatherChanger() {
        super("Weather Changer", "Changes local weather visuals", Category.UTILITIES);
    }

    public void registerSettings(SettingsManager manager) {
        ArrayList<String> options = new ArrayList<String>();
        options.add("Clear");
        options.add("Rain");
        options.add("Thunder");
        weather = new Setting("Weather", this, "Clear", options);
        manager.rSetting(weather);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.world != null) {
            originalWorld = mc.world;
            originalRain = mc.world.getRainStrength(1.0F);
            originalThunder = mc.world.getThunderStrength(1.0F);
        }
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.world == null || weather == null) {
            return;
        }
        if (originalWorld != mc.world) {
            originalWorld = mc.world;
            originalRain = mc.world.getRainStrength(1.0F);
            originalThunder = mc.world.getThunderStrength(1.0F);
        }
        String selected = weather.getValString();
        float rain = "Clear".equals(selected) ? 0.0F : 1.0F;
        float thunder = "Thunder".equals(selected) ? 1.0F : 0.0F;
        mc.world.setRainStrength(rain);
        mc.world.setThunderStrength(thunder);
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.world != null && mc.world == originalWorld) {
            mc.world.setRainStrength(originalRain);
            mc.world.setThunderStrength(originalThunder);
        }
        originalWorld = null;
    }
}
