package baseclient.gg.dhyeybg.baseclient.module.player;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.AutoLogin;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.gui.GuiBaseClientAutoLogin;

public class AutoLoginModule extends Module {

	private Setting command;

	public AutoLoginModule() {
		super("Auto Login", "Sends a configured login command when joining its server", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("/login");
		commands.add("/l");
		command = new Setting("Command", this, "/login", commands);
		manager.rSetting(command);
	}

	public String getCommand() {
		return command == null ? "/login" : command.getValString();
	}

	@Override
	public void onClickGuiEnable() {
		openConfigScreen();
	}

	public void openConfigScreen() {
		if (mc != null) {
			mc.displayGuiScreen(new GuiBaseClientAutoLogin(mc.currentScreen));
		}
	}

	@Override
	public void onDisable() {
		AutoLogin.disable();
	}
}
