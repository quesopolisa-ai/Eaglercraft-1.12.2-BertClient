package baseclient.gg.dhyeybg.baseclient.module.utilities;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;

public class BoatFly extends Module {
    private Setting horizontalSpeed;
    private Setting verticalSpeed;

    public BoatFly() {
        super("Boat Fly", "Controls horizontal and vertical boat movement", Category.UTILITIES);
    }

    public void registerSettings(SettingsManager manager) {
        horizontalSpeed = new Setting("Horizontal Speed", this, 1.0D, 0.1D, 2.5D, false);
        verticalSpeed = new Setting("Vertical Speed", this, 0.4D, 0.1D, 1.5D, false);
        manager.rSetting(horizontalSpeed);
        manager.rSetting(verticalSpeed);
    }

    public static void applyControls(EntityBoat boat) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.player == null || minecraft.player.getRidingEntity() != boat
                || BaseClient.instance == null || !BaseClient.instance.isModuleEnabled("Boat Fly")
                || minecraft.player.movementInput == null) {
            return;
        }

        Module module = BaseClient.instance.moduleManager.getModule("Boat Fly");
        if (!(module instanceof BoatFly)) {
            return;
        }

        BoatFly boatFly = (BoatFly) module;
        double forward = minecraft.player.movementInput.field_192832_b;
        double strafe = minecraft.player.movementInput.moveStrafe;
        double magnitude = Math.sqrt(forward * forward + strafe * strafe);
        double speed = boatFly.horizontalSpeed == null ? 1.0D : boatFly.horizontalSpeed.getValDouble();
        if (magnitude > 1.0E-4D) {
            forward /= magnitude;
            strafe /= magnitude;
            double yaw = Math.toRadians(minecraft.player.rotationYaw);
            boat.motionX = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * speed;
            boat.motionZ = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * speed;
        } else {
            boat.motionX = 0.0D;
            boat.motionZ = 0.0D;
        }

        double lift = boatFly.verticalSpeed == null ? 0.4D : boatFly.verticalSpeed.getValDouble();
        boat.motionY = minecraft.gameSettings.keyBindJump.isKeyDown() ? lift
                : minecraft.gameSettings.keyBindSneak.isKeyDown() ? -lift : 0.0D;
        boat.setPaddleState(forward > 0.0D, forward < 0.0D);
        boat.rotationYaw = MathHelper.wrapDegrees(minecraft.player.rotationYaw);
    }
}
