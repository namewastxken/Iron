package de.beproud.scoreboard;

import com.hylandermc.iron.Iron;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardListener implements Listener {

    private Iron plugin;
    public ScoreboardListener(Iron plugin) {
        this.plugin = plugin;
    }

    private ScoreboardManager scoreboardManager = new ScoreboardManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.setJoinMessage("");
        Player p = e.getPlayer();
        scoreboardManager.addToPlayerCache(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.setQuitMessage("");
        scoreboardManager.removeFromPlayerCache(e.getPlayer());
    }

    public void startUpdating() {

        if(Bukkit.getOnlinePlayers().size() > 0) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                scoreboardManager.addToPlayerCache(online);
            }
        }
        new BukkitRunnable(){
            public void run(){
                scoreboardManager.getPlayerScoreboards().values().forEach((playerScoreboard -> playerScoreboard.update()));
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }
}
