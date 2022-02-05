package com.hylandermc.iron.listeners;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.util.ConfigUtil;
import com.hylandermc.iron.util.NumberFormat;
import com.hylandermc.iron.managers.EnchanterManager;
import com.vk2gpz.tokenenchant.TokenEnchant;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnchanterInventoryListener implements Listener {

    private Iron plugin;
    private FileConfiguration config;
    public EnchanterInventoryListener(Iron instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onShiftClickPick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        EnchanterManager enchanterManager = new EnchanterManager(plugin);
        if(!(p.getItemInHand().getType().equals(Material.DIAMOND_PICKAXE)
                || p.getItemInHand().getType().equals(Material.IRON_PICKAXE)
                || p.getItemInHand().getType().equals(Material.GOLD_PICKAXE)
                || p.getItemInHand().getType().equals(Material.WOOD_PICKAXE)
                || p.getItemInHand().getType().equals(Material.STONE_PICKAXE))) {
            return;
        }

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if(p.isSneaking()) {
                p.openInventory(enchanterManager.getEnchanter());
                p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 3);
                return;
            }
        }
    }

    @EventHandler
    public void onEnchanterClick(InventoryClickEvent e) {
        EnchanterManager enchanterManager = new EnchanterManager(plugin);
        if (e.getInventory() == null || e.getClickedInventory() == null) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        Inventory enchanter = enchanterManager.getEnchanter();

        if (!player.getOpenInventory().getTopInventory().getTitle().equalsIgnoreCase(enchanter.getTitle())) {
            return;
        }

        e.setCancelled(true);

        String enchant = enchanterManager.getEnchantFromSlot(e.getSlot());

        if(enchant.equalsIgnoreCase("none")) {
            return;
        }

        double pricePerLevel = config.getDouble("enchants." + enchant + ".price-per-level");

        int leftClickLevels = config.getInt("enchant-amounts.left-click");
        int rightClickLevels = config.getInt("enchant-amounts.right-click");
        int shiftClickLevels = config.getInt("enchant-amounts.shift-click");


        if(enchanterManager.hasConflicts(enchant, player.getItemInHand())) {
            player.sendMessage(ConfigUtil.getColoredMessage("messages.has-conflicting-enchant")
            .replaceAll("%enchant%", TokenEnchantAPI.getInstance().getEnchantment(enchant).getDisplayName())
            .replaceAll("%conflict%", enchanterManager.getConflictingEnchant(enchant, player.getItemInHand())));
            return;
        }

        if(e.getClick() == ClickType.LEFT) {
            double leftClickPrice = pricePerLevel * leftClickLevels;
            if(TokenEnchantAPI.getInstance().getEnchantments(player.getItemInHand()).get(TokenEnchantAPI.getInstance().getEnchantment(enchant)) != null) {
                int currentLevel = TokenEnchantAPI.getInstance().getEnchantments(player.getItemInHand()).get(TokenEnchantAPI.getInstance().getEnchantment(enchant));
                if(TokenEnchantAPI.getInstance().getEnchantment(enchant).getMax() < (currentLevel + leftClickLevels)) {
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.would-exceed-max-enchant"));
                    return;
                }
            } else {
                if(TokenEnchantAPI.getInstance().getEnchantment(enchant) != null) {
                    if (TokenEnchantAPI.getInstance().getEnchantment(enchant).getMax() < leftClickLevels) {
                        player.sendMessage(ConfigUtil.getColoredMessage("messages.would-exceed-max-enchant"));
                        return;
                    }
                }
            }

            if(TokenEnchantAPI.getInstance().getTokens(player) >= leftClickPrice) {
                ItemStack enchanted = TokenEnchant.getInstance().enchant(player, player.getItemInHand(), enchant, leftClickLevels, true, leftClickPrice, false);
                if(enchanted != null) {
                    player.setItemInHand(enchanted);
                    player.updateInventory();
                    /*
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.enchant-successful")
                    .replaceAll("%level%", String.valueOf(leftClickLevels))
                    .replaceAll("%enchant%", enchant)
                    .replaceAll("%price%", NumberFormat.createString(leftClickPrice))
                    .replaceAll("%cost%", NumberFormat.createString(leftClickPrice)));
                     */
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1,1);
                    //Iron.getEcon().withdrawPlayer(player, leftClickPrice);
                    return;
                } else {
                    return;
                }
            } else {
                player.sendMessage(ConfigUtil.getColoredMessage("messages.insufficient-funds-to-enchant")
                .replaceAll("%money%", NumberFormat.createString(leftClickPrice)));
                return;
            }
        }

        if(e.getClick() == ClickType.RIGHT) {
            double rightClickPrice = pricePerLevel * rightClickLevels;
            if(TokenEnchantAPI.getInstance().getEnchantments(player.getItemInHand()).get(TokenEnchantAPI.getInstance().getEnchantment(enchant)) != null) {
                int currentLevel = TokenEnchantAPI.getInstance().getEnchantments(player.getItemInHand()).get(TokenEnchantAPI.getInstance().getEnchantment(enchant));
                if(TokenEnchantAPI.getInstance().getEnchantment(enchant).getMax() < (currentLevel + rightClickLevels)) {
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.would-exceed-max-enchant"));
                    return;
                }
            } else {
                if(TokenEnchantAPI.getInstance().getEnchantment(enchant) != null) {
                    if (TokenEnchantAPI.getInstance().getEnchantment(enchant).getMax() < rightClickLevels) {
                        player.sendMessage(ConfigUtil.getColoredMessage("messages.would-exceed-max-enchant"));
                        return;
                    }
                }
            }
            if(TokenEnchantAPI.getInstance().getTokens(player) >= rightClickPrice) {
                ItemStack enchanted = TokenEnchant.getInstance().enchant(player, player.getItemInHand(), enchant, rightClickLevels, true, rightClickPrice, false);
                if(enchanted != null) {
                    player.setItemInHand(enchanted);
                    player.updateInventory();
                    /*
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.enchant-successful")
                            .replaceAll("%level%", String.valueOf(rightClickLevels))
                            .replaceAll("%enchant%", enchant)
                            .replaceAll("%price%", NumberFormat.createString(rightClickPrice))
                            .replaceAll("%cost%", NumberFormat.createString(rightClickPrice)));
                     */
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1,1);
                    //Iron.getEcon().withdrawPlayer(player, rightClickPrice);
                    return;
                } else {
                    return;
                }
            } else {
                player.sendMessage(ConfigUtil.getColoredMessage("messages.insufficient-funds-to-enchant")
                        .replaceAll("%money%", NumberFormat.createString(rightClickPrice)));
                return;
            }
        }

        if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
            double shiftClickPrice = pricePerLevel * shiftClickLevels;

            if(TokenEnchantAPI.getInstance().getEnchantments(player.getItemInHand()).get(TokenEnchantAPI.getInstance().getEnchantment(enchant)) != null) {
                int currentLevel = TokenEnchantAPI.getInstance().getEnchantments(player.getItemInHand()).get(TokenEnchantAPI.getInstance().getEnchantment(enchant));
                if(TokenEnchantAPI.getInstance().getEnchantment(enchant).getMax() < (currentLevel + shiftClickLevels)) {
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.would-exceed-max-enchant"));
                    return;
                }
            } else {
                if(TokenEnchantAPI.getInstance().getEnchantment(enchant) != null) {
                    if (TokenEnchantAPI.getInstance().getEnchantment(enchant).getMax() < shiftClickLevels) {
                        player.sendMessage(ConfigUtil.getColoredMessage("messages.would-exceed-max-enchant"));
                        return;
                    }
                }
            }

            if(TokenEnchantAPI.getInstance().getTokens(player) >= shiftClickPrice) {
                ItemStack enchanted = TokenEnchant.getInstance().enchant(player, player.getItemInHand(), enchant, shiftClickLevels, true, shiftClickPrice, false); // v = price
                if(enchanted != null) {
                    player.setItemInHand(enchanted);
                    player.updateInventory();
                    /*
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.enchant-successful")
                            .replaceAll("%level%", String.valueOf(shiftClickLevels))
                            .replaceAll("%enchant%", enchant)
                            .replaceAll("%price%", NumberFormat.createString(shiftClickPrice))
                            .replaceAll("%cost%", NumberFormat.createString(shiftClickPrice)));

                     */
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1,1);
                    //Iron.getEcon().withdrawPlayer(player, shiftClickPrice);
                    return;
                } else {
                    return;
                }
            } else {
                player.sendMessage(ConfigUtil.getColoredMessage("messages.insufficient-funds-to-enchant")
                        .replaceAll("%money%", NumberFormat.createString(shiftClickPrice)));
                return;
            }
        }
    }

}
