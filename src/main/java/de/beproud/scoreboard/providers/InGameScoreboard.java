package de.beproud.scoreboard.providers;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import com.hylandermc.iron.util.ConfigUtil;
import com.hylandermc.iron.util.NumberFormat;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import de.beproud.scoreboard.ScoreboardProvider;
import de.beproud.scoreboard.ScoreboardText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class InGameScoreboard extends ScoreboardProvider {

    private Iron plugin;

    public InGameScoreboard(Iron instance) {
        this.plugin = instance;
    }

    @Override
    public String getTitle(Player p) {
        return plugin.getConfig().getString("scoreboard-title").replaceAll("&", "§")
                .replaceAll("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }

    @Override
    public List<ScoreboardText> getLines(Player p) {
        RankManager rankManager = new RankManager(plugin, p);
        List<ScoreboardText> lines = new ArrayList<ScoreboardText>();
        Map<String, Object> variables = new HashMap<>();
        variables.clear();
        variables.put("%online%", Bukkit.getOnlinePlayers().size());
        variables.put("%player%", p.getName());
        if(rankManager.getPlayerDocument() != null) {
            variables.put("%prison_rank%", rankManager.getRank() != null ? rankManager.getRank() : "N/A");
            variables.put("%rankup_price%", rankManager.isMaxRank() ? "Max Rank" : NumberFormat.createString(rankManager.getRankupPrice()));
            variables.put("%prestige%", rankManager.getPrestige());
            variables.put("%next_rank%", rankManager.isMaxRank() ? "Max Rank" : rankManager.getNextRank());
        }
        if(TokenEnchantAPI.getInstance() != null) {
            variables.put("%tokens%", NumberFormat.createString(TokenEnchantAPI.getInstance().getTokens(p)));
        }
        if(Iron.getPerms() != null) {
            if(Iron.getPerms().hasGroupSupport()) {
                variables.put("%rank%", Iron.getPerms().getPrimaryGroup(p));
            }
        } else {
            Bukkit.getLogger().log(Level.WARNING, "Permissions aren't found, won't be available on scoreboard.");
        }
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        variables.put("%date%", dateFormat.format(now));
        variables.put("%time%", timeFormat.format(now));
        if(Iron.getEcon() != null) {
            variables.put("%money%", NumberFormat.createString(Iron.getEcon().getBalance(p)));
            variables.put("%balance%", NumberFormat.createString(Iron.getEcon().getBalance(p)));
        } else { Bukkit.getLogger().log(Level.WARNING, "economy not found, won't be available on scoreboard."); }
        lines.addAll(ConfigUtil.getScoreboardLines(p, variables));
        return lines;
    }
}
