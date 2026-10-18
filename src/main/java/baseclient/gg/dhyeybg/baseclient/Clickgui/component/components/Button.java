package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Frame;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub.Checkbox;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub.Keybind;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub.ModuleButton;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub.NumberBox;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub.VisibleButton;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Button extends Component {

	private static final int ROW_HEIGHT = 14;
	private static final int ACCENT = 0xFFBD62FF;
	private static final int HOVER_BG = 0xFF292331;
	private static final int TOGGLED_BG = 0xFF21182B;
	private static final int IDLE_BG = 0xFF15151C;

	public Module mod;
	public Frame parent;
	public int offset;
	private boolean isHovered;
	private ArrayList<Component> subcomponents;
	public boolean open;

	public Button(Module mod, Frame parent, int offset) {
		this.mod = mod;
		this.parent = parent;
		this.offset = offset;
		this.subcomponents = new ArrayList<Component>();
		this.open = false;
		int opY = offset + ROW_HEIGHT;
		ArrayList<Setting> settings = BaseClient.instance.settingsManager.getSettingsByMod(mod);
		if (settings != null) {
			for (Setting s : settings) {
				if (s.isCombo()) {
					this.subcomponents.add(new ModuleButton(s, this, mod, opY));
					opY += ROW_HEIGHT;
				}
				if (s.isSlider() || s.isNumber()) {
					this.subcomponents.add(new NumberBox(s, this, opY));
					opY += ROW_HEIGHT;
				}
				if (s.isCheck()) {
					this.subcomponents.add(new Checkbox(s, this, opY));
					opY += ROW_HEIGHT;
				}
			}
		}
		this.subcomponents.add(new Keybind(this, opY));
		opY += ROW_HEIGHT;
		this.subcomponents.add(new VisibleButton(this, mod, opY));
	}

	@Override
	public void setOff(int newOff) {
		offset = newOff;
		int opY = offset + ROW_HEIGHT;
		for (Component comp : this.subcomponents) {
			comp.setOff(opY);
			opY += ROW_HEIGHT;
		}
	}

	@Override
	public void renderComponent() {
		int bg;
		if (this.isHovered) {
			bg = HOVER_BG;
		} else {
			bg = this.mod.isToggled() ? TOGGLED_BG : IDLE_BG;
		}
		Gui.drawRect(parent.getX(), this.parent.getY() + this.offset,
				parent.getX() + parent.getWidth(), this.parent.getY() + ROW_HEIGHT + this.offset, bg);
		Gui.drawRect(parent.getX(), this.parent.getY() + this.offset,
				parent.getX() + 2, this.parent.getY() + ROW_HEIGHT + this.offset,
				this.mod.isToggled() ? ACCENT : 0xFF373741);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.mod.getName(),
				parent.getX() + 7, parent.getY() + offset + 3, this.mod.isToggled() ? ACCENT : 0xFFE2E2E8);
		if (this.subcomponents.size() > 2) {
			Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.open ? "-" : "+",
					parent.getX() + parent.getWidth() - 11, parent.getY() + offset + 3, 0xFFAAAAAF);
		}
		if (this.open && !this.subcomponents.isEmpty()) {
			for (Component comp : this.subcomponents) {
				comp.renderComponent();
			}
			Gui.drawRect(parent.getX() + 2, parent.getY() + this.offset + ROW_HEIGHT,
					parent.getX() + 3,
					parent.getY() + this.offset + ((this.subcomponents.size() + 1) * ROW_HEIGHT), ACCENT);
		}
	}

	@Override
	public int getHeight() {
		if (this.open) {
			return ROW_HEIGHT * (this.subcomponents.size() + 1);
		}
		return ROW_HEIGHT;
	}

	@Override
	public void updateComponent(int mouseX, int mouseY) {
		this.isHovered = isMouseOnButton(mouseX, mouseY);
		for (Component comp : this.subcomponents) {
			comp.updateComponent(mouseX, mouseY);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (isMouseOnButton(mouseX, mouseY) && button == 0 && !isWithinSettings(mouseX, mouseY)) {
			this.mod.toggle();
			if (this.mod.isToggled()) {
				this.mod.onClickGuiEnable();
			}
		}
		if (isMouseOnButton(mouseX, mouseY) && button == 1) {
			this.open = !this.open;
			this.parent.refresh();
		}
		for (Component comp : this.subcomponents) {
			comp.mouseClicked(mouseX, mouseY, button);
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (Component comp : this.subcomponents) {
			comp.mouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void keyTyped(char typedChar, int key) {
		for (Component comp : this.subcomponents) {
			comp.keyTyped(typedChar, key);
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		return x > parent.getX() && x < parent.getX() + parent.getWidth()
				&& y > this.parent.getY() + this.offset && y < this.parent.getY() + ROW_HEIGHT + this.offset;
	}

	private boolean isWithinSettings(int mouseX, int mouseY) {
		return this.open && mouseY >= this.parent.getY() + this.offset + ROW_HEIGHT
				&& mouseY < this.parent.getY() + this.offset + getHeight();
	}
}
