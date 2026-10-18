package baseclient.gg.dhyeybg.baseclient.module.render;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.util.EnumParticleTypes;

public class NoCluster extends Module {
    public NoCluster() {
        super("NoCluster", "Hides large explosion particle clusters", Category.RENDER);
    }

    public static boolean shouldSuppress(EnumParticleTypes type) {
        if (BaseClient.instance == null || !BaseClient.instance.isModuleEnabled("NoCluster")) {
            return false;
        }
        return type == EnumParticleTypes.EXPLOSION_NORMAL || type == EnumParticleTypes.EXPLOSION_LARGE
                || type == EnumParticleTypes.EXPLOSION_HUGE;
    }
}
