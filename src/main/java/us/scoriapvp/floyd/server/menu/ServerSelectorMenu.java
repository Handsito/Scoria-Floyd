package us.scoriapvp.floyd.server.menu;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.nightmare.menu.button.Button;
import us.scoriapvp.nightmare.menu.button.ButtonMenu;
import us.scoriapvp.nightmare.util.item.ItemBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServerSelectorMenu extends ButtonMenu<Floyd> {

    public ServerSelectorMenu() {
        super("Server Selector", 3 * 9);
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (GameServer server : plugin.getServerManager().getServers(GameServer.class)) {
            int slot = server.getSlot();
            if (slot == -1 || slot > inventory.getSize()) continue;

            buttons.put(slot, new Button() {
                @Override
                public ItemStack getIcon(Player player) {
                    ItemBuilder builder = new ItemBuilder(server.getIcon().clone());
                    boolean online = server.isOnline();
                    builder.setDisplayName(server.getDisplayName() + (online ? ChatColor.GRAY + " (" + ChatColor.WHITE + server.getOnlinePlayers() + ChatColor.GRAY + '/' + ChatColor.WHITE + server.getMaxPlayers() + ChatColor.GRAY + ')' : ""));
                    builder.addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE);
                    List<String> description = server.getDescription();
                    description.forEach(line -> builder.addLore(ChatColor.GRAY + line));
                    if (!description.isEmpty()) {
                        builder.addLore("");
                    }

                    builder.addLore(online ? ChatColor.YELLOW + "Click to connect this server!" : ChatColor.RED + "This server is currently offline!");
                    builder.addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE);
                    return builder.build();
                }

                @Override
                public void onClick(Player player, int slot, ClickType type, InventoryAction action) {
                    player.closeInventory();

                    server.connect(player);
                    if (!server.isOnline()) {
                        player.sendMessage(ChatColor.RED + "This server is currently offline.");
                        player.playSound(Sound.ITEM_BREAK, 1.0f, 0.5f);
                    }
                }
            });
        }

        return buttons;
    }
}