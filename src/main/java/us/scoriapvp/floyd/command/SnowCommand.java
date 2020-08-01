package us.scoriapvp.floyd.command;

import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.user.User;
import us.scoriapvp.nightmare.util.command.NightmareCommand;

public final class SnowCommand extends NightmareCommand<Floyd> {

    public SnowCommand() {
        super("snow", "Makes it snow.", "letitsnow", "snowparticles");
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        User user = plugin.getUserManager().getUser(player);
        if (user == null) {
            player.sendMessage(ChatColor.RED + "An error occurred, please report this issue if this error consists (user returned null).");
            return;
        }

        boolean snowEnabled = !user.isSnowEnabled();
        user.setSnowEnabled(snowEnabled);
        player.sendPacket(new PacketPlayOutGameStateChange(snowEnabled ? 2 : 1, 0));
        player.sendMessage(ChatColor.YELLOW + "You have " + (snowEnabled ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.YELLOW + " snow particles in lobby.");
    }
}