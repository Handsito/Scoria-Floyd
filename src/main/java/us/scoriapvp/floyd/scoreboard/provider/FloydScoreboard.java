package us.scoriapvp.floyd.scoreboard.provider;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.scoreboard.ScoreboardProvider;
import us.scoriapvp.floyd.server.Server;
import us.scoriapvp.floyd.user.User;
import us.scoriapvp.nightmare.util.LuckPermsUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static us.scoriapvp.nightmare.NightmareConfiguration.PRIMARY_COLOR;

public final class FloydScoreboard implements ScoreboardProvider {

    @Override
    public List<String> getLines(Player player, User user) {
        List<String> lines = new ArrayList<>();
        lines.add(STRAIGHT_SCOREBOARD_LINE);
        lines.add(PRIMARY_COLOR + "Players");
        Server global = Server.GLOBAL;
        lines.add(global.isOnline() ? NumberFormat.getInstance().format(global.getOnlinePlayers()) + ChatColor.GRAY + '/' + ChatColor.WHITE + NumberFormat.getInstance().format(global.getMaxPlayers()) : ChatColor.RED + "Loading" + Floyd.DOTS_ANIMATION.getCurrentFrame());
        lines.add("");
        lines.add(PRIMARY_COLOR + "Rank" + ChatColor.GRAY + ": " + LuckPermsUtils.getGroup(player, true));
        lines.add("");
        lines.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "scoriapvp.us");
        lines.add(STRAIGHT_SCOREBOARD_LINE);
        return lines;
    }
}