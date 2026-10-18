package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiVoid extends Module {
    private Setting rescueHeight;
    private double safeX;
    private double safeY;
    private double safeZ;
    private boolean hasSafePosition;
    private World safeWorld;

    public AntiVoid() {
        super("Anti Void", "Returns you to your last grounded position before a void fall", Category.MOVEMENT);
    }

    public void registerSettings(SettingsManager manager) {
        rescueHeight = Setting.textBox("Rescue Height", this, 2.0D, -64.0D, 8.0D);
        manager.rSetting(rescueHeight);
    }

    @Override
    public void onEnable() {
        hasSafePosition = false;
        safeWorld = null;
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null) {
            hasSafePosition = false;
            safeWorld = null;
            return;
        }

        if (safeWorld != mc.world) {
            hasSafePosition = false;
            safeWorld = mc.world;
        }

        double triggerHeight = rescueHeight == null ? 2.0D : rescueHeight.getValDouble();
        BlockPos supportPos = new BlockPos(mc.player.posX,
                mc.player.getEntityBoundingBox().minY - 0.05D, mc.player.posZ);
        if (mc.player.onGround && mc.world.getBlockState(supportPos).getMaterial().isSolid()
                && mc.player.posY > triggerHeight + 1.0D) {
            safeX = mc.player.posX;
            safeY = mc.player.posY;
            safeZ = mc.player.posZ;
            hasSafePosition = true;
        } else if (hasSafePosition && mc.player.posY <= triggerHeight
                && mc.player.motionY < 0.0D) {
            mc.player.setPosition(safeX, safeY, safeZ);
            mc.player.motionX = 0.0D;
            mc.player.motionY = 0.0D;
            mc.player.motionZ = 0.0D;
            mc.player.fallDistance = 0.0F;
            mc.player.onGround = true;
        }
    }

    @Override
    public void onDisable() {
        hasSafePosition = false;
        safeWorld = null;
    }
}
