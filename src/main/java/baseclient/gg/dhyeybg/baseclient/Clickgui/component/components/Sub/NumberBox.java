package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class NumberBox extends Component {

	private static final int KEY_BACKSPACE = 14;
	private static final int KEY_ENTER = 28;
	private static final int KEY_NUMPAD_ENTER = 156;
	private static final int MAX_CHARS = 6;

	private boolean hovered;
	private boolean focused;
	private final Setting set;
	private final Button parent;
	private int offset;
	private int x;
	private int y;
	private String text;

	public NumberBox(Setting setting, Button button, int offset) {
		this.set = setting;
		this.parent = button;
		this.offset = offset;
		this.x = button.parent.getX();
		this.y = button.parent.getY() + offset;
		this.text = format(setting.getValDouble());
	}

	private static String format(double value) {
		if (value == Math.floor(value)) {
			return Long.toString((long) value);
		}
		return Double.toString(Math.round(value * 100.0D) / 100.0D);
	}

	@Override
	public void renderComponent() {
		int left = parent.parent.getX();
		int top = parent.parent.getY() + offset;
		int right = left + parent.parent.getWidth();
		int background = this.focused ? 0xFF173C4A : (this.hovered ? 0xFF202C39 : 0xFF131A23);
		Gui.drawRect(left + 2, top, right, top + 14, background);
		Gui.drawRect(left, top, left + 2, top + 14, 0xFF263241);
		String shown;
		if (this.focused) {
			shown = this.text + ((System.currentTimeMillis() / 500L) % 2L == 0L ? "_" : "");
		} else {
			shown = format(this.set.getValDouble());
		}
		String label = this.set.getName();
		int valueWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(shown);
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.75F, 0.75F, 1.0F);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(label,
				(int) ((left + 6) / 0.75F), (int) ((top + 2) / 0.75F), 0xFFB8C4D0);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(shown,
				(int) ((right - valueWidth - 6) / 0.75F), (int) ((top + 2) / 0.75F),
				this.focused ? 0xFF80D8E8 : 0xFFE2EAF2);
		GlStateManager.popMatrix();
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
			this.focused = true;
			this.text = format(this.set.getValDouble());
		} else {
			this.focused = false;
		}
	}

	@Override
	public void keyTyped(char typedChar, int key) {
		if (!this.focused) {
			return;
		}
		if (key == KEY_ENTER || key == KEY_NUMPAD_ENTER) {
			this.focused = false;
			return;
		}
		if (key == KEY_BACKSPACE) {
			if (this.text.length() > 0) {
				this.text = this.text.substring(0, this.text.length() - 1);
			}
		} else if (this.text.length() < MAX_CHARS
				&& ((typedChar >= '0' && typedChar <= '9') || (typedChar == '.' && this.text.indexOf('.') < 0))) {
			this.text = this.text + typedChar;
		} else {
			return;
		}
		applyText();
	}

	private void applyText() {
		if (this.text.length() == 0 || ".".equals(this.text)) {
			return;
		}
		try {
			this.set.setValDouble(Double.parseDouble(this.text));
		} catch (NumberFormatException ignored) {
			// partial input such as "1." is parsed on the next keystroke
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		return x > this.x && x < this.x + parent.parent.getWidth() && y > this.y && y < this.y + 14;
	}
}
