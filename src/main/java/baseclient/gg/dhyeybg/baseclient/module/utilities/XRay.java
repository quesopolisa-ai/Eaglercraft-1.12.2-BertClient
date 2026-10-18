package baseclient.gg.dhyeybg.baseclient.module.utilities;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;

public class XRay extends Module {
	private Setting showValuables;

	public XRay() {
		super("XRay", "Hides common terrain blocks while showing ores", Category.UTILITIES);
	}

	public void registerSettings(SettingsManager manager) {
		showValuables = new Setting("Valuable Blocks", this, true);
		manager.rSetting(showValuables);
	}

	public boolean shouldRender(IBlockState state) {
		Block block = state.getBlock();
		if (!isToggled() || !baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld.isModuleAllowed(this)) return true;
		boolean ore = block == Blocks.COAL_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE
				|| block == Blocks.REDSTONE_ORE || block == Blocks.LAPIS_ORE || block == Blocks.DIAMOND_ORE
				|| block == Blocks.EMERALD_ORE || block == Blocks.QUARTZ_ORE;
		if (ore) return true;
		return showValuables != null && showValuables.getValBoolean()
				&& (block == Blocks.DIAMOND_BLOCK || block == Blocks.EMERALD_BLOCK
				|| block == Blocks.GOLD_BLOCK || block == Blocks.IRON_BLOCK || block == Blocks.CHEST
				|| block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST
				|| block == Blocks.MOB_SPAWNER);
	}

	@Override
	public void onEnable() {
		reloadChunks();
	}

	@Override
	public void onDisable() {
		reloadChunks();
	}

	private void reloadChunks() {
		if (mc != null && mc.renderGlobal != null) mc.renderGlobal.loadRenderers();
	}
}
