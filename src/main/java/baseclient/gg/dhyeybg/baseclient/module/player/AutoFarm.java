package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class AutoFarm extends Module {
    private Setting radius;
    private BlockPos miningPos;

    public AutoFarm() {
        super("AutoFarm", "Harvests mature wheat, carrot, potato, and beetroot crops nearby", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        radius = new Setting("Radius", this, 4.0D, 1.0D, 6.0D, true);
        manager.rSetting(radius);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null || mc.playerController == null
                || !SingleplayerWorld.isSingleplayer()
                || mc.currentScreen != null) {
            stopMining();
            return;
        }

        int scanRadius = radius == null ? 4 : (int) radius.getValDouble();
        BlockPos playerPos = new BlockPos(mc.player);
        BlockPos target = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    IBlockState state = mc.world.getBlockState(pos);
                    if (!(state.getBlock() instanceof BlockCrops)
                            || !((BlockCrops) state.getBlock()).isMaxAge(state)) {
                        continue;
                    }
                    double distance = mc.player.getDistanceSqToCenter(pos);
                    if (distance < bestDistance) {
                        target = pos;
                        bestDistance = distance;
                    }
                }
            }
        }

        if (target == null) {
            stopMining();
            return;
        }
        if (miningPos != null && !miningPos.equals(target)) {
            mc.playerController.resetBlockRemoving();
        }
        miningPos = target;
        mc.playerController.onPlayerDamageBlock(target, EnumFacing.UP);
    }

    @Override
    public void onDisable() {
        stopMining();
    }

    private void stopMining() {
        if (miningPos != null && mc != null && mc.playerController != null) {
            mc.playerController.resetBlockRemoving();
        }
        miningPos = null;
    }
}
