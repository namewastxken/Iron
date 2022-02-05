package com.hylandermc.iron.listeners;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.events.TokenMineEvent;
import com.hylandermc.iron.managers.RankManager;
import com.hylandermc.iron.util.ConfigUtil;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import me.clip.autosell.events.AutoSellEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PlayerListeners implements Listener {

    private Iron plugin;
    private FileConfiguration config;
    public PlayerListeners(Iron plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handlePlayerDocument(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        RankManager rankManager = new RankManager(plugin, player);
        if(rankManager.getPlayerDocument() == null) {
            rankManager.setupPlayerDocument();
        }
        if(plugin.getMongoManager().getRankCache().containsKey(player)) {
            if(rankManager.getPlayerDocument().getString("rank").equalsIgnoreCase(plugin.getMongoManager().getRankCache().get(player))) {
                return;
            }
            plugin.getMongoManager().getRankCache().remove(player);
        }

        if(plugin.getMongoManager().getPrestigeCache().containsKey(player)) {
            if(rankManager.getPlayerDocument().getInteger("prestige").equals(plugin.getMongoManager().getPrestigeCache().get(player))) {
                return;
            }
            plugin.getMongoManager().getPrestigeCache().remove(player);
        }
    }

    @EventHandler
    public void onTokenMine(BlockBreakEvent e) {
        if(config.getBoolean("give-tokens-on-mine")) {
           // TokenEnchantAPI.getInstance().addTokens(e.getPlayer(), config.getInt("token-amount-per-block-mined"));
            Bukkit.getPluginManager().callEvent(new TokenMineEvent(e.getPlayer(), config.getInt("token-amount-per-block-mined")));
        }
    }

    @EventHandler
    public void onTokenExplodeEvent(TEBlockExplodeEvent e) {
        if(config.getBoolean("give-tokens-on-mine")) {
            // TokenEnchantAPI.getInstance().addTokens(e.getPlayer(), config.getInt("token-amount-per-block-mined"));
            Bukkit.getPluginManager().callEvent(new TokenMineEvent(e.getPlayer(), config.getInt("token-amount-per-block-mined")));
        }
    }

    @EventHandler
    public void onTokenMineEvent(TokenMineEvent e) {
        if(!e.isCancelled()) {
            TokenEnchantAPI.getInstance().addTokens(e.getPlayer(), e.getTokensGiven());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent e) {
        if(config.getBoolean("disable-item-damage")) {
            Player p = e.getPlayer();
            e.setCancelled(true);
            e.setDamage(0);
            ItemStack item = e.getItem();
            ItemMeta meta = item.getItemMeta();
            if(!meta.spigot().isUnbreakable()) {
                meta.spigot().setUnbreakable(true);
               // meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        }
    }

    @EventHandler
    public void onAutoSell(AutoSellEvent e) {

    }

    @EventHandler
    public void antiDupe(CraftItemEvent e) {
        if(e.getCurrentItem().getType() == Material.BOOK_AND_QUILL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRankInventoryClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null || e.getInventory() == null) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        if(e.getClickedInventory().getTitle().equalsIgnoreCase(ConfigUtil.getColoredMessage("rank-inventory.title")) || p.getOpenInventory().getTitle().equalsIgnoreCase(ConfigUtil.getColoredMessage("rank-inventory.title"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJump(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (e.getTo().getX() == e.getFrom().getX()
                && e.getTo().getY() > e.getFrom().getY()
                && e.getFrom().getZ() == e.getTo().getZ()) {
            Location check = new Location(p.getWorld(), p.getLocation().getX(), e.getFrom().getY() - 1, p.getLocation().getZ());
            if (p.getWorld().getBlockAt(check).getType() == Material.SPONGE) {
                new BukkitRunnable() {
                    public void run() {
                        try {
                            p.setVelocity(new Vector(0, 3.75, 0));
                        } catch (Exception err) {
                            return;
                        }
                    }
                }.runTaskAsynchronously(plugin);
                p.playSound(p.getLocation(), Sound.BAT_TAKEOFF, 1, 1);

            }
        }
    }




}
