package us.scoriapvp.floyd.server;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.CaseInsensitiveMap;
import redis.clients.jedis.Jedis;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.floyd.server.impl.LobbyServer;
import us.scoriapvp.nightmare.redis.RedisCallable;
import us.scoriapvp.nightmare.redis.RedisMessageListener;
import us.scoriapvp.nightmare.util.GenericUtils;
import us.scoriapvp.nightmare.util.serialization.mongo.DocumentSerialization;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ServerManager implements RedisMessageListener {

    private final MongoCollection collection;
    private final Floyd plugin;

    private final Map<String, Server> servers = new CaseInsensitiveMap<>();

    private final EnumMap<ServerType, Class<? extends Server>> types = new EnumMap<>(ServerType.class);

    public ServerManager(Floyd plugin) {
        collection = (this.plugin = plugin).getMongoConnection().getDatabase().getCollection("servers");

        types.put(ServerType.GAME, GameServer.class);
        types.put(ServerType.LOBBY, LobbyServer.class);
        types.put(ServerType.UNKNOWN, Server.class);

        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            collection.find().forEach((Consumer<Document>) document -> servers.put(document.getString("_id"), deserializeServer(document)));

            plugin.getRedisConnection().registerListener(this);
        }, 10L);
    }

    private Server deserializeServer(Document document) {
        try {
            return types.getOrDefault(GenericUtils.getIfPresent(ServerType.class, document.getString("type")).orElse(ServerType.UNKNOWN), Server.class).getConstructor(Document.class).newInstance(document);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public Server getServer(String name) {
        return servers.get(name);
    }

    public <T extends Server> T getServer(Class<? extends Server> clazz, String name) {
        Server server = name == null ? null : getServer(name);
        return server != null && clazz.isAssignableFrom(server.getClass()) ? (T) clazz.cast(server) : null;
    }

    public Server getServer(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        return getServers().stream().filter(server -> {
            String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (itemName.contains("/")) {
                itemName = itemName.split("\\s\\(\\d+")[0];
            }

            String name = server instanceof GameServer ? ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', ((GameServer) server).getDisplayName())) : server.getName();
            return name.equalsIgnoreCase(itemName);
        }).findFirst().orElse(null);
    }

    public List<Server> getServers() {
        return ImmutableList.copyOf(servers.values());
    }

    public <S> List<S> getServers(Class<S> clazz) {
        return servers.values().stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }

    public void broadcast(String action, Document document, Server server, Consumer<Server> consumer) {
        consumer.accept(server);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new RedisCallable<Long>(plugin.getRedisConnection()) {
            @Override
            protected Long call(Jedis jedis) {
                return jedis.publish("floyd", encode(action, document));
            }
        });
    }

    public void saveServer(Server server) {
        collection.replaceOne(Filters.eq(server.getName()), server.serialize(), new ReplaceOptions().upsert(true));
    }

    public void createServer(Server server) {
        broadcast("SERVER_CREATE", server.serialize(), server, this::saveServer);
    }

    public void deleteServer(Server server) {
        Document document = new Document("_id", server.getName());
        broadcast("SERVER_DELETE", document, server, target -> collection.deleteOne(document));
    }

    public <S extends Server> void editServer(Consumer<S> consumer, S server) {
        consumer.accept(server);
        broadcast("SERVER_EDIT", server.serialize(), server, this::saveServer);
    }

    @Override
    public String[] getChannels() {
        return new String[] { "floyd" };
    }

    @Override
    public void onReceive(String channel, String action, Document payload) {
        switch (action) {
            case "SERVER_UPDATED_DATA": {
                Server server;
                String name = payload.getString("name");
                if ((server = name.equalsIgnoreCase("Global") ? Server.GLOBAL : getServer(name)) != null) {
                    server.update(payload);
                }

                break;
            }

            case "SERVER_CREATE": {
                servers.put(payload.getString("_id"), deserializeServer(payload));
                break;
            }

            case "SERVER_DELETE": {
                servers.remove(payload.getString("_id"));
                break;
            }

            case "SERVER_EDIT": {
                GameServer server = getServer(GameServer.class, payload.getString("_id"));
                if (server != null) {
                    server.setDisplayName(payload.getString("displayName"));
                    server.setDescription(payload.getList("description", String.class, new ArrayList<>()));
                    server.setSlot(payload.getInteger("slot", -1));
                    Document icon = (Document) payload.get("icon");
                    server.setIcon(icon != null ? DocumentSerialization.deserializeItem(icon) : null);
                }
            }
        }
    }
}