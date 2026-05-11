package de.yannik.dreamveilCore.rank.util;

import org.bukkit.ChatColor;

/**
 * Utility for creating color gradients
 */
public class GradientUtil {

    /**
     * Create a gradient string from start to end color
     * Distributes colors evenly across all characters
     */
    public static String applyGradient(String text, ChatColor startColor, ChatColor endColor) {
        if (text == null || text.isEmpty()) return text;

        // If both colors are the same, just apply that color
        if (startColor.equals(endColor)) {
            return startColor + text + ChatColor.RESET;
        }

        // Get RGB values
        int[] startRGB = getRGB(startColor);
        int[] endRGB = getRGB(endColor);

        if (startRGB == null || endRGB == null) {
            // Fallback for colors without RGB
            return startColor + text + ChatColor.RESET;
        }

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            // Calculate progress (0.0 to 1.0)
            double progress = (double) i / (length - 1);

            // Interpolate RGB values
            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * progress);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * progress);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * progress);

            // Get closest ChatColor
            ChatColor color = getClosestChatColor(r, g, b);
            result.append(color).append(text.charAt(i));
        }

        result.append(ChatColor.RESET);
        return result.toString();
    }

    public static String applyGradient(String text, net.md_5.bungee.api.ChatColor startColor, net.md_5.bungee.api.ChatColor endColor) {
        if (text == null || text.isEmpty()) return text;

        if (startColor.equals(endColor)) {
            return startColor + text + net.md_5.bungee.api.ChatColor.RESET;
        }

        java.awt.Color start = startColor.getColor();
        java.awt.Color end = endColor.getColor();

        if (start == null || end == null) {
            return startColor + text;
        }

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            double progress = length == 1 ? 0 : (double) i / (length - 1);

            int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
            int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
            int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);

            net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of(new java.awt.Color(r, g, b));
            result.append(color).append(text.charAt(i));
        }

        result.append(net.md_5.bungee.api.ChatColor.RESET);
        return result.toString();
    }

    /**
     * Get RGB values from ChatColor
     */
    private static int[] getRGB(ChatColor color) {
        return switch (color) {
            case BLACK -> new int[]{0, 0, 0};
            case DARK_BLUE -> new int[]{0, 0, 170};
            case DARK_GREEN -> new int[]{0, 170, 0};
            case DARK_AQUA -> new int[]{0, 170, 170};
            case DARK_RED -> new int[]{170, 0, 0};
            case DARK_PURPLE -> new int[]{170, 0, 170};
            case GOLD -> new int[]{255, 170, 0};
            case GRAY -> new int[]{170, 170, 170};
            case DARK_GRAY -> new int[]{85, 85, 85};
            case BLUE -> new int[]{85, 85, 255};
            case GREEN -> new int[]{85, 255, 85};
            case AQUA -> new int[]{85, 255, 255};
            case RED -> new int[]{255, 85, 85};
            case LIGHT_PURPLE -> new int[]{255, 85, 255};
            case YELLOW -> new int[]{255, 255, 85};
            case WHITE -> new int[]{255, 255, 255};
            default -> null;
        };
    }

    /**
     * Get closest ChatColor to RGB values
     */
    private static ChatColor getClosestChatColor(int r, int g, int b) {
        ChatColor[] colors = {
                ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
                ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE,
                ChatColor.GOLD, ChatColor.GRAY, ChatColor.DARK_GRAY,
                ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA,
                ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW,
                ChatColor.WHITE
        };

        ChatColor closest = ChatColor.WHITE;
        double minDistance = Double.MAX_VALUE;

        for (ChatColor color : colors) {
            int[] rgb = getRGB(color);
            if (rgb == null) continue;

            double distance = Math.sqrt(
                    Math.pow(r - rgb[0], 2) +
                    Math.pow(g - rgb[1], 2) +
                    Math.pow(b - rgb[2], 2)
            );

            if (distance < minDistance) {
                minDistance = distance;
                closest = color;
            }
        }

        return closest;
    }
}