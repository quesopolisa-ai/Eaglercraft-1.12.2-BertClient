package baseclient.gg.dhyeybg.baseclient.module.render;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiHUDLayout;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;

public class HUD extends Module {
	private static final long[] CLICK_TIMES = new long[32];
	private static final int PANEL_COUNT = 10;
	private static final String[] PANEL_TITLES = {
			"DESYNC CLIENT", "PERFORMANCE", "LOCATION", "ARMOR", "EFFECTS",
			"KEYSTROKES", "CLICKS", "TARGET", "ELYTRA", "DAMAGE"
	};
	private static final int[] PANEL_WIDTHS = { 150, 174, 194, 184, 194, 130, 92, 190, 176, 100 };
	private static final int[] PANEL_HEIGHTS = { 30, 48, 46, 60, 72, 78, 42, 54, 48, 42 };
	private static final int[] DEFAULT_X = { 8, 8, 8, 8, 650, 365, 8, 390, 8, 390 };
	private static final int[] DEFAULT_Y = { 8, 44, 100, 370, 8, 388, 156, 224, 206, 286 };
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
	private Setting editLayout;
	private final Setting[] panelX = new Setting[PANEL_COUNT];
	private final Setting[] panelY = new Setting[PANEL_COUNT];
	private int previousTargetId = -1;
	private float previousTargetHealth;
	private float lastDamage;
	private long damageIndicatorUntil;

	public HUD() {
		super("HUD", "Modern, configurable on-screen information", Category.RENDER);
	}

	public void registerSettings(SettingsManager manager) {
		showLabel = new Setting("Show Client Label", this, true);
		showPotions = new Setting("Potion Effects", this, false);
		showCoordinates = new Setting("Location Panel", this, true);
		showArmor = new Setting("Armor Panel", this, true);
		showKeys = new Setting("Keystrokes", this, false);
		showStatus = new Setting("Status Panel", this, true);
		showCps = new Setting("CPS Counter", this, false);
		showTarget = new Setting("Target Info", this, false);
		showNotifications = new Setting("Module Notifications", this, false);
		showDamage = new Setting("Damage Indicators", this, false);
		showElytra = new Setting("Elytra Status", this, false);
		customCrosshair = new Setting("Custom Crosshair", this, false);
		editLayout = new Setting("Edit HUD Layout", this, false);
		crosshairSize = Setting.textBox("Crosshair Size", this, 5.0D, 2.0D, 12.0D);
		crosshairGap = Setting.textBox("Crosshair Gap", this, 3.0D, 1.0D, 8.0D);
		ArrayList<String> colors = new ArrayList<String>();
		colors.add("White");
		colors.add("Red");
		colors.add("Cyan");
		colors.add("Yellow");
		colors.add("Green");
		crosshairColor = new Setting("Crosshair Color", this, "Cyan", colors);

		for (int i = 0; i < PANEL_COUNT; i++) {
			String key = PANEL_TITLES[i].replace(' ', '_');
			panelX[i] = Setting.hiddenTextBox("_HUD_X_" + key, this, DEFAULT_X[i], 0.0D, 4096.0D);
			panelY[i] = Setting.hiddenTextBox("_HUD_Y_" + key, this, DEFAULT_Y[i], 0.0D, 4096.0D);
		}

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
		manager.rSetting(editLayout);
		manager.rSetting(crosshairSize);
		manager.rSetting(crosshairGap);
		manager.rSetting(crosshairColor);
		for (int i = 0; i < PANEL_COUNT; i++) {
			manager.rSetting(panelX[i]);
			manager.rSetting(panelY[i]);
		}
	}

	public boolean shouldShowLabel() {
		return showLabel == null || showLabel.getValBoolean();
	}

	public boolean shouldShowToggleNotifications() {
		return showNotifications != null && showNotifications.getValBoolean();
	}

	@Override
	public void onUpdate() {
		updateLayoutEditor();
	}

	public void updateLayoutEditor() {
		if (mc != null && mc.player != null && editLayout != null && editLayout.getValBoolean()
				&& mc.currentScreen == null) {
			mc.displayGuiScreen(new GuiHUDLayout(this));
		}
	}

	@Override
	public void onDisable() {
		if (editLayout != null && editLayout.getValBoolean()) {
			editLayout.setValBoolean(false);
			saveLayout();
		}
		if (mc != null && mc.currentScreen instanceof GuiHUDLayout) {
			mc.displayGuiScreen(null);
		}
	}

