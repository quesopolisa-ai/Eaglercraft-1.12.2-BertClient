package baseclient.gg.dhyeybg.baseclient.module.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import baseclient.gg.dhyeybg.baseclient.BaseClient;
import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.client.gui.ScaledResolution;

public class Waypoints extends Module {
    private static final List<Waypoint> WAYPOINTS = new ArrayList<Waypoint>();

    public Waypoints() {
        super("Waypoints", "Saves and displays named locations", Category.UTILITIES);
        visible = false;
    }

    public static boolean handleChatCommand(String message) {
        if (message == null || !message.startsWith("!wp")) {
            return false;
        }
        String[] parts = message.trim().split("\\s+", 3);
        if (parts.length < 2 || mc == null || mc.player == null || mc.world == null) {
            showHelp();
            return true;
        }

        String action = parts[1].toLowerCase();
        if ("add".equals(action) && parts.length == 3) {
            add(parts[2].trim());
        } else if ("remove".equals(action) && parts.length == 3) {
            remove(parts[2].trim());
        } else if ("list".equals(action)) {
            list();
        } else {
            showHelp();
        }
        return true;
    }

    private static void add(String name) {
        if (name.isEmpty()) {
            showHelp();
            return;
        }
        for (Waypoint waypoint : WAYPOINTS) {
            if (waypoint.name.equalsIgnoreCase(name) && waypoint.dimension == mc.player.dimension) {
                waypoint.x = mc.player.posX;
                waypoint.y = mc.player.posY;
                waypoint.z = mc.player.posZ;
                waypoint.dimension = mc.player.dimension;
                save();
                mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString("Updated waypoint " + name));
                return;
            }
        }
        WAYPOINTS.add(new Waypoint(name, mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.dimension));
        save();
        mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString("Added waypoint " + name));
    }

    private static void remove(String name) {
        Iterator<Waypoint> iterator = WAYPOINTS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().name.equalsIgnoreCase(name)) {
                iterator.remove();
                save();
                mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString("Removed waypoint " + name));
                return;
            }
        }
        mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString("Waypoint not found: " + name));
    }

    private static void list() {
        if (WAYPOINTS.isEmpty()) {
            mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString("No saved waypoints."));
            return;
        }
        for (Waypoint waypoint : WAYPOINTS) {
            mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString(waypoint.toString()));
        }
    }

    private static void showHelp() {
        if (mc != null && mc.player != null) {
            mc.player.addChatMessage(new net.minecraft.util.text.TextComponentString(
                    "Waypoints: !wp add <name>, !wp list, !wp remove <name>"));
        }
    }

    private static void save() {
        if (BaseClient.instance != null) {
            BaseClient.instance.saveConfig();
        }
    }

    public static JSONArray serialize() throws JSONException {
        JSONArray serialized = new JSONArray();
        for (Waypoint waypoint : WAYPOINTS) {
            JSONObject object = new JSONObject();
            object.put("name", waypoint.name);
            object.put("x", waypoint.x);
            object.put("y", waypoint.y);
            object.put("z", waypoint.z);
            object.put("dimension", waypoint.dimension);
            serialized.put(object);
        }
        return serialized;
    }

    public static void deserialize(JSONArray serialized) throws JSONException {
        WAYPOINTS.clear();
        if (serialized == null) {
            return;
        }
        for (int i = 0; i < serialized.length(); i++) {
            JSONObject object = serialized.optJSONObject(i);
            if (object == null || !object.has("name")) {
                continue;
            }
            WAYPOINTS.add(new Waypoint(object.getString("name"), object.getDouble("x"),
                    object.getDouble("y"), object.getDouble("z"), object.getInt("dimension")));
        }
    }

    @Override
    public void onRender2D() {
        if (mc == null || mc.player == null || mc.fontRendererObj == null) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        int y = 24;
        for (Waypoint waypoint : WAYPOINTS) {
            if (waypoint.dimension != mc.player.dimension) {
                continue;
            }
            int distance = (int) mc.player.getDistance(waypoint.x, waypoint.y, waypoint.z);
            String line = waypoint.name + " [" + (int) waypoint.x + ", " + (int) waypoint.y + ", "
                    + (int) waypoint.z + "] " + distance + "m";
            mc.fontRendererObj.drawStringWithShadow(line,
                    resolution.getScaledWidth() - mc.fontRendererObj.getStringWidth(line) - 2, y, 0xFFFFD45C);
            y += 10;
        }
    }

    private static final class Waypoint {
        private final String name;
        private double x;
        private double y;
        private double z;
        private int dimension;

        private Waypoint(String name, double x, double y, double z, int dimension) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
        }

        @Override
        public String toString() {
            return name + " (" + (int) x + ", " + (int) y + ", " + (int) z + ") dimension " + dimension;
        }
    }
}
