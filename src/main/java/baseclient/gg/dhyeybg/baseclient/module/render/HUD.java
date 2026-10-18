package baseclient.gg.dhyeybg.baseclient.module.render;

import java.util.ArrayList;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;

public class HUD extends Module {
	private static final long[] CLICK_TIMES = new long[32];
	private static int nextClickIndex;

	private Setting showLabel;
	private Setting showPotions;
	private Setting showCoordinates;
	private Setting showArmor;
	private Setting showKeys;
	private Setting showStatus;
	private Setting showCps;
	private Setting showTarget;
	private Setting showNotifications;
	private Setting showDamage;
	private Setting showElytra;
	private Setting customCrosshair;
	private Setting crosshairSize;
	private Setting crosshairGap;
	private Setting crosshairColor;
	private int previousTargetId = -1;
	private float previousTargetHealth;
	private float lastDamage;
	private long damageIndicatorUntil;

	public HUD() {
		super("HUD", "Displays the module list", Category.RENDER);
		this.visible = false;
	}

	public void registerSettings(SettingsManager manager) {
		showLabel = new Setting("Show Client Label", this, true);
		showPotions = new Setting("Potion Effects", this, false);
		showCoordinates = new Setting("Coordinates", this, false);
		showArmor = new Setting("Armor Status", this, false);
		showKeys = new Setting("Keystrokes", this, false);
		showStatus = new Setting("FPS and Cooldown", this, false);
		showCps = new Setting("CPS Counter", this, false);
		showTarget = new Setting("Target Info", this, false);
		showNotifications = new Setting("Module Notifications", this, false);
		showDamage = new Setting("Damage Indicators", this, false);
		showElytra = new Setting("Elytra Status", this, false);
		customCrosshair = new Setting("Custom Crosshair", this, false);
		crosshairSize = Setting.textBox("Crosshair Size", this, 5.0D, 2.0D, 12.0D);
		crosshairGap = Setting.textBox("Crosshair Gap", this, 3.0D, 1.0D, 8.0D);
		ArrayList<String> colors = new ArrayList<String>();
		colors.add("White");
		colors.add("Red");
		colors.add("Cyan");
		colors.add("Yellow");
		colors.add("Green");
		crosshairColor = new Setting("Crosshair Color", this, "White", colors);
		manager.rSetting(showLabel);
		manager.rSetting(showPotions);
		manager.rSetting(showCoordinates);
		manager.rSetting(showArmor);
		manager.rSetting(showKeys);
		manager.rSetting(showStatus);
		manager.rSetting(showCps);
		manager.rSetting(showTarget);
		manager.rSetting(showNotifications);
		manager.rSetting(showDamage);
		manager.rSetting(showElytra);
		manager.rSetting(customCrosshair);
		manager.rSetting(crosshairSize);
		manager.rSetting(crosshairGap);
		manager.rSetting(crosshairColor);
	}

	public boolean shouldShowLabel() {
		return showLabel == null || showLabel.getValBoolean();
	}

	@Override
	public void onRender2D() {
		if (mc == null || mc.player == null || mc.world == null) {
			return;
		}

		ScaledResolution resolution = new ScaledResolution(mc);
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		int line = 10;

		if (showCoordinates != null && showCoordinates.getValBoolean()) {
			BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
			String direction = mc.player.getHorizontalFacing().getName().toUpperCase();
			String biome = mc.world.getBiome(pos).getBiomeName();
			mc.fontRendererObj.drawStringWithShadow(
					String.format("XYZ: %d %d %d | %s | %s", pos.getX(), pos.getY(), pos.getZ(), direction, biome),
					2, height - 30, 0xFFFFFFFF);
		}

		if (showArmor != null && showArmor.getValBoolean()) {
			int y = height - 42;
			for (int i = 0; i < mc.player.inventory.armorInventory.size(); i++) {
				net.minecraft.item.ItemStack stack = mc.player.inventory.armorInventory.get(i);
				if (stack == null || stack.func_190926_b()) {
					continue;
				}
				int remaining = stack.getMaxDamage() <= 0 ? 100
						: 100 * (stack.getMaxDamage() - stack.getItemDamage()) / stack.getMaxDamage();
				mc.fontRendererObj.drawStringWithShadow(stack.getDisplayName() + " " + remaining + "%",
						2, y, remaining <= 20 ? 0xFFFF5555 : 0xFFFFFFFF);
				y -= line;
			}
		}

		if (showPotions != null && showPotions.getValBoolean()) {
			int y = 2;
			for (PotionEffect effect : mc.player.getActivePotionEffects()) {
				Potion potion = effect.getPotion();
				String level = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
				String text = potion.getName() + level + " "
						+ Potion.getPotionDurationString(effect, 1.0F);
				mc.fontRendererObj.drawStringWithShadow(text, width - mc.fontRendererObj.getStringWidth(text) - 2,
						y, potion.getLiquidColor());
				y += line;
			}
		}

		if (showKeys != null && showKeys.getValBoolean()) {
			String keys = keyLabel(mc.gameSettings.keyBindForward, "W") + " "
					+ keyLabel(mc.gameSettings.keyBindLeft, "A") + " "
					+ keyLabel(mc.gameSettings.keyBindBack, "S") + " "
					+ keyLabel(mc.gameSettings.keyBindRight, "D");
			mc.fontRendererObj.drawStringWithShadow(keys, width / 2 - 22, height - 45, 0xFFFFFFFF);
			mc.fontRendererObj.drawStringWithShadow(keyLabel(mc.gameSettings.keyBindJump, "JUMP"),
					width / 2 - 12, height - 35, 0xFFFFFFFF);
		}

		if (showStatus != null && showStatus.getValBoolean()) {
			String ping = "";
			if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(mc.player.getUniqueID()) != null) {
				ping = " | " + mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime() + " ms";
			}
			String status = "FPS " + mc.getDebugFPS() + ping + " | Attack "
					+ Math.round(mc.player.getCooledAttackStrength(0.0F) * 100.0F) + "%";
			mc.fontRendererObj.drawStringWithShadow(status,
					width - mc.fontRendererObj.getStringWidth(status) - 2, height - 12, 0xFFFFFFFF);
		}

