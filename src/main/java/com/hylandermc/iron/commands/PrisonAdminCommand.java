package com.hylandermc.iron.commands;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import com.hylandermc.iron.util.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigInteger;

public class PrisonAdminCommand implements CommandExecutor {
    private Iron plugin;
    public PrisonAdminCommand(Iron plugin) {
        this.plugin = plugin;
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

        if(args.length == 0) {
            p.sendMessage("§b/iron reload §7- reload config");
            p.sendMessage("§b/iron setrank <player> <rank> §7- updates a player's prison rank.");
            p.sendMessage("§b/iron setprestige <player> <prestige>  §7- update's a player's prestige.");
            p.sendMessage("§b/iron setprice <prison rank> <price> §7- sets a prison rank's rankup price.");
            p.sendMessage("§b/iron settokensperblock <amount> §7- sets the amount of tokens found from mining a  single block.");
            p.sendMessage("§b/enchanter setprice <enchant> <price> §7- sets the token price of an enchantment in the enchanter.");
            return true;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfigs();
                p.sendMessage("§aReloaded Iron configuration.");
                return true;
            }
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("settokenperblock") || args[0].equalsIgnoreCase("settokensperblock")) {
                try {
                    int amt = Integer.parseInt(args[1]);
                    if(amt < 0) {
                        p.sendMessage("Please set a value that is greater than or equal to zero.");
                        return true;
                    }

                    plugin.getConfig().set("token-amount-per-block-mined", amt);
                    plugin.saveConfig();
                    p.sendMessage("§fSuccessfully set the amount of tokens gained from mining a block to §b" + amt);
                    return true;
                } catch (NumberFormatException exception) {
                    p.sendMessage("§cPlease input a valid number");
                    return true;
                }
            }
        }

        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("setrank")) {
                Player target = Bukkit.getPlayer(args[1]);
                if(target == null) {
                    p.sendMessage("§cPlayer not found.");
                    return true;
                }

                RankManager rankManager = new RankManager(plugin, target);
                if(!RankManager.isRank(args[2])) {
                    p.sendMessage("§cThat is not a valid mine rank, please try again.");
                    return true;
                }

                rankManager.setRank(args[2]);
                p.sendMessage("§bYou have successfully set §f" + target.getName() + "§b's prison rank to: §f" + args[2]);
                target.sendMessage("§bYour prison rank has been updated. Your new prison rank is: §f" + args[2]);
                return true;
            }

            if(args[0].equalsIgnoreCase("setprestige")) {
                Player target = Bukkit.getPlayer(args[1]);
                if(target == null) {
                    p.sendMessage("§cPlayer not found.");
                    return true;
                }

                try {
                    int prestige = Integer.parseInt(args[2]);
                    if(prestige >= 0) {
                        RankManager rankManager = new RankManager(plugin, target);
                        rankManager.setPrestige(Integer.parseInt(args[2]));
                        p.sendMessage("§bYou have successfully set §f" + target.getName() + "§b's prestige to: §f" + args[2]);
                        target.sendMessage("§bYour prestige has been updated. Your new prestige is: §f" + args[2]);
                        return true;
                    } else {
                        p.sendMessage("§cPrestige has to be positive.");
                        return true;
                    }
                } catch (NumberFormatException er) {
                    p.sendMessage("§cInvalid number.");
                    return true;
                }
            }

            if(args[0].equalsIgnoreCase("setprice")) {
                String rank = args[1];

                if(!RankManager.isRank(rank)) {
                    p.sendMessage("§cThat is not a valid prison rank");
                    return true;
                }

                try {
                    BigInteger price = BigInteger.valueOf(Long.parseLong(args[2]));
                    RankManager.setRankPrice(rank, price);
                    p.sendMessage("§bUpdated rankup price for §f" + args[1] + " §bto: §f$" + NumberFormat.createString(Long.parseLong(args[2])));
                } catch (NumberFormatException e) {
                    p.sendMessage("§cInput valid number");
                    return true;
                }
            }
        }

        return true;
    }
}
