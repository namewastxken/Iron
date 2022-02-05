package com.hylandermc.iron.commands.warp;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.WarpManager;
import com.hylandermc.iron.objects.Warp;
import com.hylandermc.iron.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private Iron plugin;
    private WarpManager warpManager;
    private FileConfiguration config;
    public WarpCommand(Iron instance) {
        this.plugin = instance;
        this.warpManager = plugin.getWarpManager();
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;

        if(args.length != 1) {
            p.sendMessage(ConfigUtil.getColoredMessage("messages.warp-list")
                    .replaceAll("%warps%", warpManager.getAllowedWarps(p).toString()
                            .replaceAll("\\[", "")
                            .replaceAll("\\]", "").trim()));
            return true;
        }

        String warpName = args[0];
        Warp warp = warpManager.getWarp(warpName);
        if(warp == null) {
            p.sendMessage(ConfigUtil.getColoredMessage("messages.invalid-warp"));
            return true;
        }

        if(!warp.canWarp(p)) {
            p.sendMessage("§cNo Permission.");
            return true;
        }

        p.teleport(warp.getLocation());
        p.sendMessage(ConfigUtil.getColoredMessage("messages.warping-to-warp").replaceAll("%name%", warp.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = (Player) sender;

        if(warpManager.getAllowedWarps(player).contains("No warps have been set.")) {
            return null;
        }
        final List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], warpManager.getAllowedWarps(player), completions);
        Collections.sort(completions);
        return completions;
    }
}
