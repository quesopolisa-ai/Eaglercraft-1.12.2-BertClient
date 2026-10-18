package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.util.EnumHand;

public class AutoFish extends Module {
	private Setting recastDelay;
	private Setting useOffhand;
	private int waitTicks;
	private boolean reeledForCurrentHook;

	public AutoFish() {
		super("Auto Fish", "Reels in a caught fish while holding a fishing rod", Category.PLAYER);
	}

	public void registerSettings(SettingsManager manager) {
		recastDelay = new Setting("Recast Delay", this, 12.0D, 1.0D, 40.0D, true);
		useOffhand = new Setting("Use Offhand", this, true);
		manager.rSetting(recastDelay);
		manager.rSetting(useOffhand);
	}

	@Override
	public void onEnable() {
		waitTicks = 0;
		reeledForCurrentHook = false;
	}

	@Override
	public void onUpdate() {
		if (mc == null || mc.player == null || mc.world == null || mc.playerController == null) {
			waitTicks = 0;
			reeledForCurrentHook = false;
			return;
		}
		if (!SingleplayerWorld.isSingleplayer()) {
			return;
		}
		if (mc.currentScreen != null) return;
		EnumHand rodHand = getRodHand();
		if (rodHand == null) {
			waitTicks = 0;
			reeledForCurrentHook = false;
			return;
		}
		if (mc.player.fishEntity == null) {
			if (waitTicks > 0) {
				waitTicks--;
				return;
			}
			reeledForCurrentHook = false;
			mc.playerController.processRightClick(mc.player, mc.world, rodHand);
			waitTicks = 10;
			return;
		}
		if (!reeledForCurrentHook && (mc.player.fishEntity.isCatchable()
				|| mc.player.fishEntity.isInWater() && mc.player.fishEntity.motionY < -0.04D)) {
			mc.playerController.processRightClick(mc.player, mc.world, rodHand);
			reeledForCurrentHook = true;
			waitTicks = recastDelay == null ? 12 : (int) recastDelay.getValDouble();
		}
	}

	private EnumHand getRodHand() {
		if (mc.player.getHeldItemMainhand().getItem() instanceof ItemFishingRod) {
			return EnumHand.MAIN_HAND;
		}
		if ((useOffhand == null || useOffhand.getValBoolean())
				&& mc.player.getHeldItemOffhand().getItem() instanceof ItemFishingRod) {
			return EnumHand.OFF_HAND;
		}
		return null;
	}

	@Override
	public void onDisable() {
		waitTicks = 0;
		reeledForCurrentHook = false;
	}
}
