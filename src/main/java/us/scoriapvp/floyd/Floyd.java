package us.scoriapvp.floyd;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import us.scoriapvp.floyd.listener.EnvironmentListener;
import us.scoriapvp.floyd.listener.LobbyListener;
import us.scoriapvp.floyd.scoreboard.ScoreboardManager;
import us.scoriapvp.floyd.scoreboard.ToggleScoreboardCommand;
import us.scoriapvp.floyd.server.ServerManager;
import us.scoriapvp.floyd.server.command.NetworkServerCommand;
import us.scoriapvp.floyd.tablist.FloydTablist;
import us.scoriapvp.floyd.user.UserManager;
import us.scoriapvp.nightmare.Nightmare;
import us.scoriapvp.nightmare.connection.MongoConnection;
import us.scoriapvp.nightmare.connection.RedisConnection;
import us.scoriapvp.nightmare.util.animation.DotsAnimation;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Getter
public final class Floyd extends JavaPlugin {

    public static final DotsAnimation DOTS_ANIMATION = new DotsAnimation(5, ChatColor.RED.toString());

    private Nightmare nightmare;

    private FloydConfiguration config;

    private MongoConnection mongoConnection;
    private RedisConnection redisConnection;

    private ScoreboardManager scoreboardManager;
    private ServerManager serverManager;
    private UserManager userManager;

    @Override
    public void onLoad() {
        nightmare = JavaPlugin.getPlugin(Nightmare.class);

        config = new FloydConfiguration(this);
    }

    private void registerManagers() {
        scoreboardManager = new ScoreboardManager(this);
        serverManager = new ServerManager(this);
        userManager = new UserManager(this);
    }

    public void registerListeners(Listener... listeners) {
        Stream.of(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerListeners() {
        registerListeners(
                new EnvironmentListener(),
                new LobbyListener(this)
        );
    }

    private void registerCommands() {
        // nightmare.registerCommand(new SnowCommand());
        nightmare.registerCommand(new ToggleScoreboardCommand());
        nightmare.registerCommand(new NetworkServerCommand());
    }

    @Override
    public void onEnable() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        mongoConnection = new MongoConnection(config.getConfigurationSection("mongodb"));
        getLogger().info("Successfully connected to Mongo Database.");

        redisConnection = new RedisConnection(this, config.getConfigurationSection("redis"));
        getLogger().info("Successfully established Redis connection.");

        nightmare.getTablistManager().setProvider(FloydTablist.class, this);

        registerManagers();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        userManager.saveUsers();

        try {
            Thread.sleep(500L);
        } catch (InterruptedException ignored) {
        }

        mongoConnection.close();
        redisConnection.close();
    }
}