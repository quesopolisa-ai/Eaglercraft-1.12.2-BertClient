package baseclient.gg.dhyeybg.baseclient.module.player;

import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

public class MiddleClickFriend extends Module {
    private static final Set<String> FRIENDS = new LinkedHashSet<String>();

    public MiddleClickFriend() {
        super("Middle Click Friend", "Adds or removes the targeted player from your local friends list", Category.PLAYER);
    }

    public static void toggleTarget(EntityPlayer target) {
        if (target == null || mc == null || mc.player == null) {
            return;
        }
        String name = target.getName();
        if (FRIENDS.remove(name)) {
            mc.player.addChatMessage(new TextComponentString("Removed " + name + " from friends."));
        } else {
            FRIENDS.add(name);
            mc.player.addChatMessage(new TextComponentString("Added " + name + " to friends."));
        }
        if (baseclient.gg.dhyeybg.baseclient.BaseClient.instance != null) {
            baseclient.gg.dhyeybg.baseclient.BaseClient.instance.saveConfig();
        }
    }

    public static boolean isFriend(String playerName) {
        return playerName != null && FRIENDS.contains(playerName);
    }

    public static JSONArray serialize() throws JSONException {
        JSONArray saved = new JSONArray();
        for (String friend : FRIENDS) {
            saved.put(friend);
        }
        return saved;
    }

    public static void deserialize(JSONArray saved) {
        FRIENDS.clear();
        if (saved != null) {
            for (int i = 0; i < saved.length(); i++) {
                String name = saved.optString(i, "");
                if (!name.isEmpty()) {
                    FRIENDS.add(name);
                }
            }
        }
    }
}