	@Override
	public void onRender2D() {
		if (mc == null || mc.player == null || mc.world == null
				|| mc.currentScreen instanceof GuiHUDLayout) {
			return;
		}

		ScaledResolution resolution = new ScaledResolution(mc);
		int screenWidth = resolution.getScaledWidth();
		int screenHeight = resolution.getScaledHeight();
		for (int i = 0; i < PANEL_COUNT; i++) {
			boolean enabled = isPanelEnabled(i);
			if (!enabled && !isEditingLayout()) {
				continue;
			}
			int x = clampX(i, screenWidth);
			int y = clampY(i, screenHeight);
			drawPanel(i, x, y, enabled);
		}

		if (customCrosshair != null && customCrosshair.getValBoolean() && mc.currentScreen == null
				&& mc.gameSettings.thirdPersonView == 0) {
			drawCrosshair(screenWidth, screenHeight);
		}
	}

	private void drawPanel(int panel, int x, int y, boolean enabled) {
		int width = PANEL_WIDTHS[panel];
		int height = PANEL_HEIGHTS[panel];
		int alpha = enabled ? 0xE8 : 0x8A;
		int accent = panel == 3 ? 0xFF66D9C2 : panel == 7 ? 0xFFFF8D7A : 0xFF5DC8E8;
		Gui.drawRect(x - 2, y + 2, x + width + 2, y + height + 3, 0x4A000000);
		Gui.drawRect(x, y, x + width, y + height, (alpha << 24) | 0x101722);
		Gui.drawRect(x, y, x + width, y + 1, 0xFF344150);
		Gui.drawRect(x, y, x + 3, y + height, accent);
		Gui.drawRect(x + 4, y + 18, x + width - 6, y + 19, 0x553A4654);
		mc.fontRendererObj.drawString(PANEL_TITLES[panel], x + 9, y + 6, enabled ? 0xFFF0F4FA : 0xFF8A929E);
		if (!enabled) {
			mc.fontRendererObj.drawString("Hidden - drag to position", x + 9, y + 26, 0xFF9DA7B5);
			return;
		}

		switch (panel) {
		case 0:
			drawBrand(x, y);
			break;
		case 1:
			drawStatus(x, y);
			break;
		case 2:
			drawCoordinates(x, y);
			break;
		case 3:
			drawArmor(x, y);
			break;
		case 4:
			drawPotions(x, y);
			break;
		case 5:
			drawKeys(x, y);
			break;
		case 6:
			drawCps(x, y);
			break;
		case 7:
			drawTarget(x, y);
			break;
		case 8:
			drawElytra(x, y);
			break;
		case 9:
			drawDamage(x, y);
			break;
		default:
			break;
		}
	}

	private void drawBrand(int x, int y) {
		String name = BaseClient.NAME;
		String version = BaseClient.VERSION;
		mc.fontRendererObj.drawString(name, x + 9, y + 20, 0xFFF1F4F8);
		String build = "v" + version;
		int buildWidth = mc.fontRendererObj.getStringWidth(build);
		mc.fontRendererObj.drawString(build, x + PANEL_WIDTHS[0] - buildWidth - 9, y + 20, 0xFF7E92A8);
	}

