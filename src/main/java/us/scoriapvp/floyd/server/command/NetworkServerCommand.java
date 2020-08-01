package us.scoriapvp.floyd.server.command;

import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.command.argument.NetworkServerCreateArgument;
import us.scoriapvp.floyd.server.command.argument.NetworkServerDeleteArgument;
import us.scoriapvp.floyd.server.command.argument.NetworkServerEditArgument;
import us.scoriapvp.nightmare.util.command.CommandParent;

public final class NetworkServerCommand extends CommandParent<Floyd> {

    public NetworkServerCommand() {
        super("networkserver", "Reference command for servers.", "networksv", "nsv");

        addArgument(new NetworkServerCreateArgument(this));
        addArgument(new NetworkServerDeleteArgument(this));
        addArgument(new NetworkServerEditArgument(this));
    }

    @Override
    public boolean isRequiresPermission() {
        return true;
    }
}