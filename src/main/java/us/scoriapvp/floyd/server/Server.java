package us.scoriapvp.floyd.server;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import us.scoriapvp.nightmare.util.serialization.mongo.DocumentSerializable;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class Server implements DocumentSerializable {

    public static final Server GLOBAL = new Server("Global") {};

    protected String name;
    @Setter protected int onlinePlayers, maxPlayers;

    private long lastUpdate;

    public Server(String name) {
        this.name = Preconditions.checkNotNull(name, "Server name cannot be null");
    }

    public Server(Document document) {
        name = document.getString("_id");
    }

    public boolean isOnline() {
        boolean online = System.currentTimeMillis() - lastUpdate < TimeUnit.SECONDS.toMillis(2L);
        if (!online) {
            onlinePlayers = 0;
            maxPlayers = 0;
        }

        return online;
    }

    public void update(Document document) {
        onlinePlayers = document.getInteger("onlinePlayers", 0);
        maxPlayers = document.getInteger("maxPlayers", 0);

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public Document serialize() {
        return new Document("_id", name);
    }
}