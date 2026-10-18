package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;

public class Criticals extends Module {

    private Setting offset;

    public Criticals() {
        super("Criticals", "Attempts critical hits with a short position hop", Category.COMBAT);
    }

    public void registerSettings(SettingsManager manager) {
        offset = new Setting("Packet Offset", this, 0.0625D, 0.01D, 0.2D, false);
        manager.rSetting(offset);
    }

    private double getPacketOffset() {
        return offset == null ? 0.0625D : offset.getValDouble();
    }

    private static boolean isActive() {
        BaseClient client = BaseClient.instance;
        if (client == null || client.moduleManager == null) {
            return false;
        }
        Module criticals = client.moduleManager.getModule("Criticals");
        return criticals != null && criticals.isToggled();
    }

    /**
     * Called from PlayerControllerMP.attackEntity just before the use-entity packet is sent.
     * Two position packets make the server see the player falling (fallDistance > 0 and not on
     * the ground), so the server's own critical-hit check passes.
     */
    public static void onAttack(Entity target) {
        if (!(target instanceof EntityLivingBase) || !isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.player.connection == null) {
            return;
        }
        if (mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder() || mc.player.isRiding()) {
            return;
        }
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;
        BaseClient client = BaseClient.instance;
        Criticals module = (Criticals) client.moduleManager.getModule("Criticals");
        double packetOffset = module.getPacketOffset();
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y + packetOffset, z, false));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
    }
}
