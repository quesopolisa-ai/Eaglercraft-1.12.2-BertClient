package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("Auto Respawn", "Respawns after death", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc != null && mc.player != null && mc.player.isDead) {
            if (mc.currentScreen != null) {
                mc.displayGuiScreen(null);
            }
            mc.player.respawnPlayer();
        }
    }
}
