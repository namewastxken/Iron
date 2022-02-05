package com.hylandermc.iron.commands;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import com.hylandermc.iron.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PrestigeCommand implements CommandExecutor {

    private Iron plugin;
    private FileConfiguration config;
    public PrestigeCommand(Iron instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;
        RankManager rankManager = new RankManager(plugin, p);

        if(!rankManager.isMaxRank()) {
            p.sendMessage(ConfigUtil.getColoredMessage("messages.cannot-prestige"));
            return true;
        }

        if(rankManager.isMaxPrestige()) {
            p.sendMessage(ConfigUtil.getColoredMessage("messages.max-prestige"));
            return true;
        }
        rankManager.prestige();
        p.performCommand("spawn");
        return true;
    }
}
