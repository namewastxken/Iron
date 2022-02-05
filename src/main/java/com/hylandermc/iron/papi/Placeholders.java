package com.hylandermc.iron.papi;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    private Iron plugin;
    public Placeholders(Iron instance) {
        this.plugin = instance;
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        RankManager rankManager = new RankManager(plugin, p);
        if(identifier.equalsIgnoreCase("prison_rank")) {
            return rankManager.getRank();
        }

        if(identifier.equalsIgnoreCase("prison_prestige")) {
            if(rankManager.getPrestige() == 0) {
                return "";
            }
            return String.valueOf(rankManager.getPrestige()) + " ";
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "iron";
    }

    @Override
    public String getAuthor() {
        return "";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    public boolean persist() {
        return true;
    }
}
