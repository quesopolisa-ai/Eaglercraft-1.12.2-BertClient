package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class FastLadder extends Module {
    private Setting speed;

    public FastLadder() {
        super("Fast Ladder", "Adjusts climbing speed on ladders and vines", Category.MOVEMENT);
    }

    public void registerSettings(SettingsManager manager) {
        speed = new Setting("Climb Speed", this, 0.35D, 0.2D, 1.0D, false);
        manager.rSetting(speed);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.player.movementInput == null || !mc.player.isOnLadder()) {
            return;
        }

        double climbSpeed = speed == null ? 0.35D : speed.getValDouble();
        if (mc.player.movementInput.field_192832_b > 0.0F) {
            mc.player.motionY = Math.max(mc.player.motionY, climbSpeed);
        } else if (mc.player.movementInput.field_192832_b < 0.0F) {
            mc.player.motionY = -climbSpeed;
        }
    }
}
