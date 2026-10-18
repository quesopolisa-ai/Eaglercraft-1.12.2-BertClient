package baseclient.gg.dhyeybg.baseclient.module.utilities;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.util.math.Vec3d;

public class ElytraBoost extends Module {
    private Setting boost;
    private Setting delay;
    private long nextBoostTime;

    public ElytraBoost() {
        super("Elytra Boost", "Adds a forward impulse while using an Elytra", Category.UTILITIES);
    }

    public void registerSettings(SettingsManager manager) {
        boost = new Setting("Boost Strength", this, 0.8D, 0.1D, 2.0D, false);
        delay = new Setting("Delay MS", this, 750.0D, 100.0D, 3000.0D, true);
        manager.rSetting(boost);
        manager.rSetting(delay);
    }

    @Override
    public void onEnable() {
        nextBoostTime = 0L;
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || !mc.player.isElytraFlying()
                || mc.player.isHandActive()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextBoostTime) {
            return;
        }

        Vec3d look = mc.player.getLook(1.0F);
        double strength = boost == null ? 0.8D : boost.getValDouble();
        mc.player.motionX += look.xCoord * strength;
        mc.player.motionY += look.yCoord * strength * 0.35D;
        mc.player.motionZ += look.zCoord * strength;

        long delayMillis = delay == null ? 750L : (long) delay.getValDouble();
        nextBoostTime = now + Math.max(100L, delayMillis);
    }
}
