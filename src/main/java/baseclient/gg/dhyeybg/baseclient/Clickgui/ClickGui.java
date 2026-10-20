package baseclient.gg.dhyeybg.baseclient.Clickgui;

import java.io.IOException;
import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Frame;
import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import net.minecraft.client.gui.GuiScreen;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.minecraft.client.gui.Gui;

public class ClickGui extends GuiScreen {

	public static ArrayList<Frame> frames;
	public static int color = 0xFF17212D;

	public ClickGui() {
		if (frames == null) {
			frames = new ArrayList<Frame>();
			int frameX = 5;
			for (Category category : Category.values()) {
				Frame frame = new Frame(category);
				frame.setX(frameX);
				frames.add(frame);
				frameX += frame.getWidth() + 3;
			}
		}
	}

	@Override
	public void initGui() {
		float size = getSize();
		int logicalWidth = (int) (this.width / size);
		int logicalHeight = (int) (this.height / size);
		for (Frame frame : frames) {
			frame.clampToBounds(logicalWidth, logicalHeight);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		Gui.drawRect(0, 0, this.width, this.height, 0x33040A12);
		float size = getSize();
		int logicalWidth = (int) (this.width / size);
		int logicalHeight = (int) (this.height / size);
		GlStateManager.pushMatrix();
		GlStateManager.scale(size, size, 1.0F);
		for (Frame frame : frames) {
			frame.renderFrame(this.fontRendererObj);
			frame.updatePosition((int) (mouseX / size), (int) (mouseY / size));
			frame.clampToBounds(logicalWidth, logicalHeight);
			for (Component comp : frame.getComponents()) {
				comp.updateComponent((int) (mouseX / size), (int) (mouseY / size));
			}
		}
		GlStateManager.popMatrix();
		Gui.drawRect(0, this.height - 20, this.width, this.height, 0xE80D131B);
		Gui.drawRect(0, this.height - 20, this.width, this.height - 19, 0xFF5DC8E8);
		String help = "LEFT CLICK  Toggle     RIGHT CLICK  Settings     DRAG  Panels     ESC  Close";
		int helpWidth = this.fontRendererObj.getStringWidth(help);
		this.fontRendererObj.drawStringWithShadow(help, (this.width - helpWidth) / 2,
				this.height - 13, 0xFFD5E0EA);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		float size = getSize();
		mouseX = (int) (mouseX / size);
		mouseY = (int) (mouseY / size);
		for (Frame frame : frames) {
			if (frame.isWithinHeader(mouseX, mouseY) && mouseButton == 0) {
				frame.setDrag(true);
				frame.dragX = mouseX - frame.getX();
				frame.dragY = mouseY - frame.getY();
			}
			if (frame.isWithinHeader(mouseX, mouseY) && mouseButton == 1) {
				frame.setOpen(!frame.isOpen());
			}
			if (frame.isOpen()) {
				for (Component component : frame.getComponents()) {
					component.mouseClicked(mouseX, mouseY, mouseButton);
				}
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		for (Frame frame : frames) {
			if (frame.isOpen() && keyCode != 1) {
				for (Component component : frame.getComponents()) {
					component.keyTyped(typedChar, keyCode);
				}
			}
		}
		if (keyCode == 1) {
			this.mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		float size = getSize();
		mouseX = (int) (mouseX / size);
		mouseY = (int) (mouseY / size);
		for (Frame frame : frames) {
			frame.setDrag(false);
		}
		for (Frame frame : frames) {
			if (frame.isOpen()) {
				for (Component component : frame.getComponents()) {
					component.mouseReleased(mouseX, mouseY, state);
				}
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	private float getSize() {
		if (BaseClient.instance == null || BaseClient.instance.moduleManager == null) return 1.0F;
		baseclient.gg.dhyeybg.baseclient.module.Module module =
				BaseClient.instance.moduleManager.getModule("ClickGUI");
		float configuredSize = module instanceof baseclient.gg.dhyeybg.baseclient.module.render.ClickGUI
				? ((baseclient.gg.dhyeybg.baseclient.module.render.ClickGUI) module).getSize() : 1.0F;
		int contentWidth = frames == null || frames.isEmpty() ? this.width
				: 5 + frames.size() * (frames.get(0).getWidth() + 3) - 3;
		return Math.min(configuredSize, Math.max(0.5F, (float) this.width / contentWidth));
	}
}
