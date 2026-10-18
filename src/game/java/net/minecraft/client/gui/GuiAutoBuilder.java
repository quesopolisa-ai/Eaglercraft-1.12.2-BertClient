package net.minecraft.client.gui;

import java.io.IOException;
import java.util.Map;

import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoBuilder;
import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoBuilder.PlannedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

public class GuiAutoBuilder extends GuiScreen {
	private static final int IMPORT_BUTTON = 0;
	private static final int BUILD_BUTTON = 1;
	private static final int PREVIOUS_LAYER_BUTTON = 2;
	private static final int NEXT_LAYER_BUTTON = 3;
	private static final int CLOSE_BUTTON = 4;
	private static final int PREVIOUS_MATERIALS_BUTTON = 5;
	private static final int NEXT_MATERIALS_BUTTON = 6;
	private static final int SCHEMATIC_CORNER_ONE_BUTTON = 7;
	private static final int SCHEMATIC_CORNER_TWO_BUTTON = 8;
	private static final int EXPORT_SCHEMATIC_BUTTON = 9;
	private final AutoBuilder builder;
	private int materialPage;
	private GuiButton previousMaterialsButton;
	private GuiButton nextMaterialsButton;

	public GuiAutoBuilder(AutoBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		int left = width / 2 - 155;
		int bottom = height - 34;
		buttonList.add(new GuiButton(IMPORT_BUTTON, left, bottom, 90, 20, "Import Blueprint"));
		buttonList.add(new GuiButton(BUILD_BUTTON, left + 94, bottom, 90, 20,
				builder.isBuilding() ? "Stop" : "Auto Build"));
		buttonList.add(new GuiButton(PREVIOUS_LAYER_BUTTON, left + 188, bottom, 48, 20, "Layer -"));
		buttonList.add(new GuiButton(NEXT_LAYER_BUTTON, left + 238, bottom, 48, 20, "Layer +"));
		buttonList.add(new GuiButton(CLOSE_BUTTON, left + 290, bottom, 20, 20, "X"));
		previousMaterialsButton = new GuiButton(PREVIOUS_MATERIALS_BUTTON, left + 103, 75, 34, 16, "<");
		nextMaterialsButton = new GuiButton(NEXT_MATERIALS_BUTTON, left + 140, 75, 34, 16, ">");
		buttonList.add(previousMaterialsButton);
		buttonList.add(nextMaterialsButton);
		buttonList.add(new GuiButton(SCHEMATIC_CORNER_ONE_BUTTON, left, height - 62, 100, 20, "Mark Corner 1"));
		buttonList.add(new GuiButton(SCHEMATIC_CORNER_TWO_BUTTON, left + 104, height - 62, 100, 20,
				"Mark Corner 2"));
		buttonList.add(new GuiButton(EXPORT_SCHEMATIC_BUTTON, left + 208, height - 62, 102, 20, "Export .schematic"));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case IMPORT_BUTTON:
			builder.importStructure();
			break;
		case BUILD_BUTTON:
			if (builder.isBuilding()) {
				builder.stopBuild();
			} else {
				builder.startBuild();
			}
			button.displayString = builder.isBuilding() ? "Stop" : "Auto Build";
			break;
		case PREVIOUS_LAYER_BUTTON:
			builder.changeLayer(-1);
			break;
		case NEXT_LAYER_BUTTON:
			builder.changeLayer(1);
			break;
		case CLOSE_BUTTON:
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			break;
		case PREVIOUS_MATERIALS_BUTTON:
			materialPage = Math.max(0, materialPage - 1);
			break;
		case NEXT_MATERIALS_BUTTON:
			materialPage = Math.min(getMaterialPageCount() - 1, materialPage + 1);
			break;
		case SCHEMATIC_CORNER_ONE_BUTTON:
			builder.beginMarkSchematicCorner(1);
			break;
		case SCHEMATIC_CORNER_TWO_BUTTON:
			builder.beginMarkSchematicCorner(2);
			break;
		case EXPORT_SCHEMATIC_BUTTON:
			builder.exportSchematic();
			break;
		default:
			break;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int left = width / 2 - 155;
		int top = 20;
		drawRect(left, top, left + 310, height - 28, 0xE0101010);
		drawCenteredString(fontRendererObj, "Auto Builder - Survival", width / 2, top + 8, 0xFFFFFFFF);
		drawString(fontRendererObj, "File: " + (builder.getBlueprintName().isEmpty() ? "none" : builder.getBlueprintName()),
				left + 10, top + 27, 0xFFCCCCCC);
		drawString(fontRendererObj,
				"Size: " + builder.getSizeX() + " x " + builder.getSizeY() + " x " + builder.getSizeZ()
						+ "    Blocks: " + builder.getBlockCount(),
				left + 10, top + 39, 0xFFCCCCCC);

		drawString(fontRendererObj, "Required blocks", left + 10, top + 57, 0xFFFFFF55);
		int pageCount = getMaterialPageCount();
		materialPage = Math.max(0, Math.min(pageCount - 1, materialPage));
		previousMaterialsButton.enabled = materialPage > 0;
		nextMaterialsButton.enabled = materialPage + 1 < pageCount;
		int lineY = top + 69;
		int start = materialPage * 8;
		int index = 0;
		for (Map.Entry<String, Integer> entry : builder.getMaterials().entrySet()) {
			if (index++ < start) {
				continue;
			}
			if (index > start + 8) {
				break;
			}
			drawString(fontRendererObj, entry.getValue() + "x " + displayName(entry.getKey()), left + 10, lineY,
					0xFFFFFFFF);
			lineY += 10;
		}
		drawString(fontRendererObj, "Page " + (materialPage + 1) + " / " + pageCount, left + 10, top + 153,
				0xFFAAAAAA);

		drawString(fontRendererObj,
				"Layer " + (builder.getSizeY() == 0 ? 0 : builder.getCurrentLayer() + 1) + " / "
						+ builder.getSizeY(),
				left + 180, top + 57, 0xFFFFFF55);
		drawLayerPreview(left + 180, top + 72);

		drawString(fontRendererObj, "Corner 1: " + formatPosition(builder.getSchematicCornerOne()), left + 10,
				height - 96, 0xFFCCCCCC);
		drawString(fontRendererObj, "Corner 2: " + formatPosition(builder.getSchematicCornerTwo()), left + 160,
				height - 96, 0xFFCCCCCC);
		drawString(fontRendererObj, builder.getStatus(), left + 10, height - 85, 0xFFFFFFFF);
		drawString(fontRendererObj,
				"Import .nbt/.schematic; export by marking two corners. Uses hotbar blocks and does not move you.",
				left + 10, height - 74, 0xFFAAAAAA);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawLayerPreview(int x, int y) {
		int areaWidth = 120;
		int areaHeight = Math.max(40, Math.min(120, height - y - 70));
		drawRect(x - 1, y - 1, x + areaWidth + 1, y + areaHeight + 1, 0xFF666666);
		drawRect(x, y, x + areaWidth, y + areaHeight, 0xFF252525);
		if (builder.getBlockCount() == 0 || builder.getSizeX() <= 0 || builder.getSizeZ() <= 0) {
			return;
		}
		float cellWidth = areaWidth / (float) builder.getSizeX();
		float cellHeight = areaHeight / (float) builder.getSizeZ();
		for (PlannedBlock planned : builder.getBlocks()) {
			if (planned.pos.getY() != builder.getCurrentLayer()) {
				continue;
			}
			int x1 = x + (int) (planned.pos.getX() * cellWidth);
			int z1 = y + (int) (planned.pos.getZ() * cellHeight);
			int x2 = x + (int) ((planned.pos.getX() + 1) * cellWidth);
			int z2 = y + (int) ((planned.pos.getZ() + 1) * cellHeight);
			MapColor mapColor = planned.state.getMapColor(mc.world, planned.pos);
			drawRect(x1, z1, Math.max(x1 + 1, x2), Math.max(z1 + 1, z2),
					mapColor == null ? blockColor(planned.state.getBlock()) : 0xFF000000 | mapColor.colorValue);
		}
	}

	private int blockColor(Block block) {
		ResourceLocation name = Block.REGISTRY.getNameForObject(block);
		int hash = name == null ? block.hashCode() : name.toString().hashCode();
		int red = 80 + (hash & 0x7F);
		int green = 80 + ((hash >>> 8) & 0x7F);
		int blue = 80 + ((hash >>> 16) & 0x7F);
		return 0xFF000000 | red << 16 | green << 8 | blue;
	}

	private int getMaterialPageCount() {
		return Math.max(1, (builder.getMaterials().size() + 7) / 8);
	}

	private String displayName(String material) {
		int separator = material.lastIndexOf(':');
		String id = separator < 0 ? material : material.substring(0, separator);
		Block block = Block.REGISTRY.getObject(new ResourceLocation(id));
		if (block == null) {
			return material;
		}
		String name = block.getLocalizedName();
		return name + " [" + material.substring(separator + 1) + "]";
	}

	private String formatPosition(net.minecraft.util.math.BlockPos pos) {
		return pos == null ? "not set" : pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
