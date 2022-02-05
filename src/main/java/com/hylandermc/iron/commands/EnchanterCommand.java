package com.hylandermc.iron.commands;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.EnchanterManager;
import com.hylandermc.iron.util.NumberFormat;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class EnchanterCommand implements CommandExecutor {

    private Iron plugin;
    private FileConfiguration config;
    public EnchanterCommand(Iron instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;
        EnchanterManager enchanterManager = new EnchanterManager(plugin);

        if(args.length != 3 || !p.hasPermission("iron.admin")) {
            if(!(p.getItemInHand().getType().equals(Material.DIAMOND_PICKAXE)
                    || p.getItemInHand().getType().equals(Material.IRON_PICKAXE)
                    || p.getItemInHand().getType().equals(Material.GOLD_PICKAXE)
                    || p.getItemInHand().getType().equals(Material.WOOD_PICKAXE)
                    || p.getItemInHand().getType().equals(Material.STONE_PICKAXE))) {
                p.sendMessage("§cYou need to have a pickaxe in your hand to use the enchanter.");
                return true;
            }

            p.openInventory(enchanterManager.getEnchanter());
            p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 3);
        } else {
            if(!args[0].equalsIgnoreCase("setprice")) {
                return true;
            }

            String enchantment = args[1];
            try {
                int price = Integer.parseInt(args[2]);
                for(String enchant : config.getConfigurationSection("enchants").getKeys(false)) {
                    if(config.getString("enchants." + enchant + ".enchant-name").equalsIgnoreCase(enchantment)) {
                        config.set("enchants." + enchant + ".price-per-level", price);
                        plugin.saveConfig();
                        p.sendMessage("§fYou have successfully set the price of §b" + enchant + "§f to §b" + NumberFormat.createString(price) + " §ftokens.");
                        return true;
                    }
                }
                p.sendMessage("§cEnchantment not found in the enchanter.");
                return true;
            } catch (NumberFormatException exception) {
                p.sendMessage("§cPlease use a valid number.");
                return true;
            }
        }
        return true;
    }
}
