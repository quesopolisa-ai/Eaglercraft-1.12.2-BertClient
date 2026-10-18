package baseclient.gg.dhyeybg.baseclient.module.render;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ChatTimestamps extends Module {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.ROOT);

    public ChatTimestamps() {
        super("Chat Timestamps", "Adds local timestamps to chat messages", Category.RENDER);
    }

    public static ITextComponent addTimestamp(ITextComponent message) {
        TextComponentString prefix = new TextComponentString("[" + TIME_FORMAT.format(new Date()) + "] ");
        prefix.getStyle().setColor(TextFormatting.GRAY);
        prefix.appendSibling(message.createCopy());
        return prefix;
    }
}
