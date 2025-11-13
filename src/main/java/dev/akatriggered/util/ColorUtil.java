package dev.akatriggered.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String colorize(String text) {
        text = translateHexColors(text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String translateHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
            
            StringBuilder magic = new StringBuilder("&");
            for (char c : replaceSharp.toCharArray()) {
                magic.append('&').append(c);
            }
            
            matcher.appendReplacement(buffer, magic.toString());
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
}
