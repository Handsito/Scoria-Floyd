package us.scoriapvp.floyd.scoreboard;

import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.*;
import us.scoriapvp.floyd.user.User;

import java.util.List;

public final class PlayerScoreboard {

    private static final String[] ENTRIES = new String[15];

    static {
        for (int i = 0; i < 15; i++) {
            ENTRIES[i] = ChatColor.values()[i].toString();
        }
    }

    private final ScoreboardProvider provider;

    private final Player player;
    private final User user;

    private final Scoreboard scoreboard;
    private final Objective objective;

    private int lastSentEntries;

    public PlayerScoreboard(ScoreboardProvider provider, Player player, User user) {
        this.provider = provider;

        this.player = player;
        this.user = user;

        Scoreboard scoreboard = player.getScoreboard();
        Server server = player.getServer();
        ScoreboardManager manager = server.getScoreboardManager();
        if (scoreboard == null || scoreboard == manager.getMainScoreboard()) {
            scoreboard = manager.getNewScoreboard();
        }

        Objective objective = (this.scoreboard = scoreboard).getObjective("PlayerScoreboard");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("PlayerScoreboard", "dummy");
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(provider.getTitle());
        this.objective = objective;

        player.setScoreboard(scoreboard);
    }

    public boolean isVisible() {
        return objective.getDisplaySlot() == DisplaySlot.SIDEBAR;
    }

    private boolean canUpdate() {
        return player != null && player.isOnline() && player.getScoreboard() == scoreboard && isVisible();
    }

    public void update() {
        if (!canUpdate()) return;

        String title = provider.getTitle();
        if (!objective.getDisplayName().equals(title)) {
            objective.setDisplayName(title);
        }

        int index;
        List<String> lines = provider.getLines(player, user);
        for (index = 0; index < lines.size(); ++index) {
            String line = lines.get(index), entry = ENTRIES[index], prefix = line, suffix = "";
            if (line.length() > 16) {
                prefix = line.substring(0, 16);
                if (prefix.endsWith("§")) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                    suffix += '§';
                }

                suffix = StringUtils.left(ChatColor.getLastColors(prefix) + suffix + line.substring(16), 16);
            }

            Team team = scoreboard.getTeam(entry);
            if (team == null) {
                try {
                    team = scoreboard.registerNewTeam(entry);
                } catch (IllegalArgumentException exception) {
                    net.minecraft.server.v1_8_R3.Scoreboard nmsScoreboard = ((CraftScoreboard) scoreboard).getHandle();
                    ScoreboardTeam scoreboardTeam = nmsScoreboard.getTeam(entry);
                    if (scoreboardTeam != null) {
                        nmsScoreboard.removeTeam(scoreboardTeam);
                    }

                    continue;
                }
            }

            if (!team.hasEntry(entry)) {
                team.addEntry(entry);
            }

            team.setPrefix(prefix);
            team.setSuffix(suffix);

            objective.getScore(team.getName()).setScore(lines.size() - index);
        }

        index = lines.size();
        for (int i = 0; i < lastSentEntries - index; ++i) {
            scoreboard.resetScores(ENTRIES[index + i]);
        }
        lastSentEntries = lines.size();
    }

    public void unregister() {
        player.setScoreboard(player.getServer().getScoreboardManager().getMainScoreboard());
    }

    public void setVisible(boolean visible) {
        objective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }
}