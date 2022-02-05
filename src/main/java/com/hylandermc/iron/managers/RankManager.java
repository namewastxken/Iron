package com.hylandermc.iron.managers;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.util.ConfigUtil;
import com.hylandermc.iron.util.NumberFormat;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.eq;

public class RankManager {

    private Iron plugin;
    private Player player;
    private MongoDatabase db;
    private static FileConfiguration ranks;
    private FileConfiguration config;
    private Map<Player, String> rankCache;
    private Map<Player, Integer> prestigeCache;
    public RankManager(Iron plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.db = plugin.getMongoManager().getDatabase();
        this.ranks = plugin.getRanksConfig();
        this.config = plugin.getConfig();
        this.rankCache = plugin.getMongoManager().getRankCache();
        this.prestigeCache = plugin.getMongoManager().getPrestigeCache();
    }

    public void setupPlayerDocument() {
        new BukkitRunnable() {
            public void run() {
                Document document = new Document();
                document.put("uuid", player.getUniqueId().toString());
                document.put("rank", getDefaultRank());
                document.put("prestige", 0);

                db.getCollection("playerdata").insertOne(document);
            }
        }.runTaskAsynchronously(plugin);
    }

    public Document getPlayerDocument() {
        MongoCollection<Document> collection = db.getCollection("playerdata");
        Document document = collection.find(eq("uuid", player.getUniqueId().toString())).first();
        if(document != null) {
            return document;
        }
        return null;
    }

