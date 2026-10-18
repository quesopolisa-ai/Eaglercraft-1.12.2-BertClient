package baseclient.gg.dhyeybg.baseclient.module.utilities;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;

public class ElytraFly extends Module {
    private Setting horizontalSpeed;
    private Setting verticalSpeed;
    private Setting noGlideDown;
    private long nextFlightAttempt;

    public ElytraFly() {
        super("Elytra Fly", "Improves directional control while gliding", Category.UTILITIES);
    }

    public void registerSettings(SettingsManager manager) {
        horizontalSpeed = new Setting("Horizontal Speed", this, 1.2D, 0.5D, 2.5D, false);
        verticalSpeed = new Setting("Vertical Speed", this, 0.6D, 0.1D, 1.5D, false);
        noGlideDown = new Setting("No Glide Down", this, false);
        manager.rSetting(horizontalSpeed);
        manager.rSetting(verticalSpeed);
        manager.rSetting(noGlideDown);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.player.movementInput == null) {
            return;
        }

        if (!mc.player.isElytraFlying()) {
            startFallFlyingIfPossible();
            return;
        }

        double forward = mc.player.movementInput.field_192832_b;
        double strafe = mc.player.movementInput.moveStrafe;
        double magnitude = Math.sqrt(forward * forward + strafe * strafe);
        if (magnitude > 1.0E-4D) {
            forward /= magnitude;
            strafe /= magnitude;
            double yaw = Math.toRadians(mc.player.rotationYaw);
            double speed = horizontalSpeed == null ? 1.2D : horizontalSpeed.getValDouble();
            mc.player.motionX = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * speed;
            mc.player.motionZ = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * speed;
        }

        double climbSpeed = verticalSpeed == null ? 0.6D : verticalSpeed.getValDouble();
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY = Math.min(climbSpeed, mc.player.motionY + 0.08D);
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY = -climbSpeed;
        } else if (noGlideDown != null && noGlideDown.getValBoolean()) {
            mc.player.motionY = Math.max(0.0D, mc.player.motionY);
        } else {
            float pitch = MathHelper.clamp(mc.player.rotationPitch, -45.0F, 45.0F);
            mc.player.motionY = MathHelper.clamp(mc.player.motionY - Math.sin(Math.toRadians(pitch)) * 0.02D,
                    -climbSpeed, climbSpeed);
        }
        mc.player.fallDistance = 0.0F;
    }

    private void startFallFlyingIfPossible() {
        ItemStack chest = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest == null || !(chest.getItem() instanceof ItemElytra) || ItemElytra.isBroken(chest)
                || mc.player.onGround || mc.player.motionY >= 0.0D) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= nextFlightAttempt) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player,
                    CPacketEntityAction.Action.START_FALL_FLYING));
            nextFlightAttempt = now + 1000L;
        }
    }
}
