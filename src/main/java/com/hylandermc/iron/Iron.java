package com.hylandermc.iron;

import com.hylandermc.iron.commands.*;
import com.hylandermc.iron.commands.warp.DeleteWarpCommand;
import com.hylandermc.iron.commands.warp.SetWarpCommand;
import com.hylandermc.iron.commands.warp.WarpCommand;
import com.hylandermc.iron.papi.Placeholders;
import com.hylandermc.iron.listeners.EnchanterInventoryListener;
import com.hylandermc.iron.listeners.PlayerListeners;
import com.hylandermc.iron.managers.EnchanterManager;
import com.hylandermc.iron.managers.MongoManager;
import com.hylandermc.iron.managers.WarpManager;
import de.beproud.scoreboard.ScoreboardListener;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Iron extends JavaPlugin {

    private static Iron instance;
    @Getter private MongoManager mongoManager;
    @Getter private FileConfiguration ranksConfig;
    @Getter private FileConfiguration warpsConfig;
    @Getter private static Economy econ = null;
    @Getter private static Permission perms = null;
    @Getter private WarpManager warpManager;
    @Getter private EnchanterManager enchanterManager;
    private ScoreboardListener scoreboardListener;

    public void onEnable() {
        instance = this;

        this.mongoManager = new MongoManager(this);
        setupConfigs();
        mongoManager.connect();

        warpManager = new WarpManager(this);
        enchanterManager = new EnchanterManager(this);

        this.scoreboardListener = new ScoreboardListener(this);
        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            setupPermissions();
        }
        registerListeners();
        registerCommands();
        if(!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getLogger().log(Level.WARNING, "[Iron] PlaceholderAPI not detected! Placeholders will not be registered.");
            return;
        } else {
            new Placeholders(this).register();
        }
    }

    public void onDisable() {

    }

    private void registerCommands() {
        getCommand("rankup").setExecutor(new RankupCommand(this));
        getCommand("prisonadmin").setExecutor(new PrisonAdminCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this));
        getCommand("warp").setTabCompleter(new WarpCommand(this));
        getCommand("setwarp").setExecutor(new SetWarpCommand(this));
        getCommand("delwarp").setExecutor(new DeleteWarpCommand(this));
        getCommand("ranks").setExecutor(new RanksCommand(this));
        getCommand("prestige").setExecutor(new PrestigeCommand(this));
        getCommand("enchanter").setExecutor(new EnchanterCommand(this));
    }

    private void registerListeners() {

        PluginManager pluginManager = Bukkit.getPluginManager();
        new BukkitRunnable() {
            public void run() {
                scoreboardListener.startUpdating();
            }
        }.runTaskLater(this, 10L);
        pluginManager.registerEvents(scoreboardListener, this);


        pluginManager.registerEvents(new PlayerListeners(this), this);
        pluginManager.registerEvents(new EnchanterInventoryListener(this), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private void setupConfigs() {
        File mainConfig = new File(getDataFolder(), File.separator + "config.yml");
        File ranks = new File(getDataFolder(), File.separator + "ranks.yml");
        File warps = new File(getDataFolder(), File.separator + "warps.yml");

        if(!mainConfig.exists()) {
            saveResource("config.yml", false);
        }

        if(!ranks.exists()) {
            saveResource("ranks.yml", false);
        }

        if(!warps.exists()) {
            saveResource("warps.yml", false);
        }

        ranksConfig = YamlConfiguration.loadConfiguration(ranks);
        warpsConfig = YamlConfiguration.loadConfiguration(warps);
    }

    public void reloadConfigs() {
        enchanterManager.getEnchanter().clear(); // Clear to remake
        reloadConfig();
        ranksConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), File.separator + "ranks.yml"));
        warpsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), File.separator + "warps.yml"));
        enchanterManager.getEnchanter(); // Hopefully remake the enchanter
    }

    public void saveRanksConfig() {
        try {
            ranksConfig.save(new File(getDataFolder(), File.separator + "ranks.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWarps() {
        try {
            warpsConfig.save(new File(getDataFolder(), File.separator + "warps.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBigBalance(Player player) {
        return String.valueOf((long) econ.getBalance(player));
    }

    public static Iron getInstance() {
        return instance;
    }
}
