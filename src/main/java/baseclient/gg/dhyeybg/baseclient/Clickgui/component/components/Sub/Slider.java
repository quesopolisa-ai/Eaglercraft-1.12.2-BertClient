package baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Sub;

import baseclient.gg.dhyeybg.baseclient.Clickgui.component.Component;
import baseclient.gg.dhyeybg.baseclient.Clickgui.component.components.Button;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Slider extends Component {

	private boolean hovered;

	private Setting set;
	private Button parent;
	private int offset;
	private int x;
	private int y;
	private boolean dragging = false;

	private double renderWidth;

	public Slider(Setting value, Button button, int offset) {
		this.set = value;
		this.parent = button;
		this.x = button.parent.getX() + button.parent.getWidth();
		this.y = button.parent.getY() + button.offset;
		this.offset = offset;
	}

	@Override
	public void renderComponent() {
		int left = parent.parent.getX();
		int top = parent.parent.getY() + offset;
		int width = parent.parent.getWidth();
		Gui.drawRect(left + 2, top, left + width, top + 14, this.hovered ? 0xFF292331 : 0xFF15151C);
		Gui.drawRect(left + 2, top + 11, left + width, top + 14, 0xFF34343D);
		Gui.drawRect(left + 2, top + 11, left + (int) renderWidth, top + 14, 0xFFBD62FF);
		Gui.drawRect(left, top, left + 2, top + 14, 0xFF202027);
		String value = formatValue();
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.75F, 0.75F, 1.0F);
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.set.getName() + ": " + value,
				(int) ((left + 6) / 0.75F), (int) ((top + 2) / 0.75F), 0xFFE2E2E8);
		GlStateManager.popMatrix();
	}

	@Override
	public void setOff(int newOff) {
		offset = newOff;
	}

	@Override
	public void updateComponent(int mouseX, int mouseY) {
		this.hovered = isMouseOnButtonD(mouseX, mouseY) || isMouseOnButtonI(mouseX, mouseY);
		this.y = parent.parent.getY() + offset;
		this.x = parent.parent.getX();

		double diff = Math.min(parent.parent.getWidth() - 2, Math.max(0, mouseX - this.x));

		double min = set.getMin();
		double max = set.getMax();

		renderWidth = 2 + (parent.parent.getWidth() - 2) * (set.getValDouble() - min) / (max - min);

		if (dragging) {
			if (diff == 0) {
				set.setValDouble(set.getMin());
			} else {
				set.setValDouble(roundToPlace((diff / (parent.parent.getWidth() - 2)) * (max - min) + min, 2));
			}
		}
	}

	private static double roundToPlace(double value, int places) {
		double scale = Math.pow(10, places);
		return Math.round(value * scale) / scale;
	}

	private String formatValue() {
		double value = this.set.getValDouble();
		if (this.set.onlyInt()) {
			return Integer.toString((int) value);
		}
		return Double.toString(Math.round(value * 100.0D) / 100.0D);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (button == 0 && this.parent.open && (isMouseOnButtonD(mouseX, mouseY) || isMouseOnButtonI(mouseX, mouseY))) {
			dragging = true;
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		dragging = false;
	}

	public boolean isMouseOnButtonD(int x, int y) {
		return x > this.x && x < this.x + parent.parent.getWidth() && y > this.y && y < this.y + 14;
	}

	public boolean isMouseOnButtonI(int x, int y) {
		return x > this.x + parent.parent.getWidth() / 2 && x < this.x + parent.parent.getWidth()
				&& y > this.y && y < this.y + 14;
	}
}
