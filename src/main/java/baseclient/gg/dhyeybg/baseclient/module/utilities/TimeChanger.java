package baseclient.gg.dhyeybg.baseclient.module.utilities;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.world.World;

public class TimeChanger extends Module {
    private Setting time;
    private long originalTime;
    private World originalWorld;

    public TimeChanger() {
        super("Time Changer", "Changes the local visual time", Category.UTILITIES);
    }

    public void registerSettings(SettingsManager manager) {
        time = new Setting("Time", this, 6000.0D, 0.0D, 23999.0D, true);
        manager.rSetting(time);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.world != null) {
            originalWorld = mc.world;
            originalTime = mc.world.getWorldTime();
        }
    }

    @Override
    public void onUpdate() {
        if (mc != null && mc.world != null) {
            if (originalWorld != mc.world) {
                originalWorld = mc.world;
                originalTime = mc.world.getWorldTime();
            }
            long dayStart = mc.world.getWorldTime() - mc.world.getWorldTime() % 24000L;
            long selectedTime = time == null ? 6000L : (long) time.getValDouble();
            mc.world.setWorldTime(dayStart + selectedTime);
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.world != null && mc.world == originalWorld) {
            mc.world.setWorldTime(originalTime);
        }
        originalWorld = null;
    }
}