    public String getDefaultRank() {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            if(ranks.getBoolean("ranks.rank" + i + ".defaultRank")) {
                return ranks.getString("ranks.rank" + i + ".name");
            }
            i++;
        }
        return ranks.getString("ranks.rank1.name");
    }

    public String getRank() {
        if(!rankCache.containsKey(player)) {
            rankCache.put(player, getPlayerDocument().getString("rank"));
        }
        return rankCache.get(player);
    }

    public int getRankPosition() {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            if(ranks.getString("ranks.rank" + i + ".name").equalsIgnoreCase(getRank())) {
                return i;
            }
            i++;
        }
        return 1;
    }

    public String getNextRank() {
        return ranks.getString("ranks.rank" + (getRankPosition() + 1) + ".name");
    }

    public double getPrestigeMultiplier() {
        return 1 + (ranks.getDouble("prestige-price-multiplier-per-prestige") * getPrestige());
    }

    public double getRankupPrice() {
        return ranks.getDouble("ranks.rank" + (getRankPosition() + 1) + ".price") * getPrestigeMultiplier();
    }

    public int getPrestige() {
        if(!prestigeCache.containsKey(player)) {
            prestigeCache.put(player, getPlayerDocument().getInteger("prestige"));
        }

        return prestigeCache.get(player);
    }

    public boolean isMaxRank() {
        return getNextRank() == null;
    }

    public boolean setRank(String rank) {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            if(ranks.getString("ranks.rank" + i + ".name").equalsIgnoreCase(rank)) {
                Document document = getPlayerDocument();
                String name = ranks.getString("ranks.rank" + i + ".name");
                if(document != null) {
                    new BukkitRunnable() {
                        public void run() {
                            Document updateInformation = new Document();
                            updateInformation.put("rank", name);
                            rankCache.remove(player);
                            rankCache.put(player, name);
                            Document updateDocument = new Document();
                            updateDocument.put("$set", updateInformation);
                            db.getCollection("playerdata").updateOne(document, updateDocument);
                        }
                    }.runTaskAsynchronously(plugin);
                    return true;
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Unable to find document while trying to set rank.");
                    return false;
                }
            }
            i++;
        }
        return false;
    }

    public boolean setPrestige(int prestige) {
        Document document = getPlayerDocument();
        if(document != null) {
            Document updateInformation = new Document();
            new BukkitRunnable() {
                public void run() {
                    updateInformation.put("prestige", prestige);
                    prestigeCache.replace(player, prestige);
                    Document updateDocument = new Document();
                    updateDocument.put("$set", updateInformation);
                    db.getCollection("playerdata").updateOne(document, updateDocument);
                }
            }.runTaskAsynchronously(plugin);
            return true;
        } else {
            Bukkit.getLogger().log(Level.WARNING, "Unable to find document while trying to set prestige.");
            return false;
        }
    }

    public String getRankNameFromPosition(int position) {
        if(ranks.contains("ranks.rank" + position)) {
            return ranks.getString("ranks.rank" + position + ".name");
        }
        return null;
    }

    public boolean rankup() {
        if(isMaxRank()) {
            player.sendMessage(ConfigUtil.getColoredMessage("messages.max-rank"));
            return false;
        }

        new BukkitRunnable() {
            public void run() {
                int rankupTimes = 0;
                int currentRankPosition = getRankPosition();
                int newRank = getRankPosition();
                double bal = Iron.getEcon().getBalance(player);
                for(int i = getRankPosition() + 1; i < getMaxRankPosition(); i++) {
                    if(bal >= getRankupPriceFromPosition(i)) {
                        bal = bal - getRankupPriceFromPosition(i);
                        rankupTimes++;
                        newRank = i;
                    }
                }

                if(rankupTimes >= 1) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("%rank%", getRankNameFromPosition(newRank));

                    double moneyLost = Iron.getEcon().getBalance(player) - bal;
                    variables.put("%ranks_passed%", rankupTimes);
                    variables.put("%rankup_times%", rankupTimes);
                    variables.put("%rankup_amount%", rankupTimes);
                    variables.put("%rankup_price%", NumberFormat.createString(moneyLost));
                    ConfigUtil.getColoredList("messages.rankup-success", variables).forEach(player::sendMessage);
                    setRank(getRankNameFromPosition(newRank));
                    Iron.getEcon().withdrawPlayer(player, moneyLost);
                    return;
                } else {
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.cannot-rankup").replaceAll("%price%",
                            NumberFormat.createString(getRankupPrice())));
                    return;
                }
                /*
                if(Iron.getEcon().getBalance(player) >= getRankupPrice()) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("%rank%", getNextRank());
                    variables.put("%rankup_price%", getRankupPrice());
                    ConfigUtil.getColoredList("messages.rankup-success", variables).forEach(player::sendMessage);
                    setRank(getNextRank());
                    Iron.getEcon().withdrawPlayer(player, getRankupPrice());
                } else {
                    player.sendMessage(ConfigUtil.getColoredMessage("messages.cannot-rankup").replaceAll("%price%",
                            NumberFormat.createString(getRankupPrice())));
                }
                 */
            }
        }.runTaskAsynchronously(plugin);

        return false;
    }

    public int getRankPosition(String rankName) {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            if(ranks.getString("ranks.rank" + i + ".name").equalsIgnoreCase(rankName)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static boolean isRank(String rankName) {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            if(ranks.getString("ranks.rank" + i + ".name").equalsIgnoreCase(rankName)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public int getMaxRankPosition() {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            i++;
        }
        return i;
    }

    public double getRankupPriceFromPosition(int position) {
        if(ranks.contains("ranks.rank" + position)) {
            return ranks.getDouble("ranks.rank" + position + ".price") * getPrestigeMultiplier();
        }
        return -1;
    }

    public void prestige() {
        int nextPrestige = getPrestige() + 1;
        setRank(getRankNameFromPosition(1));
        setPrestige(nextPrestige);
        player.sendMessage(ConfigUtil.getColoredMessage("messages.successful-prestige")
                .replaceAll("%prestige%", String.valueOf(nextPrestige))
                .replaceAll("%multiplier%", String.valueOf(getPrestigeMultiplier() + ranks.getDouble("prestige-price-multiplier-per-prestige"))));
    }

    public int getMaxPrestige() {
        return ranks.getInt("max-prestige");
    }

    public boolean isMaxPrestige() {
        return ranks.getInt("max-prestige") == getPrestige();
    }

    private List<Integer> sideSlots = new ArrayList<>();
    private void addBordertoInventory(Inventory inv, ItemStack item) {
        int size = inv.getSize();
        int rows = size / 9;

        if(rows >= 3) {
            for (byte i = 0; i <= 8; i++) {
                inv.setItem(i, item);

                sideSlots.add((int) i);
            }

            for(byte s = 8; s < (inv.getSize() - 9); s += 9) {
                byte lastSlot = (byte) (s + 1);
                inv.setItem(s, item);
                inv.setItem(lastSlot, item);

                sideSlots.add((int) s);
                sideSlots.add((int) lastSlot);
            }

            for (byte lr = (byte) (inv.getSize() - 9); lr < inv.getSize(); lr++) {
                inv.setItem(lr, item);

                sideSlots.add((int) lr);
            }
        }
    }
    private int getContents(Inventory inventory) {
        int contents = 0;
        for(ItemStack item : inventory.getContents()) {
            if(item != null) {
                contents++;
            }
        }
        return contents;
    }

    private Inventory niceifyInventory(Inventory inventory) {

        int size = 27;
        int contents = getContents(inventory);

        if(contents > 7) {
            size = 36;
        }

        if(contents > 14) {
            size = 45;
        }

        if(contents > 21) {
            size = 54;
        }

        if(contents> 28) {
            size = 54;
        }

        Inventory inv = Bukkit.createInventory(null, size, inventory.getTitle());

        ItemStack border = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) DyeColor.BLACK.ordinal());
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName("§a");
        border.setItemMeta(meta);

        addBordertoInventory(inv, border);

        for(ItemStack item : inventory.getContents()) {
            if(item != null) {
                inv.addItem(item);
            }
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closedMeta = close.getItemMeta();
        closedMeta.setDisplayName("§c§lClose Menu");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Click to return to the");
        lore.add("§7skills main menu.");
        closedMeta.setLore(lore);
        close.setItemMeta(closedMeta);

       // inv.setItem(size - 5, close);
        return inv;
    }
    public Inventory getRankInventory() {
        Inventory inventory = Bukkit.createInventory(null, 45, ConfigUtil.getColoredMessage("rank-inventory.title"));

        for(int i = 1; i <= getRankPosition(); i++) {
            ItemStack item = new ItemStack(Material.getMaterial(config.getString("rank-inventory.completed-rank.item")), 1, (short) config.getInt("rank-inventory.completed-rank.data"));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ConfigUtil.getColoredMessage("rank-inventory.completed-rank.name")
            .replaceAll("%rank%", getRankNameFromPosition(i) != null ? getRankNameFromPosition(i) : "N/A"));
            Map<String, Object> variables = new HashMap<>();
            variables.put("%rank%", getRankNameFromPosition(i) != null ? getRankNameFromPosition(i) : "N/A");
            meta.setLore(ConfigUtil.getColoredList("rank-inventory.completed-rank.lore", variables));
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        if(!isMaxRank()) {
            for(int i = getRankPosition() + 1; i <= getMaxRankPosition(); i++) {
                if(getRankupPriceFromPosition(i) == -1) {
                    continue;
                }
                ItemStack item = new ItemStack(Material.getMaterial(config.getString("rank-inventory.locked-rank.item")), 1, (short) config.getInt("rank-inventory.locked-rank.data"));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ConfigUtil.getColoredMessage("rank-inventory.locked-rank.name")
                        .replaceAll("%rank%", getRankNameFromPosition(i) != null ? getRankNameFromPosition(i) : "N/A"));
                Map<String, Object> variables = new HashMap<>();
                variables.put("%rank%", getRankNameFromPosition(i) != null ? getRankNameFromPosition(i) : "N/A");
                variables.put("%balance%", NumberFormat.createString(Iron.getEcon().getBalance(player)));
                variables.put("%rankup_price%", NumberFormat.createString(getRankupPriceFromPosition(i)));
                variables.put("%percentage_complete%", ConfigUtil.getPercentage(Long.parseLong(plugin.getBigBalance(player)), getRankupPriceFromPosition(i)));
                meta.setLore(ConfigUtil.getColoredList("rank-inventory.locked-rank.lore", variables));
                item.setItemMeta(meta);
                inventory.addItem(item);
            }
        }

        return niceifyInventory(inventory);
    }

    public static void setRankPrice(String rank, BigInteger price) {
        int i = 1;
        while(ranks.contains("ranks.rank" + i)) {
            if(ranks.getString("ranks.rank" + i + ".name").equalsIgnoreCase(rank)) {
                ranks.set("ranks.rank" + i + ".price", price);
                Iron.getInstance().saveRanksConfig();
                Iron.getInstance().reloadConfigs();
            }
            i++;
        }
    }

}