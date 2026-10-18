package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;

public class ComboCounter extends Module {
    private int targetId = -1;
    private int combo;
    private int previousHurtTime;
    private long lastHitTime;

    public ComboCounter() {
        super("Combo Counter", "Counts consecutive hits on the targeted entity", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.objectMouseOver == null
                || !(mc.objectMouseOver.entityHit instanceof EntityLivingBase)) {
            reset();
            return;
        }
        EntityLivingBase target = (EntityLivingBase) mc.objectMouseOver.entityHit;
        if (target.getEntityId() != targetId) {
            targetId = target.getEntityId();
            combo = 0;
            previousHurtTime = target.hurtTime;
        } else if (target.hurtTime > 0 && previousHurtTime == 0) {
            long now = System.currentTimeMillis();
            combo = now - lastHitTime <= 2500L ? combo + 1 : 1;
            lastHitTime = now;
        }
        previousHurtTime = target.hurtTime;
    }

    @Override
    public void onRender2D() {
        if (mc == null || combo <= 0) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        String text = "Combo: " + combo;
        mc.fontRendererObj.drawStringWithShadow(text,
                resolution.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(text) / 2,
                resolution.getScaledHeight() / 2 + 40, 0xFFFFAA55);
    }

    private void reset() {
        targetId = -1;
        combo = 0;
        previousHurtTime = 0;
        lastHitTime = 0L;
    }
}
