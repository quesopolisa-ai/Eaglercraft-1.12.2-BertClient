package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;

public class WTap extends Module {
    private static int restoreTicks;
    private static boolean restoreSprint;

    public WTap() {
        super("WTap", "Briefly resets sprint after an attack", Category.COMBAT);
    }

    public static void onAttack() {
        BaseClient client = BaseClient.instance;
        if (client == null || !client.isModuleEnabled("WTap") || mc == null || mc.player == null) {
            return;
        }
        restoreSprint = mc.player.isSprinting();
        if (restoreSprint) {
            mc.player.setSprinting(false);
            restoreTicks = 2;
        }
    }

    @Override
    public void onUpdate() {
        if (restoreTicks <= 0 || mc == null || mc.player == null) {
            return;
        }
        if (mc.player.movementInput != null) {
            mc.player.movementInput.field_192832_b = 0.0F;
        }
        if (--restoreTicks == 0) {
            if (restoreSprint && mc.gameSettings.keyBindSprint.isKeyDown() && mc.player.onGround) {
                mc.player.setSprinting(true);
            }
            restoreSprint = false;
        }
    }
}
