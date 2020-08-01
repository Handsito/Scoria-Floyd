package us.scoriapvp.floyd.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.VisibilityRule;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.scoriapvp.nightmare.Nightmare;
import us.scoriapvp.nightmare.profile.Profile;
import us.scoriapvp.nightmare.util.GenericUtils;
import us.scoriapvp.nightmare.util.serialization.mongo.DocumentSerializable;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class User implements DocumentSerializable {

    private final UUID uniqueId;
    @Setter private String name;

    private VisibilityRule visibilityRule = VisibilityRule.EVERYONE;

    @Setter private boolean snowEnabled;

    private Player player;

    public User(Document document) {
        uniqueId = (UUID) document.get("_id");
        name = document.getString("name");

        visibilityRule = GenericUtils.getIfPresent(VisibilityRule.class, document.getString("visibilityRule")).orElse(VisibilityRule.EVERYONE);

        snowEnabled = document.getBoolean("snowEnabled", false);
    }

    public Player getPlayer() {
        return player == null ? player = Bukkit.getPlayer(uniqueId) : player;
    }

    public void setVisibilityRule(VisibilityRule rule) {
        visibilityRule = rule;

        Player player = getPlayer();
        if (player != null) {
            player.setVisibilityRule(rule, true);
        }
    }

    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    public Profile getProfile() {
        return JavaPlugin.getPlugin(Nightmare.class).getProfileManager().getProfile(uniqueId);
    }

    @Override
    public Document serialize() {
        Document document = new Document("_id", uniqueId);
        document.put("name", name);

        if (visibilityRule != VisibilityRule.EVERYONE) {
            document.put("visibilityRule", visibilityRule.name());
        }

        if (snowEnabled) {
            document.put("snowEnabled", true);
        }

        return document;
    }
}