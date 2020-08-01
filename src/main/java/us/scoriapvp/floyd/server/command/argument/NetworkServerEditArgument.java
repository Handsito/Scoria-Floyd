package us.scoriapvp.floyd.server.command.argument;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.Server;
import us.scoriapvp.floyd.server.command.NetworkServerCommand;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.floyd.server.menu.edit.EditServerMenu;
import us.scoriapvp.nightmare.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class NetworkServerEditArgument extends CommandArgument<Floyd> {

    public NetworkServerEditArgument(NetworkServerCommand command) {
        super(command, "edit", "Edits all configurable things of a game server.");
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <server>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            sendUsage(player, label + ' ' + name, getUsage(label));
            return;
        }

        String name = args[1];
        Server server = plugin.getServerManager().getServer(name);
        if (server == null) {
            player.sendMessage(ChatColor.RED + "Server named '" + name + "' does not exists.");
            return;
        }

        if (!(server instanceof GameServer)) {
            player.sendMessage(ChatColor.RED + "You may only use this command with game servers.");
            return;
        }

        new EditServerMenu((GameServer) server).open(player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return args.length == 2 ? plugin.getServerManager().getServers().stream().map(Server::getName).collect(Collectors.toList()) : Collections.emptyList();
    }
}