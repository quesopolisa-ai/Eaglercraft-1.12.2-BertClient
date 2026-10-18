package net.minecraft.util;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.Minecraft;
import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.movement.InventoryMove;

public class MovementInputFromOptions extends MovementInput {
	private final GameSettings gameSettings;

	public MovementInputFromOptions(GameSettings gameSettingsIn) {
		this.gameSettings = gameSettingsIn;
	}

	public void updatePlayerMoveState() {
		this.moveStrafe = 0.0F;
		this.field_192832_b = 0.0F;
		InventoryMove inventoryMove = null;
		if (BaseClient.instance != null && BaseClient.instance.moduleManager != null) {
			net.minecraft.client.gui.GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof net.minecraft.client.gui.inventory.GuiContainer) {
				inventoryMove = (InventoryMove) BaseClient.instance.moduleManager.getModule("Inventory Move");
			}
		}

		if (this.isKeyDown(this.gameSettings.keyBindForward, inventoryMove)) {
			++this.field_192832_b;
			this.forwardKeyDown = true;
		} else {
			this.forwardKeyDown = false;
		}

		if (this.isKeyDown(this.gameSettings.keyBindBack, inventoryMove)) {
			--this.field_192832_b;
			this.backKeyDown = true;
		} else {
			this.backKeyDown = false;
		}

		if (this.isKeyDown(this.gameSettings.keyBindLeft, inventoryMove)) {
			++this.moveStrafe;
			this.leftKeyDown = true;
		} else {
			this.leftKeyDown = false;
		}

		if (this.isKeyDown(this.gameSettings.keyBindRight, inventoryMove)) {
			--this.moveStrafe;
			this.rightKeyDown = true;
		} else {
			this.rightKeyDown = false;
		}

		this.jump = this.isKeyDown(this.gameSettings.keyBindJump, inventoryMove);
		this.sneak = this.isKeyDown(this.gameSettings.keyBindSneak, inventoryMove);

		if (this.sneak) {
			this.moveStrafe = (float) ((double) this.moveStrafe * 0.3D);
			this.field_192832_b = (float) ((double) this.field_192832_b * 0.3D);
		}
	}

	private boolean isKeyDown(net.minecraft.client.settings.KeyBinding binding, InventoryMove inventoryMove) {
		return inventoryMove == null ? binding.isKeyDown() : inventoryMove.isKeyDown(binding);
	}
}
