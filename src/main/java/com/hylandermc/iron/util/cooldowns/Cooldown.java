package com.hylandermc.iron.util.cooldowns;

import org.bukkit.entity.Player;

public class Cooldown {
    private Player player;
    private CooldownType cooldownType;
    public Cooldown(Player player, CooldownType cooldownType) {
        this.player = player;
        this.cooldownType = cooldownType;
    }

    public Player getPlayer() {
        return player;
    }

    public CooldownType getCooldownType() {
        return cooldownType;
    }
}
