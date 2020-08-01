package us.scoriapvp.floyd.server.menu.edit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.nightmare.menu.button.Button;
import us.scoriapvp.nightmare.menu.button.ButtonMenu;
import us.scoriapvp.nightmare.util.item.ItemBuilder;

import java.util.HashMap;
import java.util.Map;

public final class EditServerSelectorMenu extends ButtonMenu<Floyd> {

    private final GameServer server;

    public EditServerSelectorMenu(GameServer server) {
        super("Editing Server Selector", 3 * 9);

        this.server = server;
    }

    private ItemStack getServerIcon(GameServer server) {
        ItemBuilder builder = new ItemBuilder(server.getIcon());
        builder.setDisplayName(server.getDisplayName());
        builder.addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE);
        builder.addLore(ChatColor.YELLOW + "Click to swap server icon slot.");
        builder.addLore(ChatColor.YELLOW + "Shift + Click to remove server icon.");
        builder.addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE);
        return builder.build();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (GameServer server : plugin.getServerManager().getServers(GameServer.class)) {
            int slot = server.getSlot();
            if (slot == -1 || slot > inventory.getSize()) continue;

            buttons.put(slot, button -> getServerIcon(server));
        }

        if (server.getSlot() == -1) {
            player.setItemOnCursor(getServerIcon(server));
        }

        return buttons;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!topInventory.equals(inventory)) return;

        if (topInventory.equals(event.getClickedInventory())) {
            GameServer server;
            InventoryAction action = event.getAction();
            boolean remove = action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.COLLECT_TO_CURSOR;
            ItemStack cursor = event.getCursor();
            if ((server = (GameServer) plugin.getServerManager().getServer(remove ? event.getCurrentItem() : cursor)) != null) {
                server.setSlot(remove ? -1 : event.getRawSlot());
                if (remove) {
                    event.setCurrentItem(null);
                }
            }
        }
    }
}