		if (showCps != null && showCps.getValBoolean()) {
			long cutoff = System.currentTimeMillis() - 1000L;
			int clicks = 0;
			for (long clickTime : CLICK_TIMES) {
				if (clickTime >= cutoff) {
					clicks++;
				}
			}
			mc.fontRendererObj.drawStringWithShadow("CPS: " + clicks, 2, height - 12, 0xFFFFFFFF);
		}

		if (showTarget != null && showTarget.getValBoolean()
				&& mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof net.minecraft.entity.EntityLivingBase) {
			net.minecraft.entity.EntityLivingBase target =
					(net.minecraft.entity.EntityLivingBase) mc.objectMouseOver.entityHit;
			String info = target.getDisplayName().getFormattedText() + " "
					+ Math.round(target.getHealth()) + "/" + Math.round(target.getMaxHealth());
			mc.fontRendererObj.drawStringWithShadow(info,
					width / 2 - mc.fontRendererObj.getStringWidth(info) / 2, height / 2 + 18, 0xFFFFFFFF);
		}

		if (showDamage != null && showDamage.getValBoolean()
				&& mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof net.minecraft.entity.EntityLivingBase) {
			net.minecraft.entity.EntityLivingBase target =
					(net.minecraft.entity.EntityLivingBase) mc.objectMouseOver.entityHit;
			if (target.getEntityId() != previousTargetId) {
				previousTargetId = target.getEntityId();
				previousTargetHealth = target.getHealth();
			} else if (target.getHealth() < previousTargetHealth) {
				lastDamage = previousTargetHealth - target.getHealth();
				damageIndicatorUntil = System.currentTimeMillis() + 750L;
				previousTargetHealth = target.getHealth();
			} else if (target.getHealth() > previousTargetHealth) {
				previousTargetHealth = target.getHealth();
			}
			if (System.currentTimeMillis() < damageIndicatorUntil) {
				String damage = "-" + Math.round(lastDamage * 10.0F) / 10.0F + " HP";
				mc.fontRendererObj.drawStringWithShadow(damage,
						width / 2 - mc.fontRendererObj.getStringWidth(damage) / 2, height / 2 + 28, 0xFFFF5555);
			}
		}

		if (showElytra != null && showElytra.getValBoolean() && mc.player.isElytraFlying()) {
			double horizontalSpeed = Math.sqrt(mc.player.motionX * mc.player.motionX
					+ mc.player.motionZ * mc.player.motionZ) * 20.0D;
			String flight = String.format("Elytra %.1f m/s | Y %.1f", horizontalSpeed, mc.player.posY);
			mc.fontRendererObj.drawStringWithShadow(flight, 2, 12, 0xFF80E8FF);
		}

		if (customCrosshair != null && customCrosshair.getValBoolean() && mc.currentScreen == null
				&& mc.gameSettings.thirdPersonView == 0) {
			int centerX = width / 2;
			int centerY = height / 2;
			int size = crosshairSize == null ? 5 : (int) crosshairSize.getValDouble();
			int gap = crosshairGap == null ? 3 : (int) crosshairGap.getValDouble();
			int color = crosshairColor == null ? 0xFFFFFFFF : getCrosshairColor(crosshairColor.getValString());
			Gui.drawRect(centerX - 1, centerY - gap - size, centerX + 1, centerY - gap, color);
			Gui.drawRect(centerX - 1, centerY + gap, centerX + 1, centerY + gap + size, color);
			Gui.drawRect(centerX - gap - size, centerY - 1, centerX - gap, centerY + 1, color);
			Gui.drawRect(centerX + gap, centerY - 1, centerX + gap + size, centerY + 1, color);
		}
	}

	public static void recordClick() {
		CLICK_TIMES[nextClickIndex] = System.currentTimeMillis();
		nextClickIndex = (nextClickIndex + 1) % CLICK_TIMES.length;
	}

	public boolean shouldShowToggleNotifications() {
		return showNotifications != null && showNotifications.getValBoolean();
	}

	private String keyLabel(net.minecraft.client.settings.KeyBinding binding, String label) {
		return binding.isKeyDown() ? "[" + label + "]" : label;
	}

	private int getCrosshairColor(String color) {
		if ("Red".equals(color)) return 0xFFFF5555;
		if ("Cyan".equals(color)) return 0xFF55FFFF;
		if ("Yellow".equals(color)) return 0xFFFFFF55;
		if ("Green".equals(color)) return 0xFF55FF55;
		return 0xFFFFFFFF;
	}
}