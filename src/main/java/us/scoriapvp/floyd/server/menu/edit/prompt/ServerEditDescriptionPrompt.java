package us.scoriapvp.floyd.server.menu.edit.prompt;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import us.scoriapvp.floyd.server.ServerManager;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.floyd.server.menu.edit.EditServerMenu;

@AllArgsConstructor
public final class ServerEditDescriptionPrompt extends StringPrompt {

    private final GameServer server;
    private final int index;
    private final ServerManager manager;
    private final EditServerMenu menu;

    @Override
    public String getPromptText(ConversationContext context) {
        return ChatColor.YELLOW + "Please type the new description you want or type " + ChatColor.RED + "cancel" + ChatColor.YELLOW + " to cancel.\n" + ChatColor.GRAY + "This procedure will be auto cancelled in 60 seconds.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.RED + "Cancelled the procedure of editing description line.");
            return END_OF_CONVERSATION;
        }

        manager.editServer(server -> server.getDescription().set(index, ChatColor.translateAlternateColorCodes('&', input)), server);
        menu.open(player);
        player.playSound(Sound.CLICK, 1.0f, 1.0f);

        return END_OF_CONVERSATION;
    }

}