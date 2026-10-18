package baseclient.gg.dhyeybg.baseclient.module.render;

import java.util.LinkedHashMap;
import java.util.Map;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class TotemCounter extends Module {
    private static final Map<String, Integer> POP_COUNTS = new LinkedHashMap<String, Integer>();
    private static World trackedWorld;
    private Setting includeInventory;

    public TotemCounter() {
        super("Totem Counter", "Shows the number of Totems of Undying you have", Category.RENDER);
        visible = false;
    }

    public void registerSettings(SettingsManager manager) {
        includeInventory = new Setting("Include Inventory", this, true);
        manager.rSetting(includeInventory);
    }

    @Override
    public void onRender2D() {
        if (mc == null || mc.player == null || mc.fontRendererObj == null) {
            return;
        }

        int count = 0;
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            count += mc.player.getHeldItemOffhand().func_190916_E();
        }
        if (includeInventory == null || includeInventory.getValBoolean()) {
            for (ItemStack stack : mc.player.inventory.mainInventory) {
                if (stack != null && !stack.func_190926_b() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    count += stack.func_190916_E();
                }
            }
        }

        ScaledResolution resolution = new ScaledResolution(mc);
        String label = "Totems: " + count;
        int y = Math.max(30, mc.fontRendererObj.FONT_HEIGHT * 3);
        mc.fontRendererObj.drawStringWithShadow(label, 2, y, 0xFFFFFFFF);
        if (trackedWorld != mc.world) {
            POP_COUNTS.clear();
            trackedWorld = mc.world;
        }
        y += mc.fontRendererObj.FONT_HEIGHT + 2;
        for (Map.Entry<String, Integer> entry : POP_COUNTS.entrySet()) {
            mc.fontRendererObj.drawStringWithShadow(entry.getKey() + " popped " + entry.getValue(),
                    2, y, 0xFFFFAA55);
            y += mc.fontRendererObj.FONT_HEIGHT + 1;
            if (y >= resolution.getScaledHeight() - 36) {
                break;
            }
        }
    }

    public static void recordPop(Entity entity) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        String name = entity.getName();
        Integer previous = POP_COUNTS.get(name);
        POP_COUNTS.put(name, previous == null ? 1 : previous + 1);
        while (POP_COUNTS.size() > 8) {
            String first = POP_COUNTS.keySet().iterator().next();
            POP_COUNTS.remove(first);
        }
    }
}
