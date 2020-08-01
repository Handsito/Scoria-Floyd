package us.scoriapvp.floyd.server.command.argument;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.Server;
import us.scoriapvp.floyd.server.ServerManager;
import us.scoriapvp.floyd.server.command.NetworkServerCommand;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.floyd.server.impl.LobbyServer;
import us.scoriapvp.nightmare.util.JavaUtils;
import us.scoriapvp.nightmare.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;

public final class NetworkServerCreateArgument extends CommandArgument<Floyd> {

    public NetworkServerCreateArgument(NetworkServerCommand command) {
        super(command, "create", "Creates a server.");
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <game|lobby>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sendUsage(sender, label, getUsage(label));
            return;
        }

        ServerManager manager = plugin.getServerManager();

        String argument = args[1];
        if (argument.equalsIgnoreCase("game")) {
            if (args.length < 4) {
                sendUsage(sender, label + ' ' + name + " game", getUsage(label) + " <server> <displayName>");
                return;
            }

            Server server = manager.getServer(args[2]);
            if (server != null) {
                sender.sendMessage(ChatColor.RED + "There is already a server named '" + args[2] + "'.");
                return;
            }

            manager.createServer(server = new GameServer(args[2], StringUtils.join(args, ' ', 3, args.length)));
            sender.sendMessage(ChatColor.YELLOW + "Created game server " + ChatColor.BLUE + server.getName() + ChatColor.YELLOW + '.');
        } else if (argument.equalsIgnoreCase("lobby")) {
            if (args.length < 3) {
                sendUsage(sender, label + ' ' + name + " lobby", getUsage(label) + " <number>");
                return;
            }

            Integer number = JavaUtils.tryParseInteger(args[2]);
            if (number == null || number < 1) {
                sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number.");
                return;
            }

            Server server = manager.getServer("Lobby-" + number);
            if (server != null) {
                sender.sendMessage(ChatColor.RED + "There is already a server named '" + server.getName() + "'.");
                return;
            }

            manager.createServer(server = new LobbyServer(number));
            sender.sendMessage(ChatColor.YELLOW + "Created lobby server " + ChatColor.BLUE + server.getName() + ChatColor.YELLOW + '.');
        } else {
            sendUsage(sender, label + ' ' + name, getUsage(label));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        return args.length == 2 ? ImmutableList.of("game", "lobby") : Collections.emptyList();
    }
}