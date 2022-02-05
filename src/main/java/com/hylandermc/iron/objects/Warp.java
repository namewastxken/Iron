package com.hylandermc.iron.objects;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.managers.RankManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Warp {

    private Iron plugin = Iron.getInstance();
    private WarpType warpType;
    private Location location;
    private String name;
    private String permission = "";

    public Warp(String name, Location location, WarpType warpType) {
        this.name = name;
        this.location = location;
        this.warpType = warpType;

        if(warpType == WarpType.PERMISSION) {
            permission = "iron.warp." + name;
        }
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public boolean canWarp(Player player) {
        RankManager rankManager = new RankManager(plugin, player);

        if(player.hasPermission("iron.warp.bypass")) {
            return true;
        }

        if(warpType == WarpType.MINE) {
            if(rankManager.getRankPosition(name) != -1 && rankManager.getRankPosition() >= rankManager.getRankPosition(name)) {
                return true;
            }
        }

        if(warpType == WarpType.PERMISSION) {
            return player.hasPermission(permission);
        }
        return false;
    }
}
