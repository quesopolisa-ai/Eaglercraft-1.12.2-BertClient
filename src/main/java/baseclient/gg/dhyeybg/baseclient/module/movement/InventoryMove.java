package baseclient.gg.dhyeybg.baseclient.module.movement;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.lax1dude.eaglercraft.Keyboard;

public class InventoryMove extends Module {
	private Setting enableSneak;

	public InventoryMove() {
		super("Inventory Move", "Allows movement while an inventory screen is open", Category.MOVEMENT);
	}

	public void registerSettings(SettingsManager manager) {
		enableSneak = new Setting("Sneak", this, true);
		manager.rSetting(enableSneak);
	}

	public boolean isKeyDown(KeyBinding binding) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || !(minecraft.currentScreen instanceof GuiContainer)
				|| !isToggled() || !baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld.isModuleAllowed(this)) {
			return binding.isKeyDown();
		}
		if (binding == minecraft.gameSettings.keyBindSneak && (enableSneak == null || !enableSneak.getValBoolean())) {
			return false;
		}
		int keyCode = binding.getKeyCode();
		return keyCode >= 0 && Keyboard.isKeyDown(keyCode);
	}
}
