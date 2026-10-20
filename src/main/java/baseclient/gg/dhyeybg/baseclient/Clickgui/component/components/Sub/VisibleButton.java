package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class VisibleButton extends Component {

	private boolean hovered;
	private Button parent;
	private int offset;
	private int x;
	private int y;
	private Module mod;

	public VisibleButton(Button button, Module mod, int offset) {
		this.parent = button;
		this.mod = mod;
		this.x = button.parent.getX() + button.parent.getWidth();
		this.y = button.parent.getY() + button.offset;
		this.offset = offset;
	}

	@Override
	public void setOff(int newOff) {
		offset = newOff;
	}

	@Override
	public void renderComponent() {
		int left = parent.parent.getX();
		int top = parent.parent.getY() + offset;
		int right = left + parent.parent.getWidth();
		Gui.drawRect(left + 2, top, right, top + 14, this.hovered ? 0xFF202C39 : 0xFF131A23);
		Gui.drawRect(left, top, left + 2, top + 14, 0xFF263241);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Array List",
			left + 9, top + 3, 0xFFB8C4D0);
		String value = mod.visible ? "ON" : "OFF";
		int valueWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(value,
			right - valueWidth - 5, top + 3, mod.visible ? 0xFF5DC8E8 : 0xFF7F8B98);
	}

	@Override
	public void updateComponent(int mouseX, int mouseY) {
		this.hovered = isMouseOnButton(mouseX, mouseY);
		this.y = parent.parent.getY() + offset;
		this.x = parent.parent.getX();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (isMouseOnButton(mouseX, mouseY) && button == 0 && this.parent.open) {
			mod.visible = !mod.visible;
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		return x > this.x && x < this.x + parent.parent.getWidth() && y > this.y && y < this.y + 14;
	}
}
