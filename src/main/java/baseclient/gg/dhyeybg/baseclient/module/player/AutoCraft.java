package baseclient.gg.dhyeybg.baseclient.module.player;

import java.util.ArrayList;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class AutoCraft extends Module {
    private Setting recipe;

    private static final Ingredient PLANK = Ingredient.func_193367_a(Item.getItemFromBlock(Blocks.PLANKS));
    private static final Ingredient STICK = Ingredient.func_193367_a(Items.STICK);
    private static final Ingredient OAK_LOG = Ingredient.func_193369_a(
            new ItemStack(Item.getItemFromBlock(Blocks.LOG), 1, 0));
    private static final Ingredient COAL = Ingredient.func_193369_a(
            new ItemStack(Items.COAL, 1, 32767));
    private static final Ingredient COBBLESTONE = Ingredient.func_193367_a(Item.getItemFromBlock(Blocks.COBBLESTONE));
    private static final Ingredient SUGAR_CANE = Ingredient.func_193367_a(Items.REEDS);
    private static final Ingredient PAPER = Ingredient.func_193367_a(Items.PAPER);
    private static final Ingredient LEATHER = Ingredient.func_193367_a(Items.LEATHER);
    private static final Ingredient WHEAT = Ingredient.func_193367_a(Items.WHEAT);
    private static final Ingredient STRING = Ingredient.func_193367_a(Items.STRING);

    private static final Plan[] PLANS = new Plan[] {
            new Plan("Oak Planks", 1, 1, new Ingredient[] { OAK_LOG },
                    new ItemStack(Item.getItemFromBlock(Blocks.PLANKS), 4, 0)),
            new Plan("Sticks", 1, 2, new Ingredient[] { PLANK, PLANK },
                    new ItemStack(Items.STICK, 4)),
            new Plan("Crafting Table", 2, 2, new Ingredient[] { PLANK, PLANK, PLANK, PLANK },
                    new ItemStack(Item.getItemFromBlock(Blocks.CRAFTING_TABLE))),
            new Plan("Chest", 3, 3, new Ingredient[] {
                    PLANK, PLANK, PLANK, PLANK, null, PLANK, PLANK, PLANK, PLANK
            }, new ItemStack(Item.getItemFromBlock(Blocks.CHEST))),
            new Plan("Torch", 1, 2, new Ingredient[] { COAL, STICK },
                    new ItemStack(Item.getItemFromBlock(Blocks.TORCH), 4)),
            new Plan("Bread", 3, 1, new Ingredient[] { WHEAT, WHEAT, WHEAT },
                    new ItemStack(Items.BREAD)),
            new Plan("Paper", 3, 1, new Ingredient[] { SUGAR_CANE, SUGAR_CANE, SUGAR_CANE },
                    new ItemStack(Items.PAPER, 3)),
            new Plan("Book", 2, 2, new Ingredient[] { PAPER, PAPER, PAPER, LEATHER },
                    new ItemStack(Items.BOOK)),
            new Plan("Bowl", 3, 2, new Ingredient[] { PLANK, null, PLANK, null, PLANK, null },
                    new ItemStack(Items.BOWL, 4)),
            new Plan("Ladder", 3, 3, new Ingredient[] {
                    STICK, null, STICK, STICK, STICK, STICK, STICK, null, STICK
            }, new ItemStack(Item.getItemFromBlock(Blocks.LADDER), 3)),
            new Plan("Furnace", 3, 3, new Ingredient[] {
                    COBBLESTONE, COBBLESTONE, COBBLESTONE, COBBLESTONE, null, COBBLESTONE,
                    COBBLESTONE, COBBLESTONE, COBBLESTONE
            }, new ItemStack(Item.getItemFromBlock(Blocks.FURNACE))),
            new Plan("Wooden Pickaxe", 3, 3, new Ingredient[] {
                    PLANK, PLANK, PLANK, null, STICK, null, null, STICK, null
            }, new ItemStack(Items.WOODEN_PICKAXE)),
            new Plan("Stone Pickaxe", 3, 3, new Ingredient[] {
                    COBBLESTONE, COBBLESTONE, COBBLESTONE, null, STICK, null, null, STICK, null
            }, new ItemStack(Items.STONE_PICKAXE)),
            new Plan("Wooden Sword", 1, 3, new Ingredient[] { PLANK, PLANK, STICK },
                    new ItemStack(Items.WOODEN_SWORD)),
            new Plan("Bow", 3, 3, new Ingredient[] {
                    STRING, STICK, null, STRING, null, STICK, STRING, STICK, null
            }, new ItemStack(Items.BOW)),
            new Plan("Fishing Rod", 3, 3, new Ingredient[] {
                    null, null, STICK, null, STICK, STRING, STICK, null, STRING
            }, new ItemStack(Items.FISHING_ROD))
    };

    public AutoCraft() {
        super("AutoCraft", "Select a recipe in ClickGUI and craft it in an open crafting interface", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        ArrayList<String> options = new ArrayList<String>();
        for (Plan plan : PLANS) {
            options.add(plan.name);
        }
        recipe = new Setting("Recipe", this, options.get(0), options);
        manager.rSetting(recipe);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
                || !SingleplayerWorld.isSingleplayer()) {
            return;
        }

        Container container = mc.player.openContainer;
        int gridSize;
        if (mc.currentScreen instanceof GuiInventory && container instanceof ContainerPlayer) {
            gridSize = 2;
        } else if (mc.currentScreen instanceof GuiCrafting && container instanceof ContainerWorkbench) {
            gridSize = 3;
        } else {
            return;
        }

        Plan plan = getSelectedPlan();
        if (plan.width > gridSize || plan.height > gridSize) {
            return;
        }
        int gridSlots = gridSize * gridSize;
        if (!mc.player.inventory.getItemStack().func_190926_b()) {
            return;
        }

        ItemStack result = container.getSlot(0).getStack();
        if (matchesOutput(result, plan.output)) {
            mc.playerController.windowClick(container.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
            return;
        }

        int planSize = plan.width * plan.height;
        int firstInputSlot = -1;
        for (int slot = 1; slot <= gridSlots; slot++) {
            int inputIndex = (slot - 1) / gridSize * plan.width + (slot - 1) % gridSize;
            boolean insidePlan = inputIndex < planSize && (slot - 1) % gridSize < plan.width;
            ItemStack inputStack = container.getSlot(slot).getStack();
            Ingredient expected = insidePlan ? plan.ingredients[inputIndex] : null;
            if (!inputStack.func_190926_b()
                    && (expected == null || !expected.apply(inputStack))) {
                mc.playerController.windowClick(container.windowId, slot, 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }
            if (firstInputSlot < 0 && insidePlan && expected != null && inputStack.func_190926_b()) {
                firstInputSlot = slot;
            }
        }
        if (firstInputSlot < 0) {
            return;
        }
        int inputIndex = (firstInputSlot - 1) / gridSize * plan.width + (firstInputSlot - 1) % gridSize;
        Ingredient ingredient = plan.ingredients[inputIndex];
        if (!hasIngredients(container, plan, gridSize)) {
            return;
        }
        int sourceSlot = findIngredientSlot(container, ingredient);
        if (sourceSlot >= 0) {
            placeStack(container, plan, gridSize, sourceSlot, ingredient);
        }
    }

    private Plan getSelectedPlan() {
        String selected = recipe == null ? PLANS[0].name : recipe.getValString();
        for (Plan plan : PLANS) {
            if (plan.name.equals(selected)) {
                return plan;
            }
        }
        return PLANS[0];
    }

    private boolean hasIngredients(Container container, Plan plan, int gridSize) {
        for (int ingredientIndex = 0; ingredientIndex < plan.width * plan.height; ingredientIndex++) {
            Ingredient ingredient = plan.ingredients[ingredientIndex];
            if (ingredient == null) {
                continue;
            }
            int required = 0;
            for (int otherIndex = 0; otherIndex < plan.width * plan.height; otherIndex++) {
                if (plan.ingredients[otherIndex] == ingredient) {
                    required++;
                }
            }
            int placed = 0;
            for (int otherIndex = 0; otherIndex < plan.width * plan.height; otherIndex++) {
                if (plan.ingredients[otherIndex] == ingredient) {
                    int gridSlot = 1 + (otherIndex / plan.width) * gridSize + otherIndex % plan.width;
                    placed += container.getSlot(gridSlot).getStack().func_190916_E();
                }
            }
            int available = 0;
            for (Slot slot : container.inventorySlots) {
                if (slot.inventory == mc.player.inventory && ingredient.apply(slot.getStack())) {
                    available += slot.getStack().func_190916_E();
                }
            }
            if (available < required - placed) {
                return false;
            }
        }
        return true;
    }

    private int findIngredientSlot(Container container, Ingredient ingredient) {
        for (Slot slot : container.inventorySlots) {
            if (slot.inventory == mc.player.inventory && ingredient.apply(slot.getStack())) {
                return slot.slotNumber;
            }
        }
        return -1;
    }

    private void placeStack(Container container, Plan plan, int gridSize, int sourceSlot, Ingredient ingredient) {
        mc.playerController.windowClick(container.windowId, sourceSlot, 0, ClickType.PICKUP, mc.player);
        if (!mc.player.inventory.getItemStack().func_190926_b()) {
            mc.playerController.windowClick(container.windowId, -999,
                    Container.getQuickcraftMask(0, 0), ClickType.QUICK_CRAFT, mc.player);
            for (int ingredientIndex = 0; ingredientIndex < plan.width * plan.height; ingredientIndex++) {
                if (plan.ingredients[ingredientIndex] != ingredient) {
                    continue;
                }
                int gridSlot = 1 + (ingredientIndex / plan.width) * gridSize + ingredientIndex % plan.width;
                mc.playerController.windowClick(container.windowId, gridSlot,
                        Container.getQuickcraftMask(1, 0), ClickType.QUICK_CRAFT, mc.player);
            }
            mc.playerController.windowClick(container.windowId, -999,
                    Container.getQuickcraftMask(2, 0), ClickType.QUICK_CRAFT, mc.player);
        }
        if (!mc.player.inventory.getItemStack().func_190926_b()) {
            mc.playerController.windowClick(container.windowId, sourceSlot, 0, ClickType.PICKUP, mc.player);
        }
    }

    private static boolean matchesOutput(ItemStack result, ItemStack expected) {
        return result != null && !result.func_190926_b()
                && ItemStack.areItemsEqual(result, expected)
                && ItemStack.areItemStackTagsEqual(result, expected);
    }

    private static final class Plan {
        private final String name;
        private final int width;
        private final int height;
        private final Ingredient[] ingredients;
        private final ItemStack output;

        private Plan(String name, int width, int height, Ingredient[] ingredients, ItemStack output) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.ingredients = ingredients;
            this.output = output;
        }
    }
}
