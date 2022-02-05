package com.hylandermc.iron.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TokenMineEvent extends Event implements Cancellable {

    @Getter private Player player;
    @Getter @Setter private int tokensGiven;
    private boolean isCancelled;
    public static HandlerList handlers = new HandlerList();
    public TokenMineEvent(Player player, int tokensGiven) {
        this.isCancelled = false;
        this.player = player;
        this.tokensGiven = tokensGiven;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
