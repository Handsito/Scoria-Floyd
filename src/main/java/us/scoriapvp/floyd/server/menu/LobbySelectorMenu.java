package us.scoriapvp.floyd.server.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.impl.LobbyServer;
import us.scoriapvp.nightmare.menu.button.Button;
import us.scoriapvp.nightmare.menu.button.ButtonMenu;
import us.scoriapvp.nightmare.util.BukkitUtils;
import us.scoriapvp.nightmare.util.item.ItemBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class LobbySelectorMenu extends ButtonMenu<Floyd> {

    public LobbySelectorMenu() {
        super("Lobby Selector", 3 * 9);
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, Button.BACK_BUTTON);

        int slot = 10;

        for (LobbyServer server : plugin.getServerManager().getServers(LobbyServer.class).stream().sorted(Comparator.comparingInt(LobbyServer::getNumber)).collect(Collectors.toList())) {
            buttons.put(slot++, new Button() {
                @Override
                public ItemStack getIcon(Player player) {
                    ItemBuilder builder = new ItemBuilder(Material.NETHER_STAR);
                    builder.setDisplayName((server.isOnline() ? ChatColor.GREEN : ChatColor.RED) + server.getName());
                    builder.addLore(ChatColor.GRAY + BukkitUtils.ITEM_LORE_STRAIGHT_LINE);
                    if (server.isOnline()) {
                        builder.addLore(ChatColor.GRAY + "Players: " + ChatColor.WHITE + server.getOnlinePlayers() + ChatColor.GRAY + '/' + ChatColor.WHITE + server.getMaxPlayers());
                        builder.addLore("");
                    }
                    builder.addLore(server.isLocalhost() ? ChatColor.RED + "You are already connected!" : server.isOnline() ? ChatColor.YELLOW + "Click to connect this server!" : ChatColor.RED + "This server is currently offline!");
                    builder.addLore(ChatColor.GRAY + BukkitUtils.ITEM_LORE_STRAIGHT_LINE);
                    return builder.build();
                }

                @Override
                public void onClick(Player player, int slot, ClickType type, InventoryAction action) {
                    if (server.isLocalhost()) {
                        player.sendMessage(ChatColor.RED + "You are already connected to this server.");
                        player.playSound(Sound.ITEM_BREAK, 1.0f, 0.5f);
                        return;
                    }

                    if (!server.isOnline()) {
                        player.sendMessage(ChatColor.RED + "This server is currently offline.");
                        player.playSound(Sound.ITEM_BREAK, 1.0f, 0.5f);
                        return;
                    }

                    server.connect(player);
                }
            });

            if ((slot - 8) % 9 == 0) {
                slot += 2;
            }
        }

        return buttons;
    }
}