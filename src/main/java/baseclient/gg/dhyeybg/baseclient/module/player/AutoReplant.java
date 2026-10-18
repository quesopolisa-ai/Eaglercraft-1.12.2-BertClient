package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AutoReplant extends Module {
    private long nextPlantTime;

    public AutoReplant() {
        super("AutoReplant", "Replants supported crops on empty farmland", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
                || !SingleplayerWorld.isSingleplayer() || mc.currentScreen != null
                || System.currentTimeMillis() < nextPlantTime) {
            return;
        }
        BlockPos playerPos = new BlockPos(mc.player);
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos cropPos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(cropPos).getBlock() != Blocks.AIR
                            || mc.world.getBlockState(cropPos.down()).getBlock() != Blocks.FARMLAND) {
                        continue;
                    }
                    int seedSlot = findSeedSlot();
                    if (seedSlot < 0) {
                        return;
                    }
                    int previousSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = seedSlot;
                    mc.playerController.processRightClickBlock(mc.player, mc.world, cropPos.down(),
                            EnumFacing.UP, new net.minecraft.util.math.Vec3d(0.5D, 1.0D, 0.5D), EnumHand.MAIN_HAND);
                    mc.player.inventory.currentItem = previousSlot;
                    nextPlantTime = System.currentTimeMillis() + 200L;
                    return;
                }
            }
        }
    }

    private int findSeedSlot() {
        for (int slot = 0; slot < 9; slot++) {
            if (isPlantingItem(mc.player.inventory.getStackInSlot(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private boolean isPlantingItem(ItemStack stack) {
        if (stack == null || stack.func_190926_b()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Items.WHEAT_SEEDS || item == Items.CARROT || item == Items.POTATO
                || item == Items.BEETROOT_SEEDS;
    }
}
