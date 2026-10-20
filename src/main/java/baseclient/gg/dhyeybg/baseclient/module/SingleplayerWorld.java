
package baseclient.gg.dhyeybg.baseclient.module;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public final class SingleplayerWorld {
    private static final Map<String, Boolean> MULTIPLAYER_ALLOWED =
            new HashMap<String, Boolean>();

    static {
        MULTIPLAYER_ALLOWED.put("clickgui", true);
        MULTIPLAYER_ALLOWED.put("hud", true);
        MULTIPLAYER_ALLOWED.put("fullbright", true);
        MULTIPLAYER_ALLOWED.put("esp", true);
        MULTIPLAYER_ALLOWED.put("criticals", true);
        MULTIPLAYER_ALLOWED.put("killaura", true);
        MULTIPLAYER_ALLOWED.put("auto crystal", true);
        MULTIPLAYER_ALLOWED.put("auto weapon", true);
        MULTIPLAYER_ALLOWED.put("auto gg", true);
        MULTIPLAYER_ALLOWED.put("wtap", true);
        MULTIPLAYER_ALLOWED.put("hit sounds", true);
        MULTIPLAYER_ALLOWED.put("combo counter", true);
        MULTIPLAYER_ALLOWED.put("bow silent aim", true);
        MULTIPLAYER_ALLOWED.put("bow release", true);
        MULTIPLAYER_ALLOWED.put("elytra fly", true);
        MULTIPLAYER_ALLOWED.put("elytra boost", true);
        MULTIPLAYER_ALLOWED.put("boat fly", true);
        MULTIPLAYER_ALLOWED.put("surround", true);
        MULTIPLAYER_ALLOWED.put("norender", true);
        MULTIPLAYER_ALLOWED.put("no particles", true);
        MULTIPLAYER_ALLOWED.put("nocluster", true);
        MULTIPLAYER_ALLOWED.put("totem counter", true);
        MULTIPLAYER_ALLOWED.put("scaffold", true);
        MULTIPLAYER_ALLOWED.put("auto builder", true);
        MULTIPLAYER_ALLOWED.put("auto mine", true);
        MULTIPLAYER_ALLOWED.put("chest looter", true);
        MULTIPLAYER_ALLOWED.put("sell cactus", true);
        MULTIPLAYER_ALLOWED.put("sprint", true);
        MULTIPLAYER_ALLOWED.put("velocity", true);
        MULTIPLAYER_ALLOWED.put("reach", true);
        MULTIPLAYER_ALLOWED.put("aimassist", true);
        MULTIPLAYER_ALLOWED.put("autoclicker", true);
        MULTIPLAYER_ALLOWED.put("triggerbot", true);
        MULTIPLAYER_ALLOWED.put("no slow", true);
        MULTIPLAYER_ALLOWED.put("safe walk", true);
        MULTIPLAYER_ALLOWED.put("inventory move", true);
        MULTIPLAYER_ALLOWED.put("nofall", true);
        MULTIPLAYER_ALLOWED.put("fly", true);
        MULTIPLAYER_ALLOWED.put("speed", true);
        MULTIPLAYER_ALLOWED.put("highjump", true);
        MULTIPLAYER_ALLOWED.put("longjump", true);
        MULTIPLAYER_ALLOWED.put("long pickup range", true);
        MULTIPLAYER_ALLOWED.put("auto walk", true);
        MULTIPLAYER_ALLOWED.put("parkour", true);
        MULTIPLAYER_ALLOWED.put("fast ladder", true);
        MULTIPLAYER_ALLOWED.put("no web", true);
        MULTIPLAYER_ALLOWED.put("anti void", true);
        MULTIPLAYER_ALLOWED.put("step", true);
        MULTIPLAYER_ALLOWED.put("jesus", true);
        MULTIPLAYER_ALLOWED.put("spider", true);
        MULTIPLAYER_ALLOWED.put("auto tool", true);
        MULTIPLAYER_ALLOWED.put("auto totem", true);
        MULTIPLAYER_ALLOWED.put("auto armor", true);
        MULTIPLAYER_ALLOWED.put("inventory manager", true);
        MULTIPLAYER_ALLOWED.put("autosort", true);
        MULTIPLAYER_ALLOWED.put("autodrop", true);
        MULTIPLAYER_ALLOWED.put("autofarm", true);
        MULTIPLAYER_ALLOWED.put("autoreplant", true);
        MULTIPLAYER_ALLOWED.put("autocraft", true);
        MULTIPLAYER_ALLOWED.put("autosmelt", true);
        MULTIPLAYER_ALLOWED.put("autoreconnect", true);
        MULTIPLAYER_ALLOWED.put("autoscreenshot", true);
        MULTIPLAYER_ALLOWED.put("auto respawn", true);
        MULTIPLAYER_ALLOWED.put("middle click friend", true);
        MULTIPLAYER_ALLOWED.put("auto eat", true);
        MULTIPLAYER_ALLOWED.put("hotbar replenish", true);
        MULTIPLAYER_ALLOWED.put("auto fish", true);
        MULTIPLAYER_ALLOWED.put("fastplace", true);
        MULTIPLAYER_ALLOWED.put("timer", true);
        MULTIPLAYER_ALLOWED.put("packet mine", true);
        MULTIPLAYER_ALLOWED.put("xray", false);
        MULTIPLAYER_ALLOWED.put("freecam", true);
        MULTIPLAYER_ALLOWED.put("waypoints", true);
        MULTIPLAYER_ALLOWED.put("time changer", true);
        MULTIPLAYER_ALLOWED.put("weather changer", true);
        MULTIPLAYER_ALLOWED.put("chat timestamps", true);
        MULTIPLAYER_ALLOWED.put("breadcrumbs", true);
        MULTIPLAYER_ALLOWED.put("camera effects", true);
        MULTIPLAYER_ALLOWED.put("auto login", true);
        MULTIPLAYER_ALLOWED.put("auto config", true);
    }

    private SingleplayerWorld() {
    }

    public static boolean isModuleAllowed(Module module) {
        Minecraft minecraft = Minecraft.getMinecraft();

        String moduleName = module.getName().toLowerCase(Locale.ROOT);

        if ("auto config".equals(moduleName)
                || "auto login".equals(moduleName)
                || "auto reconnect".equals(moduleName)) {
            return minecraft != null;
        }

        return minecraft != null
                && minecraft.world != null
                && (isSingleplayer()
                || Boolean.TRUE.equals(MULTIPLAYER_ALLOWED.get(moduleName)));
    }

    public static boolean isSingleplayer() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null && minecraft.isSingleplayer();
    }

    public static void notifyBlocked(String moduleName) {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.addChatMessage(
                    new TextComponentString(
                            moduleName + " is disabled in this world."
                    )
            );
        }
    }
}
