package baseclient.gg.dhyeybg.baseclient;

import java.util.Locale;

import baseclient.gg.dhyeybg.baseclient.module.player.AutoLoginModule;
import net.minecraft.client.Minecraft;

public final class AutoLogin {

	private static String server = "";
	private static String password = "";
	private static boolean enabled;

	private AutoLogin() {
	}

	public static String getServer() {
		return server;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void configure(String serverAddress, String serverPassword) {
		if (serverAddress == null || serverAddress.trim().isEmpty()) {
			throw new IllegalArgumentException("Server address cannot be empty");
		}
		if (serverPassword == null || serverPassword.isEmpty()) {
			throw new IllegalArgumentException("Password cannot be empty");
		}
		if (serverPassword.length() > 128 || serverPassword.indexOf('\n') >= 0
				|| serverPassword.indexOf('\r') >= 0) {
			throw new IllegalArgumentException("Password must be 1-128 characters on one line");
		}

		server = serverAddress.trim();
		password = serverPassword;
		enabled = true;
	}

	public static void restoreConfig(String serverAddress, boolean enabledIn) {
		server = serverAddress == null ? "" : serverAddress.trim();
		enabled = enabledIn && !server.isEmpty();
		password = "";
	}

	public static void disable() {
		enabled = false;
	}

	public static void clearPassword() {
		password = "";
		enabled = false;
	}

	public static void onServerJoin(String serverAddress) {
		if (!enabled || password.isEmpty() || serverAddress == null
				|| !server.toLowerCase(Locale.ROOT).equals(serverAddress.trim().toLowerCase(Locale.ROOT))) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && mc.player.connection != null) {
			String command = "/login";
			if (BaseClient.instance != null && BaseClient.instance.moduleManager != null) {
				AutoLoginModule module = (AutoLoginModule) BaseClient.instance.moduleManager.getModule("Auto Login");
				if (module == null || !module.isToggled()) return;
				command = module.getCommand();
			}
			mc.player.sendChatMessage(command + " " + password);
		}
	}
}
