package baseclient.gg.dhyeybg.baseclient.module.render;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;

public class NoParticles extends Module {
    public NoParticles() {
        super("No particles", "Hides particle effects", Category.RENDER);
    }

    public static boolean isEnabled() {
        return BaseClient.instance != null && BaseClient.instance.isModuleEnabled("No particles");
    }
}
