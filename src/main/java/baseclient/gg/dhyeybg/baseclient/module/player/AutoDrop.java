package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

public class AutoDrop extends Module {
    private Setting rottenFlesh;
    private Setting seeds;
    private Setting string;
    private Setting spiderEyes;
    private long nextDropTime;

    public AutoDrop() {
        super("AutoDrop", "Drops only the unwanted item types you select", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        rottenFlesh = new Setting("Rotten Flesh", this, false);
        seeds = new Setting("Seeds", this, false);
        string = new Setting("String", this, false);
        spiderEyes = new Setting("Spider Eyes", this, false);
        manager.rSetting(rottenFlesh);
        manager.rSetting(seeds);
        manager.rSetting(string);
        manager.rSetting(spiderEyes);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.playerController == null || mc.currentScreen != null
                || !SingleplayerWorld.isSingleplayer()
                || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().func_190926_b()
                || System.currentTimeMillis() < nextDropTime) {
            return;
        }
        for (int containerSlot = 9; containerSlot < 45; containerSlot++) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(containerSlot).getStack();
            if (stack == null || stack.func_190926_b() || !isSelectedUnwanted(stack.getItem())) {
                continue;
            }
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, containerSlot, 0,
                    ClickType.THROW, mc.player);
            nextDropTime = System.currentTimeMillis() + 300L;
            return;
        }
    }

    private boolean isSelectedUnwanted(Item item) {
        return rottenFlesh != null && rottenFlesh.getValBoolean() && item == Items.ROTTEN_FLESH
                || seeds != null && seeds.getValBoolean()
                        && (item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS
                                || item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS)
                || string != null && string.getValBoolean() && item == Items.STRING
                || spiderEyes != null && spiderEyes.getValBoolean() && item == Items.SPIDER_EYE;
    }
}
