package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class AutoTool extends Module {
	private Setting onlyWhileMining;

	public AutoTool() {
		super("Auto Tool", "Selects the fastest hotbar tool for the targeted block", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		onlyWhileMining = new Setting("Only While Mining", this, false);
		manager.rSetting(onlyWhileMining);
	}

	@Override
	public void onUpdate() {
		if (mc.player == null || mc.world == null || mc.objectMouseOver == null
				|| mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) return;
		if (onlyWhileMining != null && onlyWhileMining.getValBoolean()
				&& !mc.gameSettings.keyBindAttack.isKeyDown()) return;

		BlockPos pos = mc.objectMouseOver.getBlockPos();
		IBlockState state = mc.world.getBlockState(pos);
		int bestSlot = mc.player.inventory.currentItem;
		float bestSpeed = 1.0F;
		for (int slot = 0; slot < 9; slot++) {
			float speed = mc.player.inventory.getStackInSlot(slot).getStrVsBlock(state);
			if (speed > bestSpeed) {
				bestSpeed = speed;
				bestSlot = slot;
			}
		}
		mc.player.inventory.currentItem = bestSlot;
		mc.playerController.updateController();
	}
}
