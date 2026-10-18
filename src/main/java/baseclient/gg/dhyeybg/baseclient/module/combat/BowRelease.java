package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;

public class BowRelease extends Module {
    private Setting chargeTicks;

    public BowRelease() {
        super("Bow Release", "Automatically releases a drawn bow after its charge threshold", Category.COMBAT);
    }

    public void registerSettings(SettingsManager manager) {
        chargeTicks = new Setting("Charge Ticks", this, 20.0D, 1.0D, 20.0D, true);
        manager.rSetting(chargeTicks);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.playerController == null || !mc.player.isHandActive()
                || mc.player.getActiveItemStack().getItem() != Items.BOW
                || !mc.gameSettings.keyBindUseItem.isKeyDown()) {
            return;
        }

        int charge = mc.player.getActiveItemStack().getMaxItemUseDuration() - mc.player.getItemInUseCount();
        int requiredCharge = chargeTicks == null ? 20 : (int) chargeTicks.getValDouble();
        if (ItemBow.getArrowVelocity(charge) >= ItemBow.getArrowVelocity(requiredCharge)) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }
    }
}
