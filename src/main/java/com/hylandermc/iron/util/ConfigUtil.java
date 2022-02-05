package com.hylandermc.iron.util;

import com.google.common.base.Strings;
import com.hylandermc.iron.Iron;
import de.beproud.scoreboard.ScoreboardText;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigUtil {

    private static Iron plugin = Iron.getInstance();

    public static String getColoredMessage(String configPath) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(configPath));
    }

    public static List<ScoreboardText> getScoreboardLines(Player player, Map<String, Object> variables) {
        List<ScoreboardText> lines = new ArrayList<>();

        if(plugin == null) {
            Bukkit.broadcastMessage("plugin is null");
        }

        for (String string : getColoredList("scoreboard-lines", variables)) {
            lines.add(new ScoreboardText(PlaceholderAPI.setPlaceholders(player, string.replaceAll("&", "§"))));
        }
        return lines;
    }

    public static List<String> getColoredList(String configPath, Map<String, Object> variables) {
        List<String> list = new ArrayList<>();
        if(variables == null) {
            for (String string : plugin.getConfig().getStringList(configPath)) {
                list.add(string.replaceAll("&", "§"));
            }
        } else {
            for (String string : plugin.getConfig().getStringList(configPath)) {
                String updatedString = string;
                for(String variable : variables.keySet()) {
                    updatedString = updatedString.replaceAll("&", "§").replaceAll(variable, String.valueOf(variables.get(variable)));
                }
                list.add(updatedString);
            }
        }
        return list;
    }

    public static String getProgressBar(int current, int max, int totalBars, String symbol, ChatColor completedColor, ChatColor notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat("" + completedColor + symbol, progressBars)
                + Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars);
    }

    public static String getProgressBar(long current, long max, int totalBars, String symbol, ChatColor completedColor, ChatColor notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat("" + completedColor + symbol, progressBars)
                + Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars);
    }

    public static ItemStack getItemFromConfig(String configPath, Map<String, Object> variables) {
        ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString(configPath + ".item")), 1, (short) plugin.getConfig().getInt(configPath + ".data"));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfig().getString(configPath + ".name").replaceAll("&", "§"));
        meta.setLore(getColoredList(configPath + ".lore", variables));
        item.setItemMeta(meta);
        return item;
    }

    public static String getPercentage(double amount, double total) {
        double complete = ((amount/total) * 100);
        if(complete >= 100) {
            return "100";
        }
        return String.valueOf(new DecimalFormat("##").format(complete));
    }

    public static String niceMaterialName(String string) {
        String[] words = string.toLowerCase().split("_");

        StringBuilder name = new StringBuilder();

        for (String x : words) {
            Character first = x.charAt(0);

            name.append(x.replaceFirst(String.valueOf(x.charAt(0)), String.valueOf(Character.toUpperCase(first)))).append(" ");
        }
        return name.toString();
    }

    public static void reloadConfig() {
        plugin.reloadConfig();
    }
}
