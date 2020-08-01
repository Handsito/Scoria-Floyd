package us.scoriapvp.floyd.scoreboard;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.scoreboard.provider.FloydScoreboard;
import us.scoriapvp.floyd.user.User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoreboardManager implements Listener {

    private final Floyd plugin;

    @Getter private final ScoreboardProvider provider;

    private final Map<UUID, PlayerScoreboard> scoreboards = new ConcurrentHashMap<>();

    public ScoreboardManager(Floyd plugin) {
        this.plugin = plugin;

        provider = new FloydScoreboard();

        plugin.registerListeners(this);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> scoreboards.forEach((uniqueId, scoreboard) -> scoreboard.update()), 2L, 2L);
    }

    public Collection<PlayerScoreboard> getScoreboards() {
        return scoreboards.values();
    }

    public PlayerScoreboard getPlayerScoreboard(Player player) {
        return scoreboards.get(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getUserManager().getUser(player);
        if (user != null) {
            scoreboards.put(player.getUniqueId(), new PlayerScoreboard(provider, player, user));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerScoreboard scoreboard = scoreboards.remove(player.getUniqueId());
        if (scoreboard != null) {
            scoreboard.unregister();
        }
    }
}