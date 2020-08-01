package us.scoriapvp.floyd.user;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.nightmare.redis.RedisCallable;
import us.scoriapvp.nightmare.redis.RedisMessageListener;
import us.scoriapvp.nightmare.util.BukkitUtils;
import us.scoriapvp.nightmare.util.JavaUtils;

import java.util.*;
import java.util.regex.Pattern;

public final class UserManager implements Listener, RedisMessageListener {

    private final Floyd plugin;

    @Getter private final MongoCollection collection;

    private final Map<UUID, User> users = new HashMap<>();

    private final Set<UUID> deniedLogin = Collections.synchronizedSet(new HashSet<>());

    public UserManager(Floyd plugin) {
        this.plugin = plugin;

        collection = plugin.getMongoConnection().getDatabase().getCollection("users");

        plugin.registerListeners(this);
        plugin.getNightmare().getRedisConnection().registerListener(this);
    }

    public Map<UUID, User> getUsersMap() {
        return ImmutableMap.copyOf(users);
    }

    public List<User> getUsers() {
        return ImmutableList.copyOf(users.values());
    }

    public boolean isLoaded(User user) {
        return users.containsKey(user.getUniqueId());
    }

    public User getUser(UUID uniqueId) {
        return users.get(uniqueId);
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(String search) {
        if (Strings.isNullOrEmpty(search)) return null;

        boolean uuid = JavaUtils.isUUID(search);
        if (!BukkitUtils.isValidUsername(search) && !uuid) return null;

        if (uuid) {
            User user = getUser(UUID.fromString(search));
            if (user != null) return user;
        } else {
            Player player = plugin.getServer().getPlayer(search);
            if (player != null) return getUser(player);
        }

        Bson filter = uuid ? Filters.eq(UUID.fromString(search)) : Filters.eq("name", Pattern.compile('^' + search + '$', Pattern.CASE_INSENSITIVE));
        Document document = (Document) collection.find(filter).first();
        if (document != null) return new User(document);

        return null;
    }

    public void saveUser(User user) {
        UUID uniqueId = user.getUniqueId();

        new RedisCallable<Long>(plugin.getRedisConnection()) {
            @Override
            protected Long call(Jedis jedis) {
                return jedis.del("floyd-profile:" + uniqueId.toString());
            }
        }.call();

        collection.replaceOne(Filters.eq(uniqueId), user.serialize(), new ReplaceOptions().upsert(true));
    }

    public void saveUsers() {
        users.forEach((uniqueId, user) -> saveUser(user));
    }

    public void saveUserAsync(User user) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveUser(user));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uniqueId = event.getUniqueId();
        User user = new User(uniqueId);

        Document document;

        Jedis jedis = null;

        try {
            document = Document.parse((jedis = plugin.getRedisConnection().getJedis()).get("floyd-profile:" + uniqueId.toString()));
        } catch (Exception exception) {
            document = (Document) collection.find(Filters.eq(uniqueId)).first();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        if (document != null) {
            if (uniqueId.toString().equals(((UUID) document.get("_id")).toString())) {
                user = new User(document);
            } else {
                collection.deleteOne(document);
            }
        }

        String last = user.getName();
        String name = event.getName();
        if (last == null || !last.equals(name)) {
            user.setName(name);
            saveUser(user);
        }

        users.put(uniqueId, user);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        User user = getUser(player);
        if (user == null) {
            deniedLogin.add(player.getUniqueId());
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "User data could not be loaded. Please try again later. (1)");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginMonitor(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PlayerLoginEvent.Result result = event.getResult();
        if (deniedLogin.remove(player.getUniqueId()) && result == PlayerLoginEvent.Result.ALLOWED) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "User data could not be loaded. Please try again later. (2)");
        }

        if (result != PlayerLoginEvent.Result.ALLOWED) {
            users.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = users.remove(player.getUniqueId());
        if (user != null) {
            saveUserAsync(user);
        }
    }

    @Override
    public String[] getChannels() {
        return new String[] { "floyd" };
    }

    @Override
    public void onReceive(String channel, String action, Document payload) {
        if (action.equals("PROFILE_REQUEST")) {
            if (getServer().getServerName().equalsIgnoreCase(payload.getString("server"))) {
                User user = getUser(UUID.fromString(payload.getString("uuid")));
                if (user != null) {
                    new RedisCallable<String>(plugin.getRedisConnection()) {
                        @Override
                        protected String call(Jedis jedis) {
                            return jedis.set("floyd-profile:" + user.getUniqueId().toString(), user.serialize().toJson());
                        }
                    }.call();
                }
            }
        }
    }
}