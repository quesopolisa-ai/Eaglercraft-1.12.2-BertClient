package net.minecraft.client.gui;

import java.io.IOException;

import baseclient.gg.dhyeybg.baseclient.module.render.HUD;

public class GuiHUDLayout extends GuiScreen {
	private static final int ACCENT = 0xFF5DC8E8;
	private final HUD hud;
	private int draggingPanel = -1;
	private int dragOffsetX;
	private int dragOffsetY;

	public GuiHUDLayout(HUD hud) {
		this.hud = hud;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		Gui.drawRect(0, 0, this.width, this.height, 0x65070B11);
		Gui.drawRect(12, 12, Math.min(this.width - 12, 292), 46, 0xE8151C26);
		Gui.drawRect(12, 12, 15, 46, ACCENT);
		this.fontRendererObj.drawStringWithShadow("HUD LAYOUT", 23, 19, 0xFFF0F4FA);
		this.fontRendererObj.drawStringWithShadow("Drag panels to reposition  |  Esc to save and exit",
				23, 32, 0xFF9EACBA);

		if (draggingPanel >= 0) {
			int x = clamp(mouseX - dragOffsetX, 0, this.width - hud.getPanelWidth(draggingPanel));
			int y = clamp(mouseY - dragOffsetY, 0, this.height - hud.getPanelHeight(draggingPanel));
			hud.setPanelPosition(draggingPanel, x, y);
		}

		for (int i = 0; i < hud.getPanelCount(); i++) {
			int x = clamp(hud.getPanelX(i), 0, this.width - hud.getPanelWidth(i));
			int y = clamp(hud.getPanelY(i), 0, this.height - hud.getPanelHeight(i));
			hud.setPanelPosition(i, x, y);
			int right = x + hud.getPanelWidth(i);
			int bottom = y + hud.getPanelHeight(i);
			boolean enabled = hud.isPanelEnabled(i);
			int fill = enabled ? 0xE8151C26 : 0xB5121720;
			Gui.drawRect(x - 2, y + 2, right + 2, bottom + 2, 0x55000000);
			Gui.drawRect(x, y, right, bottom, fill);
			Gui.drawRect(x, y, right, y + 2, enabled ? ACCENT : 0xFF536170);
			Gui.drawRect(x, y, x + 3, bottom, enabled ? ACCENT : 0xFF536170);
			this.fontRendererObj.drawStringWithShadow(hud.getPanelTitle(i), x + 9, y + 8,
					enabled ? 0xFFF0F4FA : 0xFF98A3AF);
			String state = enabled ? "ON" : "OFF";
			int stateWidth = this.fontRendererObj.getStringWidth(state);
			this.fontRendererObj.drawStringWithShadow(state, right - stateWidth - 8, y + 8,
					enabled ? ACCENT : 0xFF7E8995);
			this.fontRendererObj.drawStringWithShadow("Drag", x + 9, bottom - 13, 0xFF9EACBA);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			for (int i = hud.getPanelCount() - 1; i >= 0; i--) {
				int x = hud.getPanelX(i);
				int y = hud.getPanelY(i);
				if (mouseX >= x && mouseX < x + hud.getPanelWidth(i)
						&& mouseY >= y && mouseY < y + hud.getPanelHeight(i)) {
					draggingPanel = i;
					dragOffsetX = mouseX - x;
					dragOffsetY = mouseY - y;
					break;
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (draggingPanel >= 0) {
			draggingPanel = -1;
			hud.saveLayout();
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			hud.finishLayoutEdit();
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.max(0, max < min ? min : Math.min(value, max)));
	}
}
