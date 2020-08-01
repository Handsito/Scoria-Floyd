package us.scoriapvp.floyd.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.user.User;
import us.scoriapvp.nightmare.NightmareConfiguration;
import us.scoriapvp.nightmare.util.BukkitUtils;

import java.util.List;

public interface ScoreboardProvider {

    String STRAIGHT_SCOREBOARD_LINE = ChatColor.GRAY + BukkitUtils.STRAIGHT_CHAT_LINE.substring(0, 22);

    default String getTitle() {
        return NightmareConfiguration.SERVER_TITLE;
    }

    List<String> getLines(Player player, User user);
}