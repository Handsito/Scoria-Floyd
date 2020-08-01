package us.scoriapvp.floyd.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.nightmare.util.command.NightmareCommand;

public final class ToggleScoreboardCommand extends NightmareCommand<Floyd> {

    public ToggleScoreboardCommand() {
        super("togglescoreboard", "Toggles your scoreboard visibility.", "togglesidebar", "togglehud", "togglesb");
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        PlayerScoreboard scoreboard = plugin.getScoreboardManager().getPlayerScoreboard(player);
        if (scoreboard == null) {
            player.sendMessage(ChatColor.RED + "An error occurred, please report this issue if this error consists (scoreboard returned null).");
            return;
        }

        boolean visible = !scoreboard.isVisible();
        scoreboard.setVisible(visible);
        sender.sendMessage(ChatColor.YELLOW + "Your scoreboard is " + (visible ? ChatColor.GREEN + "now" : ChatColor.RED + "no longer") + ChatColor.YELLOW + " visible.");
    }
}