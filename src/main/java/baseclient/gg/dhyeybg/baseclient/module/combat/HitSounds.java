package baseclient.gg.dhyeybg.baseclient.module.combat;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;

public class HitSounds extends Module {
    private int targetId = -1;
    private int previousHurtTime;

    public HitSounds() {
        super("Hit Sounds", "Plays a sound when the targeted entity takes damage", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.objectMouseOver == null
                || !(mc.objectMouseOver.entityHit instanceof EntityLivingBase)) {
            targetId = -1;
            previousHurtTime = 0;
            return;
        }
        EntityLivingBase target = (EntityLivingBase) mc.objectMouseOver.entityHit;
        if (targetId != target.getEntityId()) {
            targetId = target.getEntityId();
            previousHurtTime = target.hurtTime;
            return;
        }
        if (target.hurtTime > 0 && previousHurtTime == 0) {
            mc.player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 0.8F, 1.0F);
        }
        previousHurtTime = target.hurtTime;
    }
}
