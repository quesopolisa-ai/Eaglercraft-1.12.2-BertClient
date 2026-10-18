package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AutoEat extends Module {
	private Setting hunger;
	private int swappedInventorySlot = -1;
	private int swappedHotbarSlot = -1;

	public AutoEat() {
		super("Auto Eat", "Consumes food when hunger is low", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		hunger = new Setting("Hunger", this, 14.0D, 1.0D, 20.0D, true);
		manager.rSetting(hunger);
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.world == null || mc.playerController == null) return;
		if (swappedInventorySlot >= 0 && !mc.player.isHandActive()) {
			restoreSwap();
		}
		if (mc.currentScreen != null || mc.player.openContainer != mc.player.inventoryContainer
				|| mc.player.getFoodStats().getFoodLevel() >= getHungerThreshold()
				|| mc.player.isHandActive() || swappedInventorySlot >= 0) return;

		int hotbarSlot = -1;
		int inventorySlot = -1;
		int bestFoodValue = -1;
		for (int i = 0; i < 9; i++) {
			ItemStack stack = mc.player.inventory.getStackInSlot(i);
			if (stack.getItem() instanceof ItemFood && getFoodValue(stack) > bestFoodValue) {
				hotbarSlot = i;
				bestFoodValue = getFoodValue(stack);
			}
		}
		if (hotbarSlot == -1) {
			for (int i = 9; i < 36; i++) {
				ItemStack stack = mc.player.inventory.getStackInSlot(i);
				if (stack.getItem() instanceof ItemFood && getFoodValue(stack) > bestFoodValue) {
					inventorySlot = i;
					bestFoodValue = getFoodValue(stack);
				}
			}
		}
		if (hotbarSlot < 0) {
			hotbarSlot = mc.player.inventory.currentItem;
			if (inventorySlot < 0) {
				for (int i = 9; i < 36; i++) {
					ItemStack stack = mc.player.inventory.getStackInSlot(i);
					if (stack.getItem() instanceof ItemFood && getFoodValue(stack) > bestFoodValue) {
						inventorySlot = i;
						bestFoodValue = getFoodValue(stack);
					}
				}
			}
			if (inventorySlot >= 0) {
				mc.playerController.windowClick(mc.player.inventoryContainer.windowId, inventorySlot, hotbarSlot,
						net.minecraft.inventory.ClickType.SWAP, mc.player);
				swappedInventorySlot = inventorySlot;
				swappedHotbarSlot = hotbarSlot;
			}
		}
		if (hotbarSlot < 0 || bestFoodValue < 0) return;
		mc.player.inventory.currentItem = hotbarSlot;
		mc.playerController.updateController();
		mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
	}

	@Override
	public void onDisable() {
		restoreSwap();
	}

	private int getFoodValue(ItemStack stack) {
		ItemFood food = (ItemFood) stack.getItem();
		return food.getHealAmount(stack) * 100 + (int) (food.getSaturationModifier(stack) * 100.0F);
	}

	private void restoreSwap() {
		if (swappedInventorySlot >= 0 && mc.player != null && mc.playerController != null
				&& mc.player.openContainer == mc.player.inventoryContainer) {
			mc.playerController.windowClick(mc.player.inventoryContainer.windowId, swappedInventorySlot,
					swappedHotbarSlot, net.minecraft.inventory.ClickType.SWAP, mc.player);
		}
		swappedInventorySlot = -1;
		swappedHotbarSlot = -1;
	}

	private int getHungerThreshold() {
		return hunger == null ? 14 : (int) hunger.getValDouble();
	}
}
