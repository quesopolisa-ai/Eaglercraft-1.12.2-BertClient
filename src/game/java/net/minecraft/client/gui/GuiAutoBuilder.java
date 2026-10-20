package net.minecraft.client.gui;

import java.io.IOException;
import java.util.Map;

import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoBuilder;
import baseclient.gg.dhyeybg.baseclient.module.misc.Singleplayer.AutoBuilder.PlannedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GuiAutoBuilder extends GuiScreen {
	private static final int PANEL_WIDTH = 440;
	private static final int MATERIALS_PER_PAGE = 12;
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
	private static final int SPEED_DOWN_BUTTON = 10;
	private static final int SPEED_UP_BUTTON = 11;
	private static final int ROTATE_BUTTON = 12;
	private static final int SET_ANCHOR_BUTTON = 13;
	private static final int PLAYER_ANCHOR_BUTTON = 14;
	private final AutoBuilder builder;
	private int materialPage;
	private GuiButton previousMaterialsButton;
	private GuiButton nextMaterialsButton;
	private GuiTextField anchorXField;
	private GuiTextField anchorYField;
	private GuiTextField anchorZField;

	public GuiAutoBuilder(AutoBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		int left = width / 2 - PANEL_WIDTH / 2;
		int bottom = height - 34;
		buttonList.add(new GuiButton(IMPORT_BUTTON, left, bottom, 92, 20, "Import"));
		buttonList.add(new GuiButton(BUILD_BUTTON, left + 96, bottom, 94, 20,
				builder.isBuilding() ? "Stop Build" : "Auto Build"));
		buttonList.add(new GuiButton(ROTATE_BUTTON, left + 194, bottom, 100, 20, "Rotate 90"));
		buttonList.add(new GuiButton(SPEED_DOWN_BUTTON, left + 298, bottom, 54, 20, "B/s -"));
		buttonList.add(new GuiButton(SPEED_UP_BUTTON, left + 356, bottom, 54, 20, "B/s +"));
		buttonList.add(new GuiButton(CLOSE_BUTTON, left + 414, bottom, 24, 20, "X"));
		previousMaterialsButton = new GuiButton(PREVIOUS_MATERIALS_BUTTON, left + 128, 89, 34, 17, "<");
		nextMaterialsButton = new GuiButton(NEXT_MATERIALS_BUTTON, left + 165, 89, 34, 17, ">");
		buttonList.add(previousMaterialsButton);
		buttonList.add(nextMaterialsButton);
		buttonList.add(new GuiButton(PREVIOUS_LAYER_BUTTON, left + 226, 296, 88, 20, "Layer -"));
		buttonList.add(new GuiButton(NEXT_LAYER_BUTTON, left + 318, 296, 88, 20, "Layer +"));
		buttonList.add(new GuiButton(SCHEMATIC_CORNER_ONE_BUTTON, left, height - 61, 108, 20,
				"Mark Corner 1"));
		buttonList.add(new GuiButton(SCHEMATIC_CORNER_TWO_BUTTON, left + 112, height - 61, 108, 20,
				"Mark Corner 2"));
		buttonList.add(new GuiButton(EXPORT_SCHEMATIC_BUTTON, left + 224, height - 61, 214, 20,
				"Export Selection as .schematic"));

		BlockPos anchor = builder.getAnchor();
		anchorXField = new GuiTextField(0, fontRendererObj, left + 91, 67, 48, 18);
		anchorYField = new GuiTextField(1, fontRendererObj, left + 143, 67, 48, 18);
		anchorZField = new GuiTextField(2, fontRendererObj, left + 195, 67, 48, 18);
		anchorXField.setMaxStringLength(11);
		anchorYField.setMaxStringLength(11);
		anchorZField.setMaxStringLength(11);
		if (anchor != null) {
			anchorXField.setText(Integer.toString(anchor.getX()));
			anchorYField.setText(Integer.toString(anchor.getY()));
			anchorZField.setText(Integer.toString(anchor.getZ()));
		}
		buttonList.add(new GuiButton(SET_ANCHOR_BUTTON, left + 247, 66, 76, 20, "Set Anchor"));
		buttonList.add(new GuiButton(PLAYER_ANCHOR_BUTTON, left + 327, 66, 111, 20, "Use Player Pos"));
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
			button.displayString = builder.isBuilding() ? "Stop Build" : "Auto Build";
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
		case SPEED_DOWN_BUTTON:
			builder.adjustPlaceSpeed(-1);
			break;
		case SPEED_UP_BUTTON:
			builder.adjustPlaceSpeed(1);
			break;
		case ROTATE_BUTTON:
			builder.rotateClockwise();
			break;
		case SET_ANCHOR_BUTTON:
			builder.setAnchorFromText(anchorXField.getText(), anchorYField.getText(), anchorZField.getText());
			break;
		case PLAYER_ANCHOR_BUTTON:
			builder.usePlayerPositionAsAnchor();
			BlockPos anchor = builder.getAnchor();
			if (anchor != null) {
				anchorXField.setText(Integer.toString(anchor.getX()));
				anchorYField.setText(Integer.toString(anchor.getY()));
				anchorZField.setText(Integer.toString(anchor.getZ()));
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 28) {
			builder.setAnchorFromText(anchorXField.getText(), anchorYField.getText(), anchorZField.getText());
			return;
		}
		boolean consumed = anchorXField.textboxKeyTyped(typedChar, keyCode);
		consumed |= anchorYField.textboxKeyTyped(typedChar, keyCode);
		consumed |= anchorZField.textboxKeyTyped(typedChar, keyCode);
		if (!consumed) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		anchorXField.mouseClicked(mouseX, mouseY, mouseButton);
		anchorYField.mouseClicked(mouseX, mouseY, mouseButton);
		anchorZField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void updateScreen() {
		anchorXField.updateCursorCounter();
		anchorYField.updateCursorCounter();
		anchorZField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int left = width / 2 - PANEL_WIDTH / 2;
		int top = 12;
		drawRect(left, top, left + PANEL_WIDTH, height - 10, 0xEE101820);
		drawRect(left + 1, top + 1, left + PANEL_WIDTH - 1, top + 30, 0xFF1C2B38);
		drawCenteredString(fontRendererObj, "AUTO BUILDER", width / 2, top + 8, 0xFF9BE4F2);

		String fileName = builder.getBlueprintName().isEmpty() ? "No blueprint loaded" : builder.getBlueprintName();
		drawString(fontRendererObj, fontRendererObj.trimStringToWidth("Blueprint: " + fileName, PANEL_WIDTH - 20),
				left + 10, top + 36, 0xFFDFEAF0);
		String dimensions = "Dimensions: " + builder.getSizeX() + " x " + builder.getSizeY() + " x "
				+ builder.getSizeZ() + "     Blocks: " + builder.getBlockCount();
		drawString(fontRendererObj, dimensions, left + 10, top + 49, 0xFFB8C9D4);
		drawString(fontRendererObj, "Build anchor", left + 10, 71, 0xFF9BE4F2);
		anchorXField.drawTextBox();
		anchorYField.drawTextBox();
		anchorZField.drawTextBox();

		int columnTop = 94;
		drawString(fontRendererObj, "Required items", left + 10, columnTop, 0xFFFFD57A);
		drawString(fontRendererObj, "Layer " + (builder.getSizeY() == 0 ? 0 : builder.getCurrentLayer() + 1)
				+ " / " + builder.getSizeY() + "     Rotation: " + builder.getRotationDegrees() + " deg",
				left + 226, columnTop, 0xFFFFD57A);

		int pageCount = getMaterialPageCount();
		materialPage = Math.max(0, Math.min(pageCount - 1, materialPage));
		previousMaterialsButton.enabled = materialPage > 0;
		nextMaterialsButton.enabled = materialPage + 1 < pageCount;
		int lineY = columnTop + 23;
		int start = materialPage * MATERIALS_PER_PAGE;
		int index = 0;
		for (Map.Entry<String, Integer> entry : builder.getMaterials().entrySet()) {
			if (index++ < start) {
				continue;
			}
			if (index > start + MATERIALS_PER_PAGE) {
				break;
			}
			String requirement = entry.getValue() + " x " + displayName(entry.getKey());
			drawString(fontRendererObj, fontRendererObj.trimStringToWidth(requirement, 204), left + 10, lineY,
					0xFFEAF0F4);
			lineY += 11;
		}
		drawString(fontRendererObj, "Placed " + builder.getPlacedBlockCount() + " / " + builder.getBlockCount(),
				left + 10, 253, 0xFF7AE0B4);
		drawString(fontRendererObj, "Page " + (materialPage + 1) + " / " + pageCount, left + 125, 253,
				0xFF9AABB7);

		drawLayerPreview(left + 226, columnTop + 17);
		drawString(fontRendererObj, "Footprint: " + builder.getSizeX() + " x " + builder.getSizeZ(), left + 226,
				257, 0xFFB8C9D4);
		drawString(fontRendererObj, "Rate: " + builder.getPlaceSpeed() + " blocks/sec (1-30)", left + 226, 269,
				0xFF9BE4F2);
		drawString(fontRendererObj, "Ghost | green done | red blocked", left + 226, 281, 0xFF8FA2AD);

		drawString(fontRendererObj, "Corner 1: " + formatPosition(builder.getSchematicCornerOne()), left + 10,
				height - 104, 0xFFB8C9D4);
		drawString(fontRendererObj, "Corner 2: " + formatPosition(builder.getSchematicCornerTwo()), left + 226,
				height - 104, 0xFFB8C9D4);
		drawString(fontRendererObj, fontRendererObj.trimStringToWidth(builder.getStatus(), PANEL_WIDTH - 20), left + 10,
				height - 91, 0xFFFFFFFF);
		drawString(fontRendererObj, fontRendererObj.trimStringToWidth(
				"Set exact anchor coordinates; reopening keeps them. Walk near the preview to build.", PANEL_WIDTH - 20),
				left + 10, height - 79, 0xFF9AABB7);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawLayerPreview(int x, int y) {
		int areaWidth = 190;
		int areaHeight = 140;
		drawRect(x - 2, y - 2, x + areaWidth + 2, y + areaHeight + 2, 0xFF6A8190);
		drawRect(x, y, x + areaWidth, y + areaHeight, 0xFF19232B);
		if (builder.getBlockCount() == 0 || builder.getSizeX() <= 0 || builder.getSizeZ() <= 0) {
			drawCenteredString(fontRendererObj, "Import a blueprint", x + areaWidth / 2, y + areaHeight / 2 - 4,
					0xFF8397A3);
			return;
		}
		float scale = Math.min(areaWidth / (float) builder.getSizeX(), areaHeight / (float) builder.getSizeZ());
		int mapWidth = Math.max(1, Math.round(builder.getSizeX() * scale));
		int mapHeight = Math.max(1, Math.round(builder.getSizeZ() * scale));
		int mapX = x + (areaWidth - mapWidth) / 2;
		int mapY = y + (areaHeight - mapHeight) / 2;
		for (PlannedBlock planned : builder.getBlocks()) {
			if (planned.pos.getY() != builder.getCurrentLayer()) {
				continue;
			}
			int x1 = mapX + Math.round(planned.pos.getX() * scale);
			int z1 = mapY + Math.round(planned.pos.getZ() * scale);
			int x2 = mapX + Math.round((planned.pos.getX() + 1) * scale);
			int z2 = mapY + Math.round((planned.pos.getZ() + 1) * scale);
			x2 = Math.max(x1 + 1, x2);
			z2 = Math.max(z1 + 1, z2);
			MapColor mapColor = planned.state.getMapColor(mc.world, planned.pos);
			int color = mapColor == null ? blockColor(planned.state.getBlock()) : 0xFF000000 | mapColor.colorValue;
			drawRect(x1, z1, x2, z2, color);
			if (x2 - x1 > 2 && z2 - z1 > 2) {
				drawRect(x1, z1, x2, z1 + 1, 0x55304050);
				drawRect(x1, z2 - 1, x2, z2, 0x55304050);
			}
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
		return Math.max(1, (builder.getMaterials().size() + MATERIALS_PER_PAGE - 1) / MATERIALS_PER_PAGE);
	}

	private String displayName(String material) {
		int separator = material.lastIndexOf(':');
		String id = separator < 0 ? material : material.substring(0, separator);
		Block block = Block.REGISTRY.getObject(new ResourceLocation(id));
		if (block == null) {
			return material;
		}
		Item item = AutoBuilder.getPlacementItem(block);
		if (item == Items.AIR) {
			return "No placement item: " + block.getLocalizedName();
		}
		int metadata = 0;
		if (separator >= 0 && item instanceof ItemBlock) {
			try {
				metadata = Integer.parseInt(material.substring(separator + 1));
			} catch (NumberFormatException ignored) {
				metadata = 0;
			}
		}
		return new ItemStack(item, 1, metadata).getDisplayName();
	}

	private String formatPosition(BlockPos pos) {
		return pos == null ? "not set" : pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}