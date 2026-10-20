package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Checkbox extends Component {

	private boolean hovered;
	private Setting op;
	private Button parent;
	private int offset;
	private int x;
	private int y;

	public Checkbox(Setting option, Button button, int offset) {
		this.op = option;
		this.parent = button;
		this.x = button.parent.getX() + button.parent.getWidth();
		this.y = button.parent.getY() + button.offset;
		this.offset = offset;
	}

	@Override
	public void renderComponent() {
		int left = parent.parent.getX();
		int top = parent.parent.getY() + offset;
		int right = left + parent.parent.getWidth();
		Gui.drawRect(left + 2, top, right, top + 14, this.hovered ? 0xFF202C39 : 0xFF131A23);
		Gui.drawRect(left, top, left + 2, top + 14, 0xFF263241);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.op.getName(), left + 9, top + 3, 0xFFE2EAF2);
		int boxLeft = right - 17;
		Gui.drawRect(boxLeft, top + 4, boxLeft + 9, top + 13, 0xFF777780);
		if (this.op.getValBoolean()) {
			Gui.drawRect(boxLeft + 2, top + 6, boxLeft + 7, top + 11, 0xFF5DC8E8);
		}
	}

	@Override
	public void setOff(int newOff) {
		offset = newOff;
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
			this.op.setValBoolean(!op.getValBoolean());
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		return x > this.x && x < this.x + parent.parent.getWidth() && y > this.y && y < this.y + 14;
	}
}
