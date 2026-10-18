package baseclient.gg.dhyeybg.baseclient.module.render;

import java.util.ArrayList;
import java.util.List;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.lax1dude.eaglercraft.opengl.WorldRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Breadcrumbs extends Module {
    private static final int MAX_POINTS = 256;
    private final List<Vec3d> path = new ArrayList<Vec3d>();

    public Breadcrumbs() {
        super("Breadcrumbs", "Draws a short trail along your recent path", Category.RENDER);
    }

    @Override
    public void onEnable() {
        path.clear();
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.world == null) {
            path.clear();
            return;
        }
        Vec3d point = new Vec3d(mc.player.posX, mc.player.posY + 0.05D, mc.player.posZ);
        if (path.isEmpty() || path.get(path.size() - 1).squareDistanceTo(point) >= 1.0D) {
            path.add(point);
            if (path.size() > MAX_POINTS) {
                path.remove(0);
            }
        }
    }

    @Override
    public void onRender3D(float partialTicks) {
        Entity view = mc == null ? null : mc.getRenderViewEntity();
        if (view == null || path.size() < 2) {
            return;
        }

        double cameraX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double cameraY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double cameraZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 1; i < path.size(); i++) {
            Vec3d from = path.get(i - 1);
            Vec3d to = path.get(i);
            float intensity = 0.25F + 0.75F * i / path.size();
            buffer.pos(from.xCoord - cameraX, from.yCoord - cameraY, from.zCoord - cameraZ)
                    .color(0.75F, 0.35F, 1.0F, intensity).endVertex();
            buffer.pos(to.xCoord - cameraX, to.yCoord - cameraY, to.zCoord - cameraZ)
                    .color(0.75F, 0.35F, 1.0F, intensity).endVertex();
        }
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
