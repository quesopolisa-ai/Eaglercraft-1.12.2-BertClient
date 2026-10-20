package baseclient.gg.dhyeybg.baseclient.Clickgui.component;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.Clickgui.ClickGui;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class Frame {

	private static final int WIDTH = 130;
	private static final int HEADER_HEIGHT = 21;
	private static final int ROW_HEIGHT = 14;
	public ArrayList<Component> components;
	public Category category;
	private boolean open;
	private int width;
	private int y;
	private int x;
	private int barHeight;
	private boolean isDragging;
	public int dragX;
	public int dragY;

	public Frame(Category cat) {
		this.components = new ArrayList<Component>();
		this.category = cat;
		this.width = WIDTH;
		this.x = 5;
		this.y = 5;
		this.barHeight = HEADER_HEIGHT;
		this.dragX = 0;
		this.open = false;
		this.isDragging = false;
		int tY = this.barHeight;

		for (Module mod : BaseClient.instance.moduleManager.getModulesInCategory(category)) {
			Button modButton = new Button(mod, this, tY);
			this.components.add(modButton);
			tY += ROW_HEIGHT;
		}
	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	public void setX(int newX) {
		this.x = newX;
	}

	public void setY(int newY) {
		this.y = newY;
	}

	public void setDrag(boolean drag) {
		this.isDragging = drag;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public void renderFrame(FontRenderer fontRenderer) {
		Gui.drawRect(this.x - 2, this.y + 2, this.x + this.width + 2,
				this.y + this.barHeight + 3, 0x57000000);
		Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.barHeight, ClickGui.color);
		Gui.drawRect(this.x, this.y, this.x + this.width, this.y + 1, 0xFF3B4A5A);
		Gui.drawRect(this.x, this.y, this.x + 3, this.y + this.barHeight, 0xFF5DC8E8);
		Gui.drawRect(this.x, this.y + this.barHeight - 1, this.x + this.width,
				this.y + this.barHeight, 0xFF5DC8E8);
		fontRenderer.drawStringWithShadow(displayCategory(), this.x + 9, this.y + 7, 0xFFF0F4FA);
		String count = Integer.toString(this.components.size());
		int countWidth = fontRenderer.getStringWidth(count);
		fontRenderer.drawStringWithShadow(count, this.x + this.width - countWidth - 22,
				this.y + 7, 0xFF8797A8);
		fontRenderer.drawStringWithShadow(this.open ? "-" : "+",
				this.x + this.width - 12, this.y + 7, 0xFF73D3E6);
		if (this.open) {
			Gui.drawRect(this.x, this.y + this.barHeight, this.x + this.width,
					this.y + getExpandedHeight(), 0xEF101720);
			Gui.drawRect(this.x + this.width - 1, this.y + this.barHeight,
					this.x + this.width, this.y + getExpandedHeight(), 0xFF2B3948);
			for (Component component : components) {
				component.renderComponent();
			}
		}
	}

	private String displayCategory() {
		String name = this.category.name().toLowerCase();
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	public void refresh() {
		int off = this.barHeight;
		for (Component comp : components) {
			comp.setOff(off);
			off += comp.getHeight();
		}
	}

	public int getExpandedHeight() {
		int height = this.barHeight;
		for (Component component : components) {
			height += component.getHeight();
		}
		return height;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public void updatePosition(int mouseX, int mouseY) {
		if (this.isDragging) {
			this.setX(mouseX - dragX);
			this.setY(mouseY - dragY);
		}
	}

	public void clampToBounds(int screenWidth, int screenHeight) {
		this.x = Math.max(0, Math.min(this.x, Math.max(0, screenWidth - this.width)));
		this.y = Math.max(0, Math.min(this.y, Math.max(0, screenHeight - this.barHeight)));
	}

	public boolean isWithinHeader(int x, int y) {
		return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.barHeight;
	}
}
