package us.scoriapvp.floyd.server.command.argument;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.Server;
import us.scoriapvp.floyd.server.ServerManager;
import us.scoriapvp.floyd.server.command.NetworkServerCommand;
import us.scoriapvp.nightmare.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class NetworkServerDeleteArgument extends CommandArgument<Floyd> {

    public NetworkServerDeleteArgument(NetworkServerCommand command) {
        super(command, "delete", "Deletes a server.");
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <server>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sendUsage(sender, label + ' ' + name, getUsage(label));
            return;
        }

        ServerManager manager = plugin.getServerManager();
        String name = args[1];
        Server server = manager.getServer(name);
        if (server == null) {
            sender.sendMessage(ChatColor.RED + "Server named '" + name + "' does not exists.");
            return;
        }

        manager.deleteServer(server);
        sender.sendMessage(ChatColor.YELLOW + "Server " + ChatColor.BLUE + server.getName() + ChatColor.YELLOW + " has been successfully deleted.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return args.length == 2 ? plugin.getServerManager().getServers().stream().map(Server::getName).collect(Collectors.toList()) : Collections.emptyList();
    }
}