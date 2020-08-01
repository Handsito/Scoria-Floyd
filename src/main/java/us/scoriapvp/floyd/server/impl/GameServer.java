package us.scoriapvp.floyd.server.impl;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.scoriapvp.floyd.server.ServerType;
import us.scoriapvp.floyd.server.type.JoinableServer;
import us.scoriapvp.nightmare.util.serialization.mongo.DocumentSerialization;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class GameServer extends JoinableServer {

    public static final ItemStack DEFAULT_SERVER_ICON = new ItemStack(Material.CHEST);

    private String displayName;
    private List<String> description = new ArrayList<>();

    private int slot = -1;

    private ItemStack icon;

    public GameServer(Document document) {
        super(document);

        displayName = document.getString("displayName");
        description = document.getList("description", String.class, new ArrayList<>());

        slot = document.getInteger("slot", -1);

        if (document.containsKey("icon")) {
            setIcon(DocumentSerialization.deserializeItem((Document) document.get("icon")));
        }
    }

    public GameServer(String name, String displayName) {
        super(name);

        this.displayName = displayName;
    }

    @Override
    public ServerType getType() {
        return ServerType.GAME;
    }

    public boolean hasIcon() {
        return icon != null;
    }

    public ItemStack getIcon() {
        return (hasIcon() ? icon : DEFAULT_SERVER_ICON).clone();
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon == null || icon.getType() == Material.AIR ? null : icon.clone();
    }

    @Override
    public Document serialize() {
        Document document = super.serialize();
        document.put("displayName", displayName);

        if (!description.isEmpty()) {
            document.put("description", description);
        }

        if (slot != -1) {
            document.put("slot", slot);
        }

        if (hasIcon()) {
            document.put("icon", DocumentSerialization.serializeItem(icon));
        }

        return document;
    }
}