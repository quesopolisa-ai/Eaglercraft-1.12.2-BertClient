package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import net.lax1dude.eaglercraft.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Keybind extends Component {

	private boolean hovered;
	private boolean binding;
	private Button parent;
	private int offset;
	private int x;
	private int y;

	public Keybind(Button button, int offset) {
		this.parent = button;
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
		Gui.drawRect(left + 2, top, right, top + 14,
				binding ? 0xFF173C4A : (this.hovered ? 0xFF202C39 : 0xFF131A23));
		Gui.drawRect(left, top, left + 2, top + 14, 0xFF263241);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(binding ? "Press a key..." : "Keybind",
				left + 9, top + 3, binding ? 0xFF80D8E8 : 0xFFB8C4D0);
		String keyName = Keyboard.getKeyName(this.parent.mod.getKey());
		int keyWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(keyName);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(keyName,
				right - keyWidth - 5, top + 3, 0xFFE2E2E8);
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
			this.binding = !this.binding;
		}
	}

	@Override
	public void keyTyped(char typedChar, int key) {
		if (this.binding) {
			this.parent.mod.setKey(key);
			this.binding = false;
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		return x > this.x && x < this.x + parent.parent.getWidth() && y > this.y && y < this.y + 14;
	}
}
