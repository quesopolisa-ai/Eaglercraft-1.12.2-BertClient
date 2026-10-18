package baseclient.gg.dhyeybg.baseclient.module.utilities;

import com.mojang.authlib.GameProfile;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.math.MathHelper;

public class Freecam extends Module {
	private Setting speed;
	private EntityOtherPlayerMP camera;

	public Freecam() {
		super("Freecam", "Moves the camera independently from the player", Category.UTILITIES);
	}

	public void registerSettings(SettingsManager manager) {
		speed = new Setting("Speed", this, 0.5D, 0.1D, 2.0D, false);
		manager.rSetting(speed);
	}

	@Override
	public void onEnable() {
		if (mc.player == null || mc.world == null) {
			setToggled(false);
			return;
		}
		camera = new EntityOtherPlayerMP(mc.world, new GameProfile(mc.player.getUniqueID(), mc.player.getName()));
		camera.copyLocationAndAnglesFrom(mc.player);
		camera.prevRotationYaw = camera.rotationYaw;
		camera.prevRotationPitch = camera.rotationPitch;
		camera.prevRotationYawHead = camera.rotationYawHead;
		camera.prevPosX = camera.posX;
		camera.prevPosY = camera.posY;
		camera.prevPosZ = camera.posZ;
		camera.noClip = true;
		mc.setRenderViewEntity(camera);
	}

	@Override
	public void onUpdate() {
		if (camera == null || mc.player == null || mc.world == null) {
			setToggled(false);
			return;
		}
		camera.prevPosX = camera.posX;
		camera.prevPosY = camera.posY;
		camera.prevPosZ = camera.posZ;
		camera.prevRotationYaw = camera.rotationYaw;
		camera.prevRotationPitch = camera.rotationPitch;
		camera.prevRotationYawHead = camera.rotationYawHead;
		double forward = (mc.gameSettings.keyBindForward.isKeyDown() ? 1.0D : 0.0D)
				- (mc.gameSettings.keyBindBack.isKeyDown() ? 1.0D : 0.0D);
		double strafe = (mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0D : 0.0D)
				- (mc.gameSettings.keyBindRight.isKeyDown() ? 1.0D : 0.0D);
		double magnitude = Math.sqrt(forward * forward + strafe * strafe);
		if (magnitude > 0.0D) {
			forward /= magnitude;
			strafe /= magnitude;
		}
		double moveSpeed = speed == null ? 0.5D : speed.getValDouble();
		double yaw = Math.toRadians(camera.rotationYaw);
		double pitch = Math.toRadians(camera.rotationPitch);
		double dx = -Math.sin(yaw) * Math.cos(pitch) * forward + Math.cos(yaw) * strafe;
		double dy = -Math.sin(pitch) * forward
				+ (mc.gameSettings.keyBindJump.isKeyDown() ? 1.0D : 0.0D)
				- (mc.gameSettings.keyBindSneak.isKeyDown() ? 1.0D : 0.0D);
		double dz = Math.cos(yaw) * Math.cos(pitch) * forward + Math.sin(yaw) * strafe;
		camera.setPosition(camera.posX + dx * moveSpeed, camera.posY + dy * moveSpeed,
				camera.posZ + dz * moveSpeed);
		camera.rotationPitch = MathHelper.clamp(camera.rotationPitch, -90.0F, 90.0F);
		camera.rotationYawHead = camera.rotationYaw;
		camera.renderYawOffset = camera.rotationYaw;
	}

	@Override
	public void onDisable() {
		if (mc != null && mc.getRenderViewEntity() == camera) {
			mc.setRenderViewEntity(mc.player);
		}
		camera = null;
	}
}
