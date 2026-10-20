package baseclient.gg.dhyeybg.baseclient.module.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.lax1dude.eaglercraft.opengl.WorldRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class ESP extends Module {
    private static final int MAX_ORES = 600;
    private static final int MAX_STORAGE = 200;
    private static final int MAX_SPAWNERS = 128;
    private static final int MAX_ITEMS = 150;
    private static final int MAX_ENTITIES = 100;
    private static final int STORAGE_SCAN_TICKS = 10;
    private static final int ORE_RESTART_TICKS = 10;
    private static final int ORE_SCAN_BLOCK_BUDGET = 1024;

    private final Setting players = new Setting("Players", this, true);
    private final Setting mobs = new Setting("Mobs", this, true);
    private final Setting items = new Setting("Items", this, true);
    private final Setting ores = new Setting("Ores", this, true);
    private final Setting storage = new Setting("Storage", this, true);
    private final Setting tracers = new Setting("Tracers", this, true);
    private final Setting boxes = new Setting("Boxes", this, true);
    private final Setting nameTags = new Setting("Name Tags", this, true);
    private final Setting healthTags = new Setting("Health", this, true);
    private final Setting distanceTags = new Setting("Distance", this, false);
    private final Setting spawners = new Setting("Spawners", this, true);
    private final Setting entityRange = Setting.textBox("Entity Range", this, 48.0D, 4.0D, 256.0D);
    private final Setting itemRange = Setting.textBox("Item Range", this, 32.0D, 4.0D, 128.0D);
    private final Setting oreRange = Setting.textBox("Ore Range", this, 16.0D, 4.0D, 64.0D);
    private final Setting storageRange = Setting.textBox("Storage Range", this, 48.0D, 4.0D, 256.0D);
    private final Setting spawnerRange = Setting.textBox("Spawner Range", this, 48.0D, 4.0D, 128.0D);
    private final Setting playerColor = colorSetting("Player Color", "Red");
    private final Setting mobColor = colorSetting("Mob Color", "Green");
    private final Setting itemColor = colorSetting("Item Color", "Purple");
    private final Setting coalColor = colorSetting("Coal Ore Color", "Gray");
    private final Setting ironColor = colorSetting("Iron Ore Color", "White");
    private final Setting goldColor = colorSetting("Gold Ore Color", "Yellow");
    private final Setting lapisColor = colorSetting("Lapis Ore Color", "Blue");
    private final Setting diamondColor = colorSetting("Diamond Ore Color", "Cyan");
    private final Setting redstoneColor = colorSetting("Redstone Ore Color", "Red");
    private final Setting emeraldColor = colorSetting("Emerald Ore Color", "Green");
    private final Setting quartzColor = colorSetting("Quartz Ore Color", "Orange");
    private final Setting storageColor = colorSetting("Storage Color", "Yellow");
    private final Setting spawnerColor = colorSetting("Spawner Color", "Purple");

    private final List<BlockPos> nearbyOres = new ArrayList<BlockPos>();
    private final List<BlockPos> pendingOres = new ArrayList<BlockPos>();
    private final List<BlockPos> oreScanOffsets = new ArrayList<BlockPos>();
    private final List<BlockPos> nearbyStorage = new ArrayList<BlockPos>();
    private final List<BlockPos> nearbySpawners = new ArrayList<BlockPos>();
    private final List<EntityLivingBase> cachedTargets = new ArrayList<EntityLivingBase>();
    private final List<EntityItem> cachedItems = new ArrayList<EntityItem>();
    private net.minecraft.world.World scannedWorld;

    private boolean oreScanActive;
    private int oreScanColumn;
    private int oreScanY;
    private int oreScanCenterX;
    private int oreScanCenterZ;
    private int oreScanRange;
    private int ticksUntilOreScan;
    private int ticksUntilStorageScan;
    private static final float[] FRIEND_COLOR = new float[] { 0.2F, 1.0F, 0.3F };

    public ESP() {
        super("ESP", "Entity, item, ore and storage outlines", Category.RENDER);
    }

    public void registerSettings(SettingsManager settingsManager) {
        settingsManager.rSetting(players);
        settingsManager.rSetting(mobs);
        settingsManager.rSetting(items);
        settingsManager.rSetting(ores);
        settingsManager.rSetting(storage);
        settingsManager.rSetting(tracers);
        settingsManager.rSetting(boxes);
        settingsManager.rSetting(nameTags);
        settingsManager.rSetting(healthTags);
        settingsManager.rSetting(distanceTags);
        settingsManager.rSetting(spawners);
        settingsManager.rSetting(entityRange);
        settingsManager.rSetting(itemRange);
        settingsManager.rSetting(oreRange);
        settingsManager.rSetting(storageRange);
        settingsManager.rSetting(spawnerRange);
        settingsManager.rSetting(playerColor);
        settingsManager.rSetting(mobColor);
        settingsManager.rSetting(itemColor);
        settingsManager.rSetting(coalColor);
        settingsManager.rSetting(ironColor);
        settingsManager.rSetting(goldColor);
        settingsManager.rSetting(lapisColor);
        settingsManager.rSetting(diamondColor);
        settingsManager.rSetting(redstoneColor);
        settingsManager.rSetting(emeraldColor);
        settingsManager.rSetting(quartzColor);
        settingsManager.rSetting(storageColor);
        settingsManager.rSetting(spawnerColor);
    }

    @Override
    public void onEnable() {
        resetScans();
    }

    @Override
    public void onDisable() {
        resetScans();
        nearbyOres.clear();
        nearbyStorage.clear();
        nearbySpawners.clear();
        cachedTargets.clear();
        cachedItems.clear();
    }

    private void resetScans() {
        oreScanActive = false;
        pendingOres.clear();
        oreScanOffsets.clear();
        ticksUntilOreScan = 0;
        ticksUntilStorageScan = 0;
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null) {
            nearbyOres.clear();
            nearbyStorage.clear();
            nearbySpawners.clear();
            cachedTargets.clear();
            cachedItems.clear();
            resetScans();
            return;
        }

        if (scannedWorld != mc.world) {
            nearbyOres.clear();
            nearbyStorage.clear();
            nearbySpawners.clear();
            resetScans();
            scannedWorld = mc.world;
        }
        if (oreScanActive) {
            int moveThreshold = Math.max(4, oreScanRange / 2);
            if (Math.abs((int) Math.floor(mc.player.posX) - oreScanCenterX) > moveThreshold
                    || Math.abs((int) Math.floor(mc.player.posZ) - oreScanCenterZ) > moveThreshold) {
                resetScans();
            }
        }
        refreshEntityTargets();
        if (ores.getValBoolean()) {
            pruneNearbyOres();
            tickOreScan();
        } else {
            nearbyOres.clear();
            pendingOres.clear();
            oreScanOffsets.clear();
            oreScanActive = false;
        }

        if (storage.getValBoolean() || spawners.getValBoolean()) {
            if (ticksUntilStorageScan-- <= 0) {
                if (storage.getValBoolean()) {
                    scanStorage();
                } else {
                    nearbyStorage.clear();
                }
                if (spawners.getValBoolean()) {
                    scanSpawners();
                } else {
                    nearbySpawners.clear();
                }
                ticksUntilStorageScan = STORAGE_SCAN_TICKS;
            }
        } else {
            nearbyStorage.clear();
            nearbySpawners.clear();
        }
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (mc == null || mc.player == null || mc.world == null) {
            return;
        }
        Entity view = mc.getRenderViewEntity();
        boolean drawGeometry = boxes.getValBoolean() || tracers.getValBoolean();
        boolean drawLabels = nameTags.getValBoolean();
        if (view == null || (!drawGeometry && !drawLabels)) {
            return;
        }

        boolean drawOres = drawGeometry && ores.getValBoolean() && !nearbyOres.isEmpty();
        boolean drawStorage = drawGeometry && storage.getValBoolean() && !nearbyStorage.isEmpty();
        boolean drawSpawners = drawGeometry && spawners.getValBoolean() && !nearbySpawners.isEmpty();
        List<EntityLivingBase> targets = getTargets();
        List<EntityItem> itemTargets = getItemTargets();
        boolean hasLabels = drawLabels && (!targets.isEmpty() || !itemTargets.isEmpty());
        if (!hasLabels && !drawOres && !drawStorage && !drawSpawners
                && (!drawGeometry || (targets.isEmpty() && itemTargets.isEmpty()))) {
            return;
        }

        // Same camera-relative origin EntityRenderer uses for the world pass (feet position, interpolated).
        double viewerX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double viewerY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double viewerZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;
        double eyeY = view.getEyeHeight();

        float[] playerRgb = getColor(playerColor);
        float[] mobRgb = getColor(mobColor);
        float[] itemRgb = getColor(itemColor);
        float[] storageRgb = getColor(storageColor);
        float[] spawnerRgb = getColor(spawnerColor);

        if (drawGeometry) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer buffer = tessellator.getBuffer();

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableCull();

            buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);

            for (EntityLivingBase target : targets) {
                addEntity(buffer, target, partialTicks, viewerX, viewerY, viewerZ, eyeY,
                        target instanceof EntityPlayer
                                ? (baseclient.gg.dhyeybg.baseclient.module.player.MiddleClickFriend
                                        .isFriend(target.getName()) ? FRIEND_COLOR : playerRgb)
                                : mobRgb);
            }
            for (EntityItem item : itemTargets) {
                addEntity(buffer, item, partialTicks, viewerX, viewerY, viewerZ, eyeY, itemRgb);
            }
            if (drawOres) {
                for (BlockPos ore : nearbyOres) {
                    if (mc.world.isBlockLoaded(ore)) {
                        Setting oreColor = getOreColorSetting(mc.world.getBlockState(ore).getBlock());
                        if (oreColor != null) {
                            addBlock(buffer, ore, viewerX, viewerY, viewerZ, eyeY, getColor(oreColor));
                        }
                    }
                }
            }
            if (drawStorage) {
                for (BlockPos pos : nearbyStorage) {
                    addBlock(buffer, pos, viewerX, viewerY, viewerZ, eyeY, storageRgb);
                }
            }
            if (drawSpawners) {
                for (BlockPos pos : nearbySpawners) {
                    addBlock(buffer, pos, viewerX, viewerY, viewerZ, eyeY, spawnerRgb);
                }
            }

            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
        }

        if (drawLabels) {
            RenderManager renderManager = mc.getRenderManager();
            for (EntityLivingBase target : targets) {
                String label = buildEntityLabel(target);
                if (label != null) {
                    drawNameTag(renderManager, target, label, partialTicks, viewerX, viewerY, viewerZ);
                }
            }
            for (EntityItem item : itemTargets) {
                String label = buildItemLabel(item);
                if (label != null) {
                    drawNameTag(renderManager, item, label, partialTicks, viewerX, viewerY, viewerZ);
                }
            }
        }
    }

    private void addEntity(WorldRenderer buffer, Entity entity, float partialTicks, double viewerX, double viewerY,
            double viewerZ, double eyeY, float[] rgb) {
        AxisAlignedBB bounds = getRenderBounds(entity, partialTicks, viewerX, viewerY, viewerZ);
        if (boxes.getValBoolean()) {
            addBoxLines(buffer, bounds, rgb);
        }
        if (tracers.getValBoolean()) {
            addLine(buffer, 0.0D, eyeY, 0.0D,
                    (bounds.minX + bounds.maxX) * 0.5D,
                    (bounds.minY + bounds.maxY) * 0.5D,
                    (bounds.minZ + bounds.maxZ) * 0.5D, rgb);
        }
    }

    private void addBlock(WorldRenderer buffer, BlockPos pos, double viewerX, double viewerY, double viewerZ,
            double eyeY, float[] rgb) {
        if (boxes.getValBoolean()) {
            addBoxLines(buffer, new AxisAlignedBB(pos.getX() - viewerX, pos.getY() - viewerY, pos.getZ() - viewerZ,
                    pos.getX() + 1.0D - viewerX, pos.getY() + 1.0D - viewerY, pos.getZ() + 1.0D - viewerZ), rgb);
        }
        if (tracers.getValBoolean()) {
            addLine(buffer, 0.0D, eyeY, 0.0D, pos.getX() + 0.5D - viewerX, pos.getY() + 0.5D - viewerY,
                    pos.getZ() + 0.5D - viewerZ, rgb);
        }
    }

    @Override
    public void onRender2D() {
        if (mc == null || mc.player == null || mc.world == null
                || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        if (pos == null || !mc.world.isBlockLoaded(pos)) {
            return;
        }

        Block block = mc.world.getBlockState(pos).getBlock();
        String label = null;
        int color = 0xFFFFFFFF;
        if (ores.getValBoolean() && isOre(block)) {
            label = formatBlockName(block);
            Setting oreColor = getOreColorSetting(block);
            color = oreColor == null ? color : colorToArgb(oreColor.getValString());
        } else if (storage.getValBoolean() && isStorageBlock(block)) {
            label = formatBlockName(block);
            color = colorToArgb(storageColor.getValString());
        } else if (spawners.getValBoolean() && isSpawner(block)) {
            label = "Mob Spawner";
            color = colorToArgb(spawnerColor.getValString());
        }
        if (label == null) {
            return;
        }

        ScaledResolution resolution = new ScaledResolution(mc);
        int x = resolution.getScaledWidth() / 2 + 8;
        int y = resolution.getScaledHeight() / 2 + 8;
        int width = mc.fontRendererObj.getStringWidth(label);
        Gui.drawRect(x - 3, y - 2, x + width + 3, y + 10, 0xB9101018);
        mc.fontRendererObj.drawStringWithShadow(label, x, y, color);
    }

    private List<EntityLivingBase> getTargets() {
        return cachedTargets;
    }

    private List<EntityItem> getItemTargets() {
        return cachedItems;
    }

    private String buildEntityLabel(EntityLivingBase entity) {
        if (entity == null || !nameTags.getValBoolean()) {
            return null;
        }
        StringBuilder label = new StringBuilder(entity.getDisplayName().getFormattedText());
        if (entity instanceof EntityPlayer
                && baseclient.gg.dhyeybg.baseclient.module.player.MiddleClickFriend.isFriend(entity.getName())) {
            label.insert(0, "\u00a7a");
        }
        if (healthTags.getValBoolean()) {
            float maxHealth = entity.getMaxHealth();
            float health = entity.getHealth();
            float ratio = maxHealth <= 0.0F ? 0.0F : health / maxHealth;
            String healthColor = ratio > 0.6F ? "\u00a7a" : ratio > 0.3F ? "\u00a7e" : "\u00a7c";
            label.append(" \u00a77[").append(healthColor).append(formatOneDecimal(health)).append("\u00a77]");
        }
        appendDistance(label, entity);
        return label.toString();
    }

    private String buildItemLabel(EntityItem entity) {
        if (entity == null || !nameTags.getValBoolean() || entity.getEntityItem().func_190926_b()) {
            return null;
        }
        net.minecraft.item.ItemStack stack = entity.getEntityItem();
        StringBuilder label = new StringBuilder(stack.getDisplayName());
        if (stack.func_190916_E() > 1) {
            label.append(" \u00a77x").append(stack.func_190916_E());
        }
        appendDistance(label, entity);
        return label.toString();
    }

    private void appendDistance(StringBuilder label, Entity entity) {
        if (distanceTags.getValBoolean()) {
            label.append(" \u00a78(").append(formatOneDecimal(Math.sqrt(entity.getDistanceSqToEntity(mc.player))))
                    .append("m)");
        }
    }

    private void drawNameTag(RenderManager renderManager, Entity entity, String label, float partialTicks,
            double viewerX, double viewerY, double viewerZ) {
        if (renderManager == null) {
            return;
        }
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - viewerX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - viewerY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - viewerZ;
        float labelY = (float) y + entity.height + 0.5F - (entity.isSneaking() ? 0.25F : 0.0F);
        EntityRenderer.drawNameplate(mc.fontRendererObj, label, (float) x, labelY, (float) z, 0,
                renderManager.playerViewY, renderManager.playerViewX,
                renderManager.options != null && renderManager.options.thirdPersonView == 2,
                entity.isSneaking());
    }

    private static String formatOneDecimal(double value) {
        int tenths = (int) Math.round(value * 10.0D);
        return (tenths / 10) + "." + (tenths % 10);
    }

    private void refreshEntityTargets() {
        cachedTargets.clear();
        cachedItems.clear();
        boolean scanLiving = players.getValBoolean() || mobs.getValBoolean();
        boolean scanItems = items.getValBoolean();
        if ((!boxes.getValBoolean() && !tracers.getValBoolean() && !nameTags.getValBoolean())
                || (!scanLiving && !scanItems)) {
            return;
        }
        double entityRangeSq = entityRange.getValDouble() * entityRange.getValDouble();
        double itemRangeSq = itemRange.getValDouble() * itemRange.getValDouble();
        for (Entity entity : mc.world.loadedEntityList) {
            double dx = entity.posX - mc.player.posX;
            double dy = entity.posY - mc.player.posY;
            double dz = entity.posZ - mc.player.posZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (scanLiving && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand)
                    && entity != mc.player && entity.isEntityAlive() && distanceSq <= entityRangeSq) {
                EntityLivingBase living = (EntityLivingBase) entity;
                if ((living instanceof EntityPlayer && players.getValBoolean())
                        || (!(living instanceof EntityPlayer) && mobs.getValBoolean())) {
                    if (cachedTargets.size() < MAX_ENTITIES) {
                        cachedTargets.add(living);
                    }
                }
            } else if (scanItems && entity instanceof EntityItem && !entity.isDead
                    && distanceSq <= itemRangeSq && cachedItems.size() < MAX_ITEMS) {
                cachedItems.add((EntityItem) entity);
            }
        }
    }

    private void tickOreScan() {
        if (!oreScanActive) {
            if (ticksUntilOreScan-- > 0) {
                return;
            }
            oreScanRange = Math.max(1, Math.min(64, (int) oreRange.getValDouble()));
            oreScanCenterX = (int) Math.floor(mc.player.posX);
            oreScanCenterZ = (int) Math.floor(mc.player.posZ);
            buildOreScanOffsets(oreScanRange);
            oreScanColumn = 0;
            oreScanY = 0;
            pendingOres.clear();
            oreScanActive = true;
        } else {
            int configuredRange = Math.max(1, Math.min(64, (int) oreRange.getValDouble()));
            if (configuredRange != oreScanRange) {
                oreScanActive = false;
                ticksUntilOreScan = 0;
                tickOreScan();
                return;
            }
        }

        int blocksScanned = 0;
        while (blocksScanned < ORE_SCAN_BLOCK_BUDGET && oreScanColumn < oreScanOffsets.size()
                && pendingOres.size() < MAX_ORES) {
            BlockPos offset = oreScanOffsets.get(oreScanColumn);
            BlockPos pos = new BlockPos(oreScanCenterX + offset.getX(), oreScanY,
                    oreScanCenterZ + offset.getZ());
            if (mc.world.isBlockLoaded(pos) && isOre(mc.world.getBlockState(pos).getBlock())) {
                pendingOres.add(pos);
                if (!nearbyOres.contains(pos) && nearbyOres.size() < MAX_ORES) {
                    nearbyOres.add(pos);
                }
            }
            blocksScanned++;
            oreScanY++;
            if (oreScanY >= 256) {
                oreScanY = 0;
                oreScanColumn++;
            }
        }

        if (oreScanColumn >= oreScanOffsets.size() || pendingOres.size() >= MAX_ORES) {
            nearbyOres.clear();
            nearbyOres.addAll(pendingOres);
            oreScanActive = false;
            ticksUntilOreScan = ORE_RESTART_TICKS;
        }
    }

    private void buildOreScanOffsets(int range) {
        oreScanOffsets.clear();
        for (int ring = 0; ring <= range; ring++) {
            for (int x = -ring; x <= ring; x++) {
                for (int z = -ring; z <= ring; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == ring && x * x + z * z <= range * range) {
                        oreScanOffsets.add(new BlockPos(x, 0, z));
                    }
                }
            }
        }
    }

    private void pruneNearbyOres() {
        double range = Math.max(1.0D, Math.min(64.0D, oreRange.getValDouble()));
        double rangeSquared = range * range;
        Iterator<BlockPos> iterator = nearbyOres.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (!mc.world.isBlockLoaded(pos)
                    || mc.player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                            pos.getZ() + 0.5D) > rangeSquared
                    || !isOre(mc.world.getBlockState(pos).getBlock())) {
                iterator.remove();
            }
        }
    }

    private void scanStorage() {
        nearbyStorage.clear();
        double rangeSquared = storageRange.getValDouble() * storageRange.getValDouble();
        for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
            BlockPos pos = tileEntity.getPos();
            if (pos == null || mc.player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D) > rangeSquared) {
                continue;
            }
            if (isStorageBlock(mc.world.getBlockState(pos).getBlock())) {
                nearbyStorage.add(pos);
                if (nearbyStorage.size() >= MAX_STORAGE) {
                    break;
                }
            }
        }
    }

    private void scanSpawners() {
        nearbySpawners.clear();
        double rangeSquared = spawnerRange.getValDouble() * spawnerRange.getValDouble();
        for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
            if (!(tileEntity instanceof TileEntityMobSpawner)) {
                continue;
            }
            BlockPos pos = tileEntity.getPos();
            if (pos == null || mc.player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D) > rangeSquared) {
                continue;
            }
            nearbySpawners.add(pos);
            if (nearbySpawners.size() >= MAX_SPAWNERS) {
                break;
            }
        }
    }

    private static boolean isOre(Block block) {
        return block == Blocks.COAL_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DIAMOND_ORE || block == Blocks.REDSTONE_ORE
                || block == Blocks.LIT_REDSTONE_ORE || block == Blocks.EMERALD_ORE || block == Blocks.QUARTZ_ORE;
    }

    private static boolean isStorageBlock(Block block) {
        return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST
                || block == Blocks.HOPPER || block == Blocks.DROPPER || block == Blocks.DISPENSER
                || block == Blocks.FURNACE || block == Blocks.LIT_FURNACE || block == Blocks.BREWING_STAND
                || block instanceof BlockShulkerBox;
    }

    private static boolean isSpawner(Block block) {
        return block == Blocks.MOB_SPAWNER;
    }

    private Setting getOreColorSetting(Block block) {
        if (block == Blocks.COAL_ORE) return coalColor;
        if (block == Blocks.IRON_ORE) return ironColor;
        if (block == Blocks.GOLD_ORE) return goldColor;
        if (block == Blocks.LAPIS_ORE) return lapisColor;
        if (block == Blocks.DIAMOND_ORE) return diamondColor;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) return redstoneColor;
        if (block == Blocks.EMERALD_ORE) return emeraldColor;
        if (block == Blocks.QUARTZ_ORE) return quartzColor;
        return null;
    }

    private Setting colorSetting(String name, String defaultColor) {
        ArrayList<String> options = new ArrayList<String>();
        options.add("Red");
        options.add("Green");
        options.add("Blue");
        options.add("Purple");
        options.add("Cyan");
        options.add("Yellow");
        options.add("White");
        options.add("Gray");
        options.add("Orange");
        return new Setting(name, this, defaultColor, options);
    }

    private static AxisAlignedBB getRenderBounds(Entity entity, float partialTicks,
            double viewerX, double viewerY, double viewerZ) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        return entity.getEntityBoundingBox().offset(x - entity.posX - viewerX,
                y - entity.posY - viewerY, z - entity.posZ - viewerZ);
    }

    private static void addBoxLines(WorldRenderer buffer, AxisAlignedBB box, float[] color) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        addLine(buffer, x1, y1, z1, x2, y1, z1, color);
        addLine(buffer, x2, y1, z1, x2, y1, z2, color);
        addLine(buffer, x2, y1, z2, x1, y1, z2, color);
        addLine(buffer, x1, y1, z2, x1, y1, z1, color);
        addLine(buffer, x1, y2, z1, x2, y2, z1, color);
        addLine(buffer, x2, y2, z1, x2, y2, z2, color);
        addLine(buffer, x2, y2, z2, x1, y2, z2, color);
        addLine(buffer, x1, y2, z2, x1, y2, z1, color);
        addLine(buffer, x1, y1, z1, x1, y2, z1, color);
        addLine(buffer, x2, y1, z1, x2, y2, z1, color);
        addLine(buffer, x2, y1, z2, x2, y2, z2, color);
        addLine(buffer, x1, y1, z2, x1, y2, z2, color);
    }

    private static void addLine(WorldRenderer buffer, double x1, double y1, double z1,
            double x2, double y2, double z2, float[] color) {
        buffer.pos(x1, y1, z1).color(color[0], color[1], color[2], 1.0F).endVertex();
        buffer.pos(x2, y2, z2).color(color[0], color[1], color[2], 1.0F).endVertex();
    }

    private static float[] getColor(Setting setting) {
        return getColorForName(setting.getValString());
    }

    private static float[] getColorForName(String name) {
        if ("Green".equals(name)) return new float[] { 0.2F, 1.0F, 0.3F };
        if ("Blue".equals(name)) return new float[] { 0.2F, 0.5F, 1.0F };
        if ("Purple".equals(name)) return new float[] { 0.8F, 0.3F, 1.0F };
        if ("Cyan".equals(name)) return new float[] { 0.2F, 1.0F, 1.0F };
        if ("Yellow".equals(name)) return new float[] { 1.0F, 0.9F, 0.2F };
        if ("White".equals(name)) return new float[] { 1.0F, 1.0F, 1.0F };
        if ("Gray".equals(name)) return new float[] { 0.62F, 0.65F, 0.7F };
        if ("Orange".equals(name)) return new float[] { 1.0F, 0.48F, 0.12F };
        return new float[] { 1.0F, 0.25F, 0.25F };
    }

    private static int colorToArgb(String name) {
        float[] color = getColorForName(name);
        return 0xFF000000 | ((int) (color[0] * 255.0F) << 16) | ((int) (color[1] * 255.0F) << 8)
                | (int) (color[2] * 255.0F);
    }

    private static String formatBlockName(Block block) {
        ResourceLocation resourceLocation = Block.REGISTRY.getNameForObject(block);
        if (resourceLocation == null) {
            return block.getLocalizedName();
        }
        String path = resourceLocation.getResourcePath().replace('_', ' ');
        StringBuilder label = new StringBuilder(path.length());
        boolean capitalize = true;
        for (int i = 0; i < path.length(); i++) {
            char character = path.charAt(i);
            label.append(capitalize ? Character.toUpperCase(character) : character);
            capitalize = character == ' ';
        }
        return label.toString();
    }
}
