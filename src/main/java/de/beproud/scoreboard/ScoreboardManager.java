package de.beproud.scoreboard;

import com.hylandermc.iron.Iron;
import de.beproud.scoreboard.providers.InGameScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager implements Listener {


    private ConcurrentHashMap<Player, PlayerScoreboard> playerScoreboards;

    public ScoreboardManager() {
        this.playerScoreboards = new ConcurrentHashMap<>();
        Bukkit.getOnlinePlayers().forEach((player) -> this.addToPlayerCache(player));
    }

    public boolean hasCachedPlayerScoreboard(Player p) {
        return this.playerScoreboards.containsKey(p);
    }

    public void addToPlayerCache(Player p) {
        if (!this.hasCachedPlayerScoreboard(p))	{
            this.playerScoreboards.put(p, new PlayerScoreboard(new InGameScoreboard(Iron.getInstance()), p));
        }
    }

    public void removeFromPlayerCache(Player p) {
        if (this.hasCachedPlayerScoreboard(p)) {
            this.playerScoreboards.remove(p).disappear();
        }
    }

    public PlayerScoreboard getPlayerScoreboard(Player p) {
        if (!this.hasCachedPlayerScoreboard(p)) this.addToPlayerCache(p);

        return this.playerScoreboards.get(p);
    }

    public ConcurrentHashMap<Player, PlayerScoreboard> getPlayerScoreboards() {
        return this.playerScoreboards;
    }

}