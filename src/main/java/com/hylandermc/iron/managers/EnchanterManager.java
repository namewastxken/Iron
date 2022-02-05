package com.hylandermc.iron.managers;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.util.ConfigUtil;
import com.hylandermc.iron.util.NumberFormat;
import com.vk2gpz.tokenenchant.api.CEHandler;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class EnchanterManager {

    private Iron plugin;
    private FileConfiguration config;
    public EnchanterManager(Iron instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
    }

    public Inventory getEnchanter() {
        Inventory inv = Bukkit.createInventory(null, config.getInt("enchanter-inventory.size"), ConfigUtil.getColoredMessage("enchanter-inventory.title"));
        inv.clear();

        for(int i = 0; i < inv.getSize(); i++) {

            ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) DyeColor.LIGHT_BLUE.ordinal());
            ItemMeta meta = filler.getItemMeta();
            meta.setDisplayName("§a");
            filler.setItemMeta(meta);
            inv.setItem(i, filler);
        }

        for(String enchant : config.getConfigurationSection("enchants").getKeys(false)) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("%cost_per_level%", NumberFormat.createString(config.getInt("enchants." + enchant + ".price-per-level")));
            variables.put("%left_click_amount%", config.getInt("enchant-amounts.left-click"));
            variables.put("%right_click_amount%", config.getInt("enchant-amounts.right-click"));
            variables.put("%shift_click_amount%", config.getInt("enchant-amounts.shift-click"));
            inv.setItem(config.getInt("enchants." + enchant + ".display-item.slot"), ConfigUtil.getItemFromConfig("enchants." + enchant + ".display-item", variables));
        }

        return inv;
    }

    public String getEnchantFromSlot(int slot) {
        for(String enchant : config.getConfigurationSection("enchants").getKeys(false)) {
            if(config.getInt("enchants." + enchant + ".display-item.slot") == slot) {
                return config.getString("enchants." + enchant + ".enchant-name");
            }
        }
        return "none";
    }

    public boolean hasConflicts(String enchant, ItemStack item) {
        if(TokenEnchantAPI.getInstance().getEnchantments(item).isEmpty() || TokenEnchantAPI.getInstance().getEnchantment(enchant) == null) {
            return false;
        }
        for(CEHandler ench : TokenEnchantAPI.getInstance().getEnchantments(item).keySet()) {
            if(TokenEnchantAPI.getInstance().getEnchantment(enchant).hasConflictWith(ench.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getConflictingEnchant(String enchant, ItemStack item) {
        if(TokenEnchantAPI.getInstance().getEnchantments(item).isEmpty() || TokenEnchantAPI.getInstance().getEnchantment(enchant) == null) {
            return null;
        }
        for(CEHandler ench : TokenEnchantAPI.getInstance().getEnchantments(item).keySet()) {
            if(TokenEnchantAPI.getInstance().getEnchantment(enchant).hasConflictWith(ench.getName())) {
                return ench.getDisplayName();
            }
        }
        return null;
    }
}
