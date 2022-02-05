package com.hylandermc.iron.commands;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankupCommand implements CommandExecutor {
    private Iron plugin;
    public RankupCommand(Iron plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou cannot do this.");
            return true;
        }

        Player p = (Player) sender;
        RankManager rankManager = new RankManager(plugin, p);

        rankManager.rankup();
        return true;
    }
}
