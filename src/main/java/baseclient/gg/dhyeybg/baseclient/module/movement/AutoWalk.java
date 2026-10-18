package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("Auto Walk", "Continues walking forward", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (mc != null && mc.player != null && mc.player.movementInput != null && mc.currentScreen == null) {
            mc.player.movementInput.field_192832_b = 1.0F;
        }
    }
}
