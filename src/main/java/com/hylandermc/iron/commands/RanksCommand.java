package com.hylandermc.iron.commands;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RanksCommand implements CommandExecutor {

    private Iron plugin;
    public RanksCommand(Iron instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        RankManager rankManager = new RankManager(plugin, p);
        p.openInventory(rankManager.getRankInventory());
        return true;
    }
}
