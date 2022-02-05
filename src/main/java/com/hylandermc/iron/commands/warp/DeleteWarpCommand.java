package com.hylandermc.iron.commands.warp;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.WarpManager;
import com.hylandermc.iron.objects.Warp;
import com.hylandermc.iron.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DeleteWarpCommand implements CommandExecutor {

    private Iron plugin;
    private WarpManager warpManager;
    private FileConfiguration config;
    public DeleteWarpCommand(Iron instance) {
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

        if(args.length != 1) {
            p.sendMessage("§c/delwarp <warpName>");
            return true;
        }

        String warpName = args[0];
        Warp warp = warpManager.getWarp(warpName);
        if(warp == null) {
            p.sendMessage(ConfigUtil.getColoredMessage("messages.invalid-warp"));
            return true;
        }

        warpManager.deleteWarp(warp.getName());
        p.sendMessage(ConfigUtil.getColoredMessage("messages.deleted-warp")
                .replaceAll("%warp_name%", warp.getName()));
        plugin.saveWarps();
        return true;
    }
}
