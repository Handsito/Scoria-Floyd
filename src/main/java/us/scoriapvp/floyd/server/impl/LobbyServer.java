package us.scoriapvp.floyd.server.impl;

import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import us.scoriapvp.floyd.server.ServerType;
import us.scoriapvp.floyd.server.type.JoinableServer;

@Getter
public final class LobbyServer extends JoinableServer {

    private final int number;

    public LobbyServer(int number) {
        super("Lobby-" + number);

        this.number = number;
    }

    public LobbyServer(Document document) {
        super(document);

        number = document.getInteger("number");
    }

    @Override
    public ServerType getType() {
        return ServerType.LOBBY;
    }

    public boolean isLocalhost() {
        return Bukkit.getServerName().equals(name);
    }

    @Override
    public Document serialize() {
        return super.serialize().append("number", number);
    }
}