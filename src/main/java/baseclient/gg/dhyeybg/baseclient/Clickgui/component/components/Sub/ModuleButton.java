package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class ModuleButton extends Component {

	private boolean hovered;
	private Button parent;
	private Setting set;
	private int offset;
	private int x;
	private int y;
	private Module mod;

	private int modeIndex;

	public ModuleButton(Setting set, Button button, Module mod, int offset) {
		this.set = set;
		this.parent = button;
		this.mod = mod;
		this.x = button.parent.getX() + button.parent.getWidth();
		this.y = button.parent.getY() + button.offset;
		this.offset = offset;
		this.modeIndex = Math.max(0, set.getOptions().indexOf(set.getValString()));
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
		String value = "< " + set.getValString() + " >";
		int valueWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value);
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.75F, 0.75F, 1.0F);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(set.getName(),
				(int) ((left + 7) / 0.75F), (int) ((top + 3) / 0.75F), 0xFFB8C4D0);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(value,
				(int) ((right - valueWidth * 0.75F - 5) / 0.75F),
				(int) ((top + 3) / 0.75F), 0xFF80D8E8);
		GlStateManager.popMatrix();
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
			int size = set.getOptions().size();
			if (size == 0) return;
			modeIndex = (modeIndex + 1) % size;
			set.setValString(set.getOptions().get(modeIndex));
		}
	}

	public boolean isMouseOnButton(int x, int y) {
		return x > this.x && x < this.x + parent.parent.getWidth() && y > this.y && y < this.y + 14;
	}
}
