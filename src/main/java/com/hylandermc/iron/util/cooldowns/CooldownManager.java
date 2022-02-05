package com.hylandermc.iron.util.cooldowns;

import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;

public class CooldownManager {
    private HashMap<Cooldown, Long> cooldownMap = new HashMap<>();

    public void createCooldown(Cooldown cooldown, double delay) {
        cooldownMap.put(cooldown, (long) (System.currentTimeMillis() + (delay * 1000)));
    }

    public boolean onCooldown(Player p, CooldownType cooldownType) {
        for(Cooldown cooldown : cooldownMap.keySet()) {
            if(cooldown.getPlayer().equals(p)) {
                if(cooldown.getCooldownType() == cooldownType) {
                    if(((cooldownMap.get(cooldown) - System.currentTimeMillis())/1000) <= 0) {
                        cooldownMap.remove(cooldown);
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public double getTimeLeftOnCooldown(Player p, CooldownType cooldownType) {
        for(Cooldown cooldown : cooldownMap.keySet()) {
            if(cooldown.getPlayer().equals(p)) {
                if(cooldown.getCooldownType() == cooldownType) {

                    double timeLeft = ((cooldownMap.get(cooldown) - System.currentTimeMillis())/1000);
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    return timeLeft;
                }
            }
        }
        return 0;
    }

    public String getNiceDouble(double delay) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(delay);
    }
}
