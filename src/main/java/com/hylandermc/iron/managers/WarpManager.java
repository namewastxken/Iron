package com.hylandermc.iron.managers;

import com.hylandermc.iron.Iron;
import com.hylandermc.iron.objects.Warp;
import com.hylandermc.iron.objects.WarpType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpManager {

    private Iron plugin;
    private FileConfiguration warps;
    private FileConfiguration config;
    public WarpManager(Iron instance) {
        this.plugin = instance;
        this.warps = plugin.getWarpsConfig();
        this.config = plugin.getConfig();
    }

    public Warp getWarp(String warp) {

        for(String warpNames : warps.getKeys(false)) {
            if(warpNames.equalsIgnoreCase(warp)) {
                if(getLocationFromString(warps.getString(warpNames + ".location")) != null) {
                    return new Warp(warpNames, getLocationFromString(warps.getString(warpNames + ".location")), WarpType.valueOf(warps.getString(warpNames + ".warpType")));
                }
            }
        }
        return null;
    }

    public void setWarp(String name, Location location, WarpType warpType) {
        warps.set(name + ".location", getStringFromLocation(location));
        warps.set(name + ".warpType", warpType.toString());
        plugin.saveWarps();
    }

    public void deleteWarp(String warpName) {
        warps.set(warpName + ".location", null);
        warps.set(warpName, null);
        plugin.saveWarps();
    }

    public List<String> getAllowedWarps(Player player) {
        ArrayList<String> allowed  = new ArrayList<>();
        for(String warp : this.warps.getKeys(false)) {
            if(getWarp(warp).canWarp(player)) {
                allowed.add(getWarp(warp).getName());
            }
        }
        if(allowed.isEmpty()) {
            allowed.add("No warps have been set.");
        }
        return allowed;
    }

    private static String getStringFromLocation(Location loc) {
        if (loc == null) {
            return "";
        }
        return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch() ;
    }

    private static Location getLocationFromString(String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 6) {
            World w = Bukkit.getServer().getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(w, x, y, z, yaw, pitch);
        }
        return null;
    }
}
