package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class AutoTotem extends Module {
	private Setting healthThreshold;

	public AutoTotem() {
		super("Auto Totem", "Moves a totem into the offhand when available", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		healthThreshold = new Setting("Health Threshold", this, 20.0D, 1.0D, 20.0D, false);
		manager.rSetting(healthThreshold);
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.player.openContainer != mc.player.inventoryContainer
				|| !mc.player.inventory.getItemStack().func_190926_b()) return;
		if (mc.player.getHealth() > (healthThreshold == null ? 20.0D : healthThreshold.getValDouble())) return;
		if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) return;

		for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
			ItemStack stack = mc.player.inventory.mainInventory.get(i);
			if (stack.getItem() != Items.TOTEM_OF_UNDYING) continue;
			int containerSlot = i < 9 ? i + 36 : i;
			PlayerControllerMP controller = mc.playerController;
			controller.windowClick(mc.player.inventoryContainer.windowId, containerSlot, 0, ClickType.PICKUP, mc.player);
			controller.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
			if (!mc.player.inventory.getItemStack().func_190926_b()) {
				controller.windowClick(mc.player.inventoryContainer.windowId, containerSlot, 0, ClickType.PICKUP, mc.player);
			}
			return;
		}
	}
}