	private void drawStatus(int x, int y) {
		int ping = -1;
		if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(mc.player.getUniqueID()) != null) {
			ping = mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime();
		}
		String line = "FPS " + mc.getDebugFPS() + "   " + (ping < 0 ? "PING --" : "PING " + ping + "ms");
		mc.fontRendererObj.drawString(line, x + 9, y + 23, 0xFFE6EDF6);
		mc.fontRendererObj.drawString("ATTACK", x + 9, y + 34, 0xFF9CA8B7);
		int barX = x + 57;
		int barWidth = PANEL_WIDTHS[1] - 68;
		Gui.drawRect(barX, y + 35, barX + barWidth, y + 42, 0xFF27313D);
		float cooled = MathHelper.clamp(mc.player.getCooledAttackStrength(0.0F), 0.0F, 1.0F);
		Gui.drawRect(barX, y + 35, barX + Math.round(barWidth * cooled), y + 42,
				cooled >= 1.0F ? 0xFF65D6B6 : 0xFF59BFE0);
	}

	private void drawCoordinates(int x, int y) {
		BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
		String xyz = String.format("X %d   Y %d   Z %d", pos.getX(), pos.getY(), pos.getZ());
		String area = mc.player.getHorizontalFacing().getName().toUpperCase() + "  |  " + mc.world.getBiome(pos).getBiomeName();
		mc.fontRendererObj.drawString(xyz, x + 9, y + 23, 0xFFF0F4FA);
		mc.fontRendererObj.drawString(area, x + 9, y + 34, 0xFF9CA8B7);
	}

	private void drawArmor(int x, int y) {
		for (int displaySlot = 0; displaySlot < 4; displaySlot++) {
			int armorIndex = 3 - displaySlot;
			ItemStack stack = mc.player.inventory.armorInventory.get(armorIndex);
			int slotX = x + 9 + displaySlot * 42;
			int slotY = y + 23;
			Gui.drawRect(slotX - 2, slotY - 2, slotX + 20, slotY + 22, 0x66252F3A);
			if (stack == null || stack.func_190926_b()) {
				mc.fontRendererObj.drawString("--", slotX + 3, slotY + 6, 0xFF738092);
				continue;
			}
			mc.getRenderItem().renderItemAndEffectIntoGUI(mc.player, stack, slotX, slotY);
			mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, slotX, slotY, null);
			int durability = stack.getMaxDamage() <= 0 ? 100
					: 100 * (stack.getMaxDamage() - stack.getItemDamage()) / stack.getMaxDamage();
			int barColor = durability <= 20 ? 0xFFFF6666 : durability <= 50 ? 0xFFFFC45C : 0xFF61D4B2;
			Gui.drawRect(slotX, slotY + 19, slotX + 18, slotY + 21, 0xFF28323D);
			Gui.drawRect(slotX, slotY + 19, slotX + 18 * durability / 100, slotY + 21, barColor);
		}
	}

	private void drawPotions(int x, int y) {
		int lineY = y + 23;
		int count = 0;
		for (PotionEffect effect : mc.player.getActivePotionEffects()) {
			if (count++ >= 4) {
				mc.fontRendererObj.drawString("+ more effects", x + 9, lineY, 0xFF9CA8B7);
				break;
			}
			Potion potion = effect.getPotion();
			String level = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
			String text = potion.getName() + level + "  " + Potion.getPotionDurationString(effect, 1.0F);
			mc.fontRendererObj.drawString(text, x + 9, lineY, 0xFF000000 | potion.getLiquidColor());
			lineY += 10;
		}
		if (count == 0) {
			mc.fontRendererObj.drawString("No active effects", x + 9, lineY, 0xFF8995A3);
		}
	}

	private void drawKeys(int x, int y) {
		drawKey("W", x + 55, y + 22, mc.gameSettings.keyBindForward.isKeyDown());
		drawKey("A", x + 28, y + 40, mc.gameSettings.keyBindLeft.isKeyDown());
		drawKey("S", x + 55, y + 40, mc.gameSettings.keyBindBack.isKeyDown());
		drawKey("D", x + 82, y + 40, mc.gameSettings.keyBindRight.isKeyDown());
		boolean jump = mc.gameSettings.keyBindJump.isKeyDown();
		Gui.drawRect(x + 28, y + 59, x + 103, y + 70, jump ? 0xFF398EA2 : 0xFF252F3B);
		mc.fontRendererObj.drawString("SPACE", x + 49, y + 61, 0xFFE8F0F7);
	}

	private void drawKey(String label, int x, int y, boolean pressed) {
		Gui.drawRect(x, y, x + 20, y + 16, pressed ? 0xFF398EA2 : 0xFF252F3B);
		mc.fontRendererObj.drawString(label, x + 7, y + 4, 0xFFE8F0F7);
	}

	private void drawCps(int x, int y) {
		int clicks = getClicksPerSecond();
		mc.fontRendererObj.drawString(Integer.toString(clicks), x + 10, y + 21, 0xFFF1F4F8);
		mc.fontRendererObj.drawString("clicks/sec", x + 29, y + 25, 0xFF9CA8B7);
	}

	private void drawTarget(int x, int y) {
		EntityLivingBase target = getTarget();
		if (target == null) {
			mc.fontRendererObj.drawString("Aim at a living target", x + 9, y + 25, 0xFF9CA8B7);
			return;
		}
		String name = target.getDisplayName().getFormattedText();
		mc.fontRendererObj.drawString(name, x + 9, y + 23, 0xFFEFF3F8);
		int barX = x + 9;
		int barY = y + 38;
		int barWidth = PANEL_WIDTHS[7] - 18;
		Gui.drawRect(barX, barY, barX + barWidth, barY + 7, 0xFF29333F);
		float ratio = target.getMaxHealth() <= 0.0F ? 0.0F : target.getHealth() / target.getMaxHealth();
		Gui.drawRect(barX, barY, barX + Math.round(barWidth * MathHelper.clamp(ratio, 0.0F, 1.0F)), barY + 7,
				0xFFEB6D78);
		String health = Math.round(target.getHealth() * 10.0F) / 10.0F + " / " + Math.round(target.getMaxHealth());
		mc.fontRendererObj.drawString(health, barX, y + 46, 0xFFD5DEE9);
	}

	private void drawElytra(int x, int y) {
		String flight = "Not flying";
		if (mc.player.isElytraFlying()) {
			double horizontalSpeed = Math.sqrt(mc.player.motionX * mc.player.motionX
					+ mc.player.motionZ * mc.player.motionZ) * 20.0D;
			flight = String.format("%.1f m/s   Y %.1f", horizontalSpeed, mc.player.posY);
		}
		mc.fontRendererObj.drawString(flight, x + 9, y + 25, 0xFF9FEAFF);
	}

	private void drawDamage(int x, int y) {
		EntityLivingBase target = getTarget();
		if (target != null) {
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
		}
		if (System.currentTimeMillis() < damageIndicatorUntil) {
			String damage = "-" + Math.round(lastDamage * 10.0F) / 10.0F + " HP";
			mc.fontRendererObj.drawString(damage, x + 9, y + 22, 0xFFFF7777);
		} else {
			mc.fontRendererObj.drawString("Damage dealt", x + 9, y + 22, 0xFF9CA8B7);
		}
	}

	private EntityLivingBase getTarget() {
		if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY
				&& mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
			return (EntityLivingBase) mc.objectMouseOver.entityHit;
		}
		return null;
	}

	private int getClicksPerSecond() {
		long cutoff = System.currentTimeMillis() - 1000L;
		int clicks = 0;
		for (long clickTime : CLICK_TIMES) {
			if (clickTime >= cutoff) clicks++;
		}
		return clicks;
	}

	private void drawCrosshair(int width, int height) {
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

	public static void recordClick() {
		CLICK_TIMES[nextClickIndex] = System.currentTimeMillis();
		nextClickIndex = (nextClickIndex + 1) % CLICK_TIMES.length;
	}

	public boolean isEditingLayout() {
		return editLayout != null && editLayout.getValBoolean();
	}

	public int getPanelCount() {
		return PANEL_COUNT;
	}

	public String getPanelTitle(int panel) {
		return PANEL_TITLES[panel];
	}

	public int getPanelWidth(int panel) {
		return PANEL_WIDTHS[panel];
	}

	public int getPanelHeight(int panel) {
		return PANEL_HEIGHTS[panel];
	}

	public int getPanelX(int panel) {
		return panelX[panel] == null ? DEFAULT_X[panel] : (int) panelX[panel].getValDouble();
	}

	public int getPanelY(int panel) {
		return panelY[panel] == null ? DEFAULT_Y[panel] : (int) panelY[panel].getValDouble();
	}

	public void setPanelPosition(int panel, int x, int y) {
		if (panelX[panel] != null) panelX[panel].setValDouble(x);
		if (panelY[panel] != null) panelY[panel].setValDouble(y);
	}

	public void saveLayout() {
		if (BaseClient.instance != null) BaseClient.instance.saveConfig();
	}

	public void finishLayoutEdit() {
		if (editLayout != null) editLayout.setValBoolean(false);
		saveLayout();
		if (mc != null && mc.currentScreen instanceof GuiHUDLayout) {
			mc.displayGuiScreen(null);
		}
	}

	public boolean isPanelEnabled(int panel) {
		switch (panel) {
		case 0: return shouldShowLabel();
		case 1: return showStatus != null && showStatus.getValBoolean();
		case 2: return showCoordinates != null && showCoordinates.getValBoolean();
		case 3: return showArmor != null && showArmor.getValBoolean();
		case 4: return showPotions != null && showPotions.getValBoolean();
		case 5: return showKeys != null && showKeys.getValBoolean();
		case 6: return showCps != null && showCps.getValBoolean();
		case 7: return showTarget != null && showTarget.getValBoolean();
		case 8: return showElytra != null && showElytra.getValBoolean();
		case 9: return showDamage != null && showDamage.getValBoolean();
		default: return false;
		}
	}

	private int clampX(int panel, int screenWidth) {
		return Math.max(0, Math.min(getPanelX(panel), screenWidth - PANEL_WIDTHS[panel]));
	}

	private int clampY(int panel, int screenHeight) {
		return Math.max(0, Math.min(getPanelY(panel), screenHeight - PANEL_HEIGHTS[panel]));
	}

	private int getCrosshairColor(String color) {
		if ("Red".equals(color)) return 0xFFFF6464;
		if ("Cyan".equals(color)) return 0xFF5DC8E8;
		if ("Yellow".equals(color)) return 0xFFFFD166;
		if ("Green".equals(color)) return 0xFF65D6B6;
		return 0xFFF1F4F8;
	}
}
