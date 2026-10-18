package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class HotbarReplenish extends Module {
    private Setting threshold;
    private Setting delay;
    private long nextActionTime;
    private final ItemStack[] previousHotbarStacks = new ItemStack[9];

    public HotbarReplenish() {
        super("Hotbar Replenish", "Replaces low hotbar stacks with matching inventory stacks", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        threshold = new Setting("Replenish Below", this, 16.0D, 1.0D, 63.0D, true);
        delay = new Setting("Delay MS", this, 250.0D, 0.0D, 2000.0D, true);
        manager.rSetting(threshold);
        manager.rSetting(delay);
    }

    @Override
    public void onEnable() {
        nextActionTime = 0L;
        for (int i = 0; i < previousHotbarStacks.length; i++) {
            ItemStack stack = mc == null || mc.player == null ? null : mc.player.inventory.getStackInSlot(i);
            previousHotbarStacks[i] = stack == null || stack.func_190926_b() ? null : stack.copy();
        }
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.playerController == null
                || mc.currentScreen != null || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().func_190926_b()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) {
            return;
        }

        int replenishBelow = threshold == null ? 16 : (int) threshold.getValDouble();
        for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
            ItemStack hotbarStack = mc.player.inventory.getStackInSlot(hotbarIndex);
            boolean hotbarEmpty = hotbarStack == null || hotbarStack.func_190926_b();
            if (!hotbarEmpty) {
                previousHotbarStacks[hotbarIndex] = hotbarStack.copy();
            }
            ItemStack matchStack = hotbarEmpty ? previousHotbarStacks[hotbarIndex] : hotbarStack;
            if (matchStack == null || (!hotbarEmpty
                    && (hotbarStack.func_190916_E() >= replenishBelow
                            || hotbarStack.func_190916_E() >= hotbarStack.getMaxStackSize()))) {
                continue;
            }

            int inventorySlot = findMatchingInventoryStack(matchStack, hotbarEmpty);
            if (inventorySlot < 0) {
                continue;
            }

            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, inventorySlot,
                    hotbarIndex, ClickType.SWAP, mc.player);
            long delayMillis = delay == null ? 250L : (long) delay.getValDouble();
            nextActionTime = now + Math.max(0L, delayMillis);
            return;
        }
    }

    private int findMatchingInventoryStack(ItemStack hotbarStack, boolean hotbarEmpty) {
        int bestSlot = -1;
        int bestCount = hotbarEmpty ? 0 : hotbarStack.func_190916_E();
        for (int inventoryIndex = 9; inventoryIndex < 36; inventoryIndex++) {
            ItemStack candidate = mc.player.inventory.getStackInSlot(inventoryIndex);
            if (candidate == null || candidate.func_190926_b() || candidate.func_190916_E() <= bestCount) {
                continue;
            }
            if (ItemStack.areItemsEqual(hotbarStack, candidate)
                    && ItemStack.areItemStackTagsEqual(hotbarStack, candidate)) {
                bestSlot = inventoryIndex;
                bestCount = candidate.func_190916_E();
            }
        }
        return bestSlot;
    }
}
