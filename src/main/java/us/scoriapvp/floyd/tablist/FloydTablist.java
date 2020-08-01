package us.scoriapvp.floyd.tablist;

import lombok.AllArgsConstructor;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.Server;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.nightmare.NightmareConfiguration;
import us.scoriapvp.nightmare.profile.Profile;
import us.scoriapvp.nightmare.tablist.PlayerTablist;
import us.scoriapvp.nightmare.tablist.TablistColumn;
import us.scoriapvp.nightmare.tablist.TablistComponent;
import us.scoriapvp.nightmare.tablist.TablistProvider;
import us.scoriapvp.nightmare.util.JavaUtils;
import us.scoriapvp.nightmare.util.LuckPermsUtils;
import us.scoriapvp.nightmare.util.Texture;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public final class FloydTablist implements TablistProvider {

    private static final Texture ONLINE_TEXTURE = new Texture(
            "eyJ0aW1lc3RhbXAiOjE1ODEzNjg4OTMyNDUsInByb2ZpbGVJZCI6ImI1ODZkMzhlYjQ0YjQwNDc4NzI3MGIwOGRiYjE0YzU0IiwicHJvZmlsZU5hbWUiOiJNYW5pdGFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ODYyMTYwMDJjMGVlMzJkNmNjMTU5NDYyMzUwMmYwY2U3ZmNhZTFkYzFlZTQ3MzNmNDk0YzZkMTM4MTFjZmU5In19fQ==",
            "DsrNt5gn9vHV7spag45Q+ECSJj2vWwhVy1CfpFKCL4bB4m3sZl/AYUXKTmTl/udbGyjL3DgLAl9tahZq8+jGndnPneDQnas7NRHXdxEphKenunpBTOJtadXmmdoE928qLewRgPi1GFfCAoUzYTO4Kf0rh3mClYEzd/b3SWk14iEIt8bxTh5gETCLvznKmSLbQZkCPiSVjBFHd56sRZojobF0wJPamlORjwZVY+g+BP45vZ8lB0hiboc44rAds+yxnU6tpyJiBg8FkiV8LS452DEf84Ze2b7UsLQGVUCgoREsBCCNhPJxRYTCAGgqoryUQ9Cqj3EjPgGdLfD8d5fzrSAItCwmlskPaKLf6RTjK6UvZoGPtbvYoktZJQIst+uHVAVkT6Ddp6NGYdhRpN7n5gyufOsejJbN2kyieWlFaFNteZorqV1Ahl4aWRANoee1B6RwsijmKK+AGJyiPzJ9+S2vn/uWRmcb1iqqmp7zlUq44jUbIjAmgjMyiCfeR/6+zFSznMdRvby2w7Th95gy/Z/5MCErp9G4CToUbhYmvPB9ek89DIxwaNl7MaeENH8TR0ot2mdZgrSI7xIJILW+lA9A6AxEtjL6IEhfPDv8b+qXRRC/XxQHrjr8qQynsANpDgKzB8gJjkc9iS8fABri0CcZD++xS7SuXoS34R11OCQ="
    );
    private static final Texture OFFLINE_TEXTURE = new Texture(
            "eyJ0aW1lc3RhbXAiOjE1ODEzNjkzMzU1NDMsInByb2ZpbGVJZCI6ImI1ODZkMzhlYjQ0YjQwNDc4NzI3MGIwOGRiYjE0YzU0IiwicHJvZmlsZU5hbWUiOiJNYW5pdGFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNzMwNjVhYjU3NTVjYTFmYTU1OTE1MTU2ZjRkNjc4YjE4NDdjZWViYzM4ZDQ2ZTQ0ZGYxMmNhN2UzMmQ3MDEwIn19fQ==",
            "uI6Y1D3CA+DhH0R46iupCAFHKLmE8xw8iF+mmpXWlMspIxTWEvviPw0ize8xnneCUd/FeQSO6STfhiVt7TuoQ+hdvPKWgbTirplrmr26mjNmstnv9ANf99iHQtGk2b3/HWuyXr5mICiF6toX01oSP7NR09BIrAcc9gwfhdkNppZRe2eGmINwK7Uv+9S0nxy82iKPVWnyYueC/kFHdXKgFUV0GdCNTMeLEjZIfiOIaKn9fHfsHMF2pLflgFBn+tjz0XCghxkp2saRYAcQJHE3v879sBqvQ2kKlhTGVY/Bs9+6/QelCpOzMZeHWrz6NuZD+s0pGBKJKktoWKTvTRGQg5D3BSp0umrpr5XcU56EBH+57eR5DhWmNTqg5wyvz0W1rAyzwe4ZA2EkVwYUOBEbkS1hWVsX24DB5TCluTGAl4i1k4+XA/CU5f0dw/2HPqXtVfOV9wRi/MeA7jPFntP10nDy7cGPG5m0gvTB4+UzVQn+sdPF3mPzp4Cvkq9FSZw5ExYtzRaxT0TwxN4m3zS3Cr0S+vynLbWKaUazJfzHcvic7SvJ6AvfJPqe+njPlkxcRuXf+wsNzWEKHnDxY+fmFmcAYFDQO30BKHjmaKTpkIm08JvQI9n+rAlojJFjojXhbV6LMmW/wdZY750ryB1Rd5ohLWiIpid/13x0zSjXGR0="
    );

    private final Floyd plugin;

    @Override
    public Set<TablistComponent> getComponents(Player player, PlayerTablist tablist) {
        Set<TablistComponent> components = new HashSet<>();
        components.add(new TablistComponent(TablistColumn.MIDDLE, 2, NightmareConfiguration.SERVER_TITLE));

        Server global = Server.GLOBAL;
        components.add(new TablistComponent(TablistColumn.MIDDLE, 3, ChatColor.GRAY + "Players: " + (global.isOnline() ? ChatColor.WHITE.toString() + global.getOnlinePlayers() + ChatColor.GRAY + '/' + ChatColor.WHITE + global.getMaxPlayers() : ChatColor.RED + "Loading" + Floyd.DOTS_ANIMATION)));

        Profile profile = plugin.getNightmare().getProfileManager().getProfile(player);
        if (profile != null) {
            components.add(new TablistComponent(TablistColumn.MIDDLE, 5, NightmareConfiguration.TITLE_COLOR + "You"));

            int ping = player.spigot().getPing();
            components.add(new TablistComponent(TablistColumn.MIDDLE, 6, tablist.isLegacy() ? profile.getColoredName() : profile.getDisplayName(), ping, plugin.getNightmare().getTablistManager().getTexture(player)));

            components.add(new TablistComponent(TablistColumn.LEFT, 6, NightmareConfiguration.PRIMARY_COLOR + "Rank"));
            components.add(new TablistComponent(TablistColumn.LEFT, 7, LuckPermsUtils.getGroup(player, true)));
            InheritanceNode rank = profile.getRankNode();
            if (rank != null && rank.hasExpiry()) {
                components.add(new TablistComponent(TablistColumn.LEFT, 8, ChatColor.GRAY + "Expires In: " + ChatColor.RED + JavaUtils.formatDuration(rank.getExpiry().toEpochMilli() - System.currentTimeMillis())));
            }

            components.add(new TablistComponent(TablistColumn.RIGHT, 6, NightmareConfiguration.PRIMARY_COLOR + "Information"));
            components.add(new TablistComponent(TablistColumn.RIGHT, 7, ChatColor.GRAY + "Ping: " + ChatColor.WHITE + ping + "ms"));
            components.add(new TablistComponent(TablistColumn.RIGHT, 8, ChatColor.GRAY + "Credits: " + ChatColor.YELLOW + profile.getTagCredits()));
        }

        components.add(new TablistComponent(TablistColumn.MIDDLE, 10, NightmareConfiguration.TITLE_COLOR + "Server Status"));

        List<GameServer> servers = plugin.getServerManager().getServers(GameServer.class);
        for (int index = 0; index < servers.size(); index++) {
            int row = index % 3;
            TablistColumn column = TablistColumn.getColumn(row);
            int slot = 12 + index - row;

            GameServer server = servers.get(index);
            String name = server.getName();
            boolean online = server.isOnline();
            components.add(new TablistComponent(column, slot, NightmareConfiguration.PRIMARY_COLOR + name, online ? ONLINE_TEXTURE : OFFLINE_TEXTURE));
            components.add(new TablistComponent(column, slot + 1, !name.equals("UHC") && server.isWhitelisted() ? ChatColor.RED + "Whitelisted" : online ? ChatColor.GRAY + "Players: " + ChatColor.WHITE + server.getOnlinePlayers() + ChatColor.GRAY + "/" + ChatColor.WHITE + server.getMaxPlayers() : ChatColor.RED + "Offline" + Floyd.DOTS_ANIMATION));
        }

        return components;
    }
}