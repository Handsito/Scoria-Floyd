package us.scoriapvp.floyd.server.type;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.Server;
import us.scoriapvp.floyd.server.ServerType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter
@Setter
public abstract class JoinableServer extends Server {

    protected boolean whitelisted;

    public JoinableServer(String name) {
        super(name);
    }

    public JoinableServer(Document document) {
        super(document);
    }

    public abstract ServerType getType();

    @Override
    public void update(Document document) {
        super.update(document);

        whitelisted = document.containsKey("whitelisted");
    }

    public void connect(Player player) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(byteOutput);
        try {
            dataOutput.writeUTF("Connect");
            dataOutput.writeUTF(name);
        } catch (IOException ignored) {
        }

        player.sendPluginMessage(JavaPlugin.getPlugin(Floyd.class), "BungeeCord", byteOutput.toByteArray());
    }

    @Override
    public Document serialize() {
        Document document = super.serialize();
        document.put("type", getType().name());
        return document;
    }
}