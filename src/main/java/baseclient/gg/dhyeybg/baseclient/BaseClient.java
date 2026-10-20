package baseclient.gg.dhyeybg.baseclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import baseclient.gg.dhyeybg.baseclient.Clickgui.ClickGui;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.ModuleManager;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.module.combat.Criticals;
import baseclient.gg.dhyeybg.baseclient.module.combat.KillAura;
import baseclient.gg.dhyeybg.baseclient.module.combat.AutoCrystal;
import baseclient.gg.dhyeybg.baseclient.module.combat.AutoWeapon;
import baseclient.gg.dhyeybg.baseclient.module.misc.AutoGG;
import baseclient.gg.dhyeybg.baseclient.module.combat.BowRelease;
import baseclient.gg.dhyeybg.baseclient.module.combat.BowSilentAim;
import baseclient.gg.dhyeybg.baseclient.module.combat.Velocity;
import baseclient.gg.dhyeybg.baseclient.module.misc.Scaffold;
import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoMine;
import baseclient.gg.dhyeybg.baseclient.module.render.ESP;
import baseclient.gg.dhyeybg.baseclient.module.render.ClickGUI;
import baseclient.gg.dhyeybg.baseclient.module.render.FullBright;
import baseclient.gg.dhyeybg.baseclient.module.render.HUD;
import baseclient.gg.dhyeybg.baseclient.module.render.NoRender;
import baseclient.gg.dhyeybg.baseclient.module.render.TotemCounter;
import baseclient.gg.dhyeybg.baseclient.module.render.ChatTimestamps;
import baseclient.gg.dhyeybg.baseclient.module.render.CameraEffects;
import baseclient.gg.dhyeybg.baseclient.module.misc.SellCactus;
import baseclient.gg.dhyeybg.baseclient.module.misc.ChestLooter;
import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoBuilder;
import baseclient.gg.dhyeybg.baseclient.module.combat.Reach;
import baseclient.gg.dhyeybg.baseclient.module.combat.AimAssist;
import baseclient.gg.dhyeybg.baseclient.module.combat.AutoClicker;
import baseclient.gg.dhyeybg.baseclient.module.combat.TriggerBot;
import baseclient.gg.dhyeybg.baseclient.module.movement.InventoryMove;
import baseclient.gg.dhyeybg.baseclient.module.movement.Fly;
import baseclient.gg.dhyeybg.baseclient.module.movement.Speed;
import baseclient.gg.dhyeybg.baseclient.module.movement.HighJump;
import baseclient.gg.dhyeybg.baseclient.module.movement.LongJump;
import baseclient.gg.dhyeybg.baseclient.module.movement.Step;
import baseclient.gg.dhyeybg.baseclient.module.movement.NoFall;
import baseclient.gg.dhyeybg.baseclient.module.movement.AntiVoid;
import baseclient.gg.dhyeybg.baseclient.module.movement.Jesus;
import baseclient.gg.dhyeybg.baseclient.module.movement.NoSlow;
import baseclient.gg.dhyeybg.baseclient.module.movement.Spider;
import baseclient.gg.dhyeybg.baseclient.module.movement.SafeWalk;
import baseclient.gg.dhyeybg.baseclient.module.movement.Sprint;
import baseclient.gg.dhyeybg.baseclient.module.movement.Surround;
import baseclient.gg.dhyeybg.baseclient.module.movement.FastLadder;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoEat;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoArmor;
import baseclient.gg.dhyeybg.baseclient.module.player.InventoryManager;
import baseclient.gg.dhyeybg.baseclient.module.player.MiddleClickFriend;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoFish;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoSort;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoDrop;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoFarm;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoReplant;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoCraft;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoSmelt;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoReconnect;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoScreenshot;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoTool;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoTotem;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoConfig;
import baseclient.gg.dhyeybg.baseclient.module.player.AutoLoginModule;
import baseclient.gg.dhyeybg.baseclient.module.player.FastPlace;
import baseclient.gg.dhyeybg.baseclient.module.player.HotbarReplenish;
import baseclient.gg.dhyeybg.baseclient.module.player.LongPickupRange;
import baseclient.gg.dhyeybg.baseclient.module.player.PacketMine;
import baseclient.gg.dhyeybg.baseclient.module.player.Timer;
import baseclient.gg.dhyeybg.baseclient.module.utilities.Freecam;
import baseclient.gg.dhyeybg.baseclient.module.utilities.XRay;
import baseclient.gg.dhyeybg.baseclient.module.utilities.ElytraFly;
import baseclient.gg.dhyeybg.baseclient.module.utilities.ElytraBoost;
import baseclient.gg.dhyeybg.baseclient.module.utilities.BoatFly;
import baseclient.gg.dhyeybg.baseclient.module.utilities.Waypoints;
import baseclient.gg.dhyeybg.baseclient.module.utilities.TimeChanger;
import baseclient.gg.dhyeybg.baseclient.module.utilities.WeatherChanger;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import net.lax1dude.eaglercraft.internal.vfs2.VFile2;
import net.lax1dude.eaglercraft.internal.vfs2.EaglerFileSystemException;
import net.minecraft.client.Minecraft;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseClient {

	public static BaseClient instance;

	public static final String NAME = "DeSync Client";
	public static final String VERSION = "V1.1.5";

	public static final int CLICKGUI_KEY = 54;

	public ModuleManager moduleManager;
	public SettingsManager settingsManager;
	private final Map<Module, Boolean> pendingModuleStates = new HashMap<Module, Boolean>();
	private static final VFile2 CONFIG_FILE = new VFile2("baseclient-config.json");

	public void init() {
		settingsManager = new SettingsManager();
		moduleManager = new ModuleManager();
		((ESP) moduleManager.getModule("ESP")).registerSettings(settingsManager);
		((HUD) moduleManager.getModule("HUD")).registerSettings(settingsManager);
		((CameraEffects) moduleManager.getModule("Camera Effects")).registerSettings(settingsManager);
		((FullBright) moduleManager.getModule("FullBright")).registerSettings(settingsManager);
		((Criticals) moduleManager.getModule("Criticals")).registerSettings(settingsManager);
		((KillAura) moduleManager.getModule("KillAura")).registerSettings(settingsManager);
		((AutoCrystal) moduleManager.getModule("Auto Crystal")).registerSettings(settingsManager);
		((AutoWeapon) moduleManager.getModule("Auto Weapon")).registerSettings(settingsManager);
		((AutoGG) moduleManager.getModule("Auto GG")).registerSettings(settingsManager);
		((BowSilentAim) moduleManager.getModule("Bow Silent Aim")).registerSettings(settingsManager);
		((BowRelease) moduleManager.getModule("Bow Release")).registerSettings(settingsManager);
		((ElytraFly) moduleManager.getModule("Elytra Fly")).registerSettings(settingsManager);
		((ElytraBoost) moduleManager.getModule("Elytra Boost")).registerSettings(settingsManager);
		((BoatFly) moduleManager.getModule("Boat Fly")).registerSettings(settingsManager);
		((Surround) moduleManager.getModule("Surround")).registerSettings(settingsManager);
		((Scaffold) moduleManager.getModule("Scaffold")).registerSettings(settingsManager);
		((AutoBuilder) moduleManager.getModule("Auto Builder")).registerSettings(settingsManager);
		((AutoMine) moduleManager.getModule("Auto Mine")).registerSettings(settingsManager);
		((Sprint) moduleManager.getModule("Sprint")).registerSettings(settingsManager);
		((Velocity) moduleManager.getModule("Velocity")).registerSettings(settingsManager);
		((SafeWalk) moduleManager.getModule("Safe Walk")).registerSettings(settingsManager);
		((ClickGUI) moduleManager.getModule("ClickGUI")).registerSettings(settingsManager);
		((SellCactus) moduleManager.getModule("Sell Cactus")).registerSettings(settingsManager);
		((ChestLooter) moduleManager.getModule("Chest Looter")).registerSettings(settingsManager);
		((Reach) moduleManager.getModule("Reach")).registerSettings(settingsManager);
		((AimAssist) moduleManager.getModule("AimAssist")).registerSettings(settingsManager);
		((AutoClicker) moduleManager.getModule("AutoClicker")).registerSettings(settingsManager);
		((TriggerBot) moduleManager.getModule("TriggerBot")).registerSettings(settingsManager);
		((InventoryMove) moduleManager.getModule("Inventory Move")).registerSettings(settingsManager);
		((NoSlow) moduleManager.getModule("No Slow")).registerSettings(settingsManager);
		((Fly) moduleManager.getModule("Fly")).registerSettings(settingsManager);
		((Speed) moduleManager.getModule("Speed")).registerSettings(settingsManager);
		((HighJump) moduleManager.getModule("HighJump")).registerSettings(settingsManager);
		((LongJump) moduleManager.getModule("LongJump")).registerSettings(settingsManager);
		((Step) moduleManager.getModule("Step")).registerSettings(settingsManager);
		((NoFall) moduleManager.getModule("NoFall")).registerSettings(settingsManager);
		((AntiVoid) moduleManager.getModule("Anti Void")).registerSettings(settingsManager);
		((Jesus) moduleManager.getModule("Jesus")).registerSettings(settingsManager);
		((Spider) moduleManager.getModule("Spider")).registerSettings(settingsManager);
		((AutoTool) moduleManager.getModule("Auto Tool")).registerSettings(settingsManager);
		((AutoTotem) moduleManager.getModule("Auto Totem")).registerSettings(settingsManager);
		((AutoArmor) moduleManager.getModule("Auto Armor")).registerSettings(settingsManager);
		((InventoryManager) moduleManager.getModule("Inventory Manager")).registerSettings(settingsManager);
		((AutoSort) moduleManager.getModule("AutoSort")).registerSettings(settingsManager);
		((AutoDrop) moduleManager.getModule("AutoDrop")).registerSettings(settingsManager);
		((AutoFarm) moduleManager.getModule("AutoFarm")).registerSettings(settingsManager);
		((AutoCraft) moduleManager.getModule("AutoCraft")).registerSettings(settingsManager);
		((AutoReconnect) moduleManager.getModule("AutoReconnect")).registerSettings(settingsManager);
		((AutoScreenshot) moduleManager.getModule("AutoScreenshot")).registerSettings(settingsManager);
		((FastLadder) moduleManager.getModule("Fast Ladder")).registerSettings(settingsManager);
		((LongPickupRange) moduleManager.getModule("Long Pickup Range")).registerSettings(settingsManager);
		((AutoEat) moduleManager.getModule("Auto Eat")).registerSettings(settingsManager);
		((HotbarReplenish) moduleManager.getModule("Hotbar Replenish")).registerSettings(settingsManager);
		((AutoFish) moduleManager.getModule("Auto Fish")).registerSettings(settingsManager);
		((AutoLoginModule) moduleManager.getModule("Auto Login")).registerSettings(settingsManager);
		((AutoConfig) moduleManager.getModule("Auto Config")).registerSettings(settingsManager);
		((PacketMine) moduleManager.getModule("Packet Mine")).registerSettings(settingsManager);
		((FastPlace) moduleManager.getModule("FastPlace")).registerSettings(settingsManager);
		((Timer) moduleManager.getModule("Timer")).registerSettings(settingsManager);
		((XRay) moduleManager.getModule("XRay")).registerSettings(settingsManager);
		((Freecam) moduleManager.getModule("Freecam")).registerSettings(settingsManager);
		((TimeChanger) moduleManager.getModule("Time Changer")).registerSettings(settingsManager);
		((WeatherChanger) moduleManager.getModule("Weather Changer")).registerSettings(settingsManager);
		((NoRender) moduleManager.getModule("NoRender")).registerSettings(settingsManager);
		((TotemCounter) moduleManager.getModule("Totem Counter")).registerSettings(settingsManager);
		moduleManager.getModule("Auto Config").setToggled(true);
		loadConfig();
		System.out.println("[" + NAME + "] " + VERSION + " initialised");
	}

	public void loadConfig() {
		if (moduleManager == null || settingsManager == null) return;

		loadingConfig = true;
		try {
			if (!CONFIG_FILE.exists()) return;
			JSONObject config = new JSONObject(CONFIG_FILE.getAllChars());
			JSONArray modules = config.optJSONArray("modules");
			if (modules != null) {
				for (int i = 0; i < modules.length(); ++i) {
					JSONObject savedModule = modules.optJSONObject(i);
					if (savedModule == null) continue;
					Module module = moduleManager.getModule(savedModule.optString("name", ""));
					if (module == null) continue;

					module.setKey(savedModule.optInt("key", module.getKey()));
					JSONObject savedSettings = savedModule.optJSONObject("settings");
					if (savedSettings != null) {
						ArrayList<Setting> moduleSettings = settingsManager.getSettingsByMod(module);
						if (moduleSettings != null) {
							for (Setting setting : moduleSettings) {
								if (!savedSettings.has(setting.getName())) continue;
								if (setting.isCheck()) {
									setting.setValBoolean(savedSettings.optBoolean(setting.getName(), setting.getValBoolean()));
								} else if (setting.isSlider() || setting.isNumber()) {
									double value = savedSettings.optDouble(setting.getName(), Double.NaN);
									if (!Double.isNaN(value)) setting.setValDouble(value);
								} else if (setting.isCombo()) {
									String value = savedSettings.optString(setting.getName(), setting.getValString());
									if (setting.getOptions() != null && setting.getOptions().contains(value)) {
										setting.setValString(value);
									}
								}
							}
						}
					}

					boolean enabled = savedModule.optBoolean("enabled", false);
					if (enabled && !SingleplayerWorld.isModuleAllowed(module)) {
						pendingModuleStates.put(module, true);
					} else {
						module.setToggled(enabled);
					}
				}
			}

			JSONObject autoLogin = config.optJSONObject("autoLogin");
			if (autoLogin != null) {
				baseclient.gg.dhyeybg.baseclient.AutoLogin.restoreConfig(
						autoLogin.optString("server", ""), autoLogin.optBoolean("enabled", false));
			}
			Waypoints.deserialize(config.optJSONArray("waypoints"));
			MiddleClickFriend.deserialize(config.optJSONArray("friends"));
		} catch (JSONException e) {
			System.err.println("[BaseClient] Could not parse config: " + e.getMessage());
		} catch (EaglerFileSystemException e) {
			System.err.println("[BaseClient] Could not read config: " + e.getMessage());
		} finally {
			loadingConfig = false;
		}
	}

	private boolean loadingConfig;

	public boolean isLoadingConfig() {
		return loadingConfig;
	}

	public void notifyModuleToggle(Module module, boolean enabled) {
		if (loadingConfig || moduleManager == null || Minecraft.getMinecraft().player == null) {
			return;
		}
		Module hud = moduleManager.getModule("HUD");
		if (hud instanceof HUD && ((HUD) hud).shouldShowToggleNotifications()) {
			Minecraft.getMinecraft().player.addChatMessage(new net.minecraft.util.text.TextComponentString(
					module.getName() + (enabled ? " enabled" : " disabled")));
		}
	}

	public boolean shouldSaveOnDisconnect() {
		if (moduleManager == null) return false;
		Module autoConfig = moduleManager.getModule("Auto Config");
		return autoConfig instanceof AutoConfig && ((AutoConfig) autoConfig).shouldSaveOnDisconnect();
	}

	public void onWorldLoaded() {
		Module reconnect = moduleManager == null ? null : moduleManager.getModule("AutoReconnect");
		if (reconnect instanceof AutoReconnect) {
			((AutoReconnect) reconnect).resetAttempts();
		}
		for (Map.Entry<Module, Boolean> entry : pendingModuleStates.entrySet()) {
			Module module = entry.getKey();
			boolean enabled = entry.getValue();
			if (enabled && SingleplayerWorld.isModuleAllowed(module)) {
				module.setToggled(true);
			}
		}
		pendingModuleStates.clear();
	}

	public void saveConfig() {
		if (moduleManager == null || settingsManager == null) return;

		try {
			JSONArray modules = new JSONArray();
			for (Module module : moduleManager.getModuleList()) {
				JSONObject savedModule = new JSONObject();
				savedModule.put("name", module.getName());
				Module autoConfigModule = moduleManager.getModule("Auto Config");
				boolean restoreModuleStates = !(autoConfigModule instanceof AutoConfig)
						|| ((AutoConfig) autoConfigModule).shouldRestoreModuleStates();
				boolean enabled = module.isToggled() || Boolean.TRUE.equals(pendingModuleStates.get(module));
				savedModule.put("enabled", module == autoConfigModule || restoreModuleStates && enabled);
				savedModule.put("key", module.getKey());

				JSONObject savedSettings = new JSONObject();
				ArrayList<Setting> moduleSettings = settingsManager.getSettingsByMod(module);
				if (moduleSettings != null) {
					for (Setting setting : moduleSettings) {
						if (setting.isCheck()) {
							savedSettings.put(setting.getName(), setting.getValBoolean());
						} else if (setting.isSlider() || setting.isNumber()) {
							savedSettings.put(setting.getName(), setting.getValDouble());
						} else if (setting.isCombo() && setting.getValString() != null) {
							savedSettings.put(setting.getName(), setting.getValString());
						}
					}
				}
				savedModule.put("settings", savedSettings);
				modules.put(savedModule);
			}

			JSONObject autoLogin = new JSONObject();
			autoLogin.put("server", AutoLogin.getServer());
			autoLogin.put("enabled", AutoLogin.isEnabled());

			JSONObject config = new JSONObject();
			config.put("modules", modules);
			config.put("autoLogin", autoLogin);
			config.put("waypoints", Waypoints.serialize());
			config.put("friends", MiddleClickFriend.serialize());
			CONFIG_FILE.setAllChars(config.toString());
		} catch (JSONException e) {
			System.err.println("[BaseClient] Could not save config: " + e.getMessage());
		} catch (EaglerFileSystemException e) {
			System.err.println("[BaseClient] Could not write config: " + e.getMessage());
		}
	}

	public boolean isModuleEnabled(String name) {
		if (moduleManager == null) return false;
		Module module = moduleManager.getModule(name);
		return module != null && module.isToggled() && SingleplayerWorld.isModuleAllowed(module);
	}

	public boolean isModuleToggled(String name) {
		if (moduleManager == null) return false;
		Module module = moduleManager.getModule(name);
		return module != null && module.isToggled();
	}

	public void onUpdate() {
		if (moduleManager == null) return;
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.world == null || minecraft.player == null) return;
		Module hudModule = moduleManager.getModule("HUD");
		if (hudModule instanceof HUD) {
			((HUD) hudModule).updateLayoutEditor();
		}
		for (Module m : moduleManager.getModuleList()) {
			if (!m.isToggled()) continue;
			if (!SingleplayerWorld.isModuleAllowed(m)) {
				m.setToggled(false);
			} else {
				m.onUpdate();
			}
		}
	}

	public void onKey(int key) {
		if (moduleManager == null) return;
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen != null) return;

		if (key == CLICKGUI_KEY) {
			Module clickGui = moduleManager.getModule("ClickGUI");
			if (clickGui == null || !SingleplayerWorld.isModuleAllowed(clickGui)) {
				SingleplayerWorld.notifyBlocked("ClickGUI");
				return;
			}
			mc.displayGuiScreen(new ClickGui());
			return;
		}
		for (Module m : moduleManager.getModuleList()) {
			if (m.isToggled() && m instanceof AutoBuilder && ((AutoBuilder) m).handleSelectionKey(key)) {
				return;
			}
		}
		for (Module m : moduleManager.getModuleList()) {
			if (m.getKey() != 0 && m.getKey() == key) m.toggle();
		}
	}

	public void onRender2D() {
		if (moduleManager == null) return;
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || mc.world == null || mc.player == null) return;

		for (Module m : moduleManager.getModuleList()) {
			if (m.isToggled()) {
				if (!SingleplayerWorld.isModuleAllowed(m)) {
					m.setToggled(false);
					continue;
				}
				m.onRender2D();
			}
		}
	}

	public void onRender3D(float partialTicks) {
		if (moduleManager == null) return;
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.world == null || minecraft.player == null) return;
		for (Module module : moduleManager.getModuleList()) {
			if (!module.isToggled()) continue;
			if (!SingleplayerWorld.isModuleAllowed(module)) {
				module.setToggled(false);
			} else {
				module.onRender3D(partialTicks);
			}
		}
	}

	public boolean onVelocity(int entityId) {
		if (moduleManager == null) return false;
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null || mc.player.getEntityId() != entityId) return false;
		boolean cancel = false;
		for (Module m : moduleManager.getModuleList()) {
			if (!m.isToggled()) continue;
			if (!SingleplayerWorld.isModuleAllowed(m)) {
				m.setToggled(false);
			} else if (m.onVelocity(entityId)) {
				cancel = true;
			}
		}
		return cancel;
	}
}
