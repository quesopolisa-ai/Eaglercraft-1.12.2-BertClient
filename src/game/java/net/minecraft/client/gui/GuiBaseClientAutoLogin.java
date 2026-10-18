package net.minecraft.client.gui;

import java.io.IOException;

import baseclient.gg.dhyeybg.baseclient.AutoLogin;
import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoLoginModule;
import net.minecraft.client.multiplayer.ServerData;

public class GuiBaseClientAutoLogin extends GuiScreen {

	private final GuiScreen parent;
	private GuiTextField serverField;
	private GuiTextField passwordField;
	private String statusMessage = "";

	public GuiBaseClientAutoLogin(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		int center = this.width / 2;
		this.serverField = new GuiTextField(0, this.fontRendererObj, center - 100, this.height / 2 - 39, 200, 20);
		this.serverField.setMaxStringLength(255);
		String currentServer = "";
		ServerData serverData = this.mc.getCurrentServerData();
		if (serverData != null) {
			currentServer = serverData.serverIP;
		} else {
			currentServer = AutoLogin.getServer();
		}
		this.serverField.setText(currentServer);

		this.passwordField = new GuiTextField(1, this.fontRendererObj, center - 100, this.height / 2 + 1, 200, 20);
		this.passwordField.setMaxStringLength(128);
		this.passwordField.setPasswordField(true);
		this.passwordField.setFocused(true);

		this.buttonList.add(new GuiButton(0, center - 100, this.height / 2 + 34, 200, 20, "Save + Enable"));
		this.buttonList.add(new GuiButton(1, center - 100, this.height / 2 + 58, 98, 20, "Disable"));
		this.buttonList.add(new GuiButton(2, center + 2, this.height / 2 + 58, 98, 20, "Clear Password"));
		this.buttonList.add(new GuiButton(3, center - 100, this.height / 2 + 88, 200, 20, "Done"));
		if (AutoLogin.isEnabled()) {
			this.statusMessage = "Enter the password again; it is not stored between sessions.";
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			try {
				AutoLogin.configure(this.serverField.getText(), this.passwordField.getText());
				AutoLoginModule module = (AutoLoginModule) BaseClient.instance.moduleManager.getModule("Auto Login");
				if (module != null && !module.isToggled()) {
					module.setToggled(true);
				}
				this.passwordField.setText("");
				this.statusMessage = "Enabled for this server. Password is only kept until the client closes.";
				BaseClient.instance.saveConfig();
			} catch (IllegalArgumentException e) {
				this.statusMessage = e.getMessage();
			}
		} else if (button.id == 1) {
			AutoLogin.disable();
			AutoLoginModule module = (AutoLoginModule) BaseClient.instance.moduleManager.getModule("Auto Login");
			if (module != null && module.isToggled()) {
				module.setToggled(false);
			}
			BaseClient.instance.saveConfig();
			this.statusMessage = "Auto Login disabled.";
		} else if (button.id == 2) {
			AutoLogin.clearPassword();
			this.passwordField.setText("");
			BaseClient.instance.saveConfig();
			this.statusMessage = "Password cleared from memory.";
		} else if (button.id == 3) {
			BaseClient.instance.saveConfig();
			this.mc.displayGuiScreen(this.parent);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			BaseClient.instance.saveConfig();
			this.mc.displayGuiScreen(this.parent);
			return;
		}
		if (keyCode == 28 && this.passwordField.isFocused()) {
			this.actionPerformed(this.buttonList.get(0));
			return;
		}
		this.serverField.textboxKeyTyped(typedChar, keyCode);
		this.passwordField.textboxKeyTyped(typedChar, keyCode);
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.serverField.mouseClicked(mouseX, mouseY, mouseButton);
		this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void updateScreen() {
		this.serverField.updateCursorCounter();
		this.passwordField.updateCursorCounter();
	}

	@Override
	public void onGuiClosed() {
		if (this.passwordField != null) {
			this.passwordField.setText("");
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, "Auto Login", this.width / 2, this.height / 2 - 75, 0xFFFFFF);
		this.drawString(this.fontRendererObj, "Server address", this.width / 2 - 100, this.height / 2 - 51, 0xA0A0A0);
		this.drawString(this.fontRendererObj, "Password (/login <password>)", this.width / 2 - 100,
				this.height / 2 - 11, 0xA0A0A0);
		this.serverField.drawTextBox();
		this.passwordField.drawTextBox();
		this.drawCenteredString(this.fontRendererObj, "Password is memory-only; enter it again after restarting.",
				this.width / 2, this.height - 10, 0xAAAAAA);
		if (!this.statusMessage.isEmpty()) {
			this.drawCenteredString(this.fontRendererObj, this.statusMessage, this.width / 2,
					this.height / 2 + 23, 0xFFFFFF);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
