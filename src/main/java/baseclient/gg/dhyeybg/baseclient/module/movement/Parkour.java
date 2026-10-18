package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.util.math.AxisAlignedBB;

public class Parkour extends Module {
    public Parkour() {
        super("Parkour", "Jumps automatically at block edges", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.currentScreen != null
                || !mc.player.onGround || mc.player.isSneaking()) {
            return;
        }

        double moveX = mc.player.motionX;
        double moveZ = mc.player.motionZ;
        if (Math.abs(moveX) + Math.abs(moveZ) < 1.0E-4D && mc.player.movementInput != null) {
            double forward = mc.player.movementInput.field_192832_b;
            double strafe = mc.player.movementInput.moveStrafe;
            double yaw = Math.toRadians(mc.player.rotationYaw);
            moveX = -Math.sin(yaw) * forward + Math.cos(yaw) * strafe;
            moveZ = Math.cos(yaw) * forward + Math.sin(yaw) * strafe;
        }
        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length < 1.0E-4D) {
            return;
        }

        double stepX = moveX / length * 0.35D;
        double stepZ = moveZ / length * 0.35D;
        AxisAlignedBB supportCheck = mc.player.getEntityBoundingBox()
                .offset(stepX, -0.5D, stepZ);
        if (mc.world.getCollisionBoxes(mc.player, supportCheck).isEmpty()) {
            mc.player.jump();
        }
    }
}
