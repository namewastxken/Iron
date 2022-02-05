package com.hylandermc.iron.commands.warp;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import com.hylandermc.iron.managers.WarpManager;
import com.hylandermc.iron.objects.Warp;
import com.hylandermc.iron.objects.WarpType;
import com.hylandermc.iron.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {

    private Iron plugin;
    private WarpManager warpManager;
    private FileConfiguration config;
    public SetWarpCommand(Iron instance) {
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

        if(!p.hasPermission("iron.admin")) {
            p.sendMessage("§cNo Permission.");
            return true;
        }

        if(args.length != 2) {
            p.sendMessage("§c/setwarp <warp> <permission:mine>");
            return true;
        }

        String warpName = args[0];
        Warp warp = warpManager.getWarp(warpName);

        try {
            if(WarpType.valueOf(args[1].toUpperCase()) == WarpType.MINE) {
                if(!RankManager.isRank(warpName)) {
                    p.sendMessage("§cThe warp you are trying to set is not a valid prison rank.");
                    return true;
                }
            }

            if(warp == null) {
                warpManager.setWarp(warpName, p.getLocation(), WarpType.valueOf(args[1].toUpperCase()));
                p.sendMessage(ConfigUtil.getColoredMessage("messages.created-warp")
                        .replaceAll("%warp_type%", WarpType.valueOf(args[1].toUpperCase()).name().toLowerCase())
                        .replaceAll("%warp_name%", warpName));
                return true;
            } else {
                p.sendMessage("§cWarp already exists.");
                return true;
            }
        } catch (IllegalArgumentException ex) {
            p.sendMessage("§cYou have entered an invalid warp type, please input whether it is a §fmine §cor §fpermission §cwarp.");
            p.sendMessage("§bMine Type - §7A mine warp type is a warp to a prison rank's mine.");
            p.sendMessage("§bPermission Type - §7A permission warp type is a warp that you would need a permission to.");
            return true;
        }
    }
}