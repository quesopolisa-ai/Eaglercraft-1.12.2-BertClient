package baseclient.gg.dhyeybg.baseclient.module.combat;

import java.util.Collection;

import com.google.common.collect.Multimap;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;

public class AutoWeapon extends Module {
    private Setting onlyWhenTargeting;

    public AutoWeapon() {
        super("Auto Weapon", "Selects the strongest hotbar weapon when targeting an entity", Category.COMBAT);
    }

    public void registerSettings(SettingsManager manager) {
        onlyWhenTargeting = new Setting("Only When Targeting", this, true);
        manager.rSetting(onlyWhenTargeting);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.currentScreen != null) {
            return;
        }
        if (onlyWhenTargeting == null || onlyWhenTargeting.getValBoolean()) {
            Entity target = mc.objectMouseOver == null ? null : mc.objectMouseOver.entityHit;
            if (!(target instanceof EntityLivingBase) || !target.isEntityAlive()) {
                return;
            }
        }

        int bestSlot = mc.player.inventory.currentItem;
        double bestDamage = getDamage(mc.player.inventory.getStackInSlot(bestSlot));
        for (int slot = 0; slot < 9; slot++) {
            double damage = getDamage(mc.player.inventory.getStackInSlot(slot));
            if (damage > bestDamage + 0.01D) {
                bestDamage = damage;
                bestSlot = slot;
            }
        }
        if (bestDamage > 0.0D) {
            mc.player.inventory.currentItem = bestSlot;
        }
    }

    private double getDamage(ItemStack stack) {
        if (stack == null || stack.func_190926_b()) {
            return 0.0D;
        }
        Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        Collection<AttributeModifier> damageModifiers =
                modifiers.get(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName());
        double damage = 0.0D;
        for (AttributeModifier modifier : damageModifiers) {
            damage += modifier.getAmount();
        }
        return damage;
    }
}
