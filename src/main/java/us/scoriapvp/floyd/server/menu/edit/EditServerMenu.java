package us.scoriapvp.floyd.server.menu.edit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.ServerManager;
import us.scoriapvp.floyd.server.impl.GameServer;
import us.scoriapvp.floyd.server.menu.edit.prompt.ServerAddDescriptionPrompt;
import us.scoriapvp.floyd.server.menu.edit.prompt.ServerEditDescriptionPrompt;
import us.scoriapvp.nightmare.menu.button.Button;
import us.scoriapvp.nightmare.menu.button.ButtonMenu;
import us.scoriapvp.nightmare.menu.button.impl.BackButton;
import us.scoriapvp.nightmare.menu.impl.ConfirmationMenu;
import us.scoriapvp.nightmare.sign.SignInputSession;
import us.scoriapvp.nightmare.util.item.ItemBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EditServerMenu extends ButtonMenu<Floyd> {

    private final GameServer server;
    private final ServerManager manager;

    private int selectedLine = -1;

    public EditServerMenu(GameServer server) {
        super("Editing Server: " + server.getName());

        this.server = server;
        manager = plugin.getServerManager();
    }

    private void beginPrompt(Player player, StringPrompt prompt) {
        player.playSound(Sound.CLICK, 1.0f, 1.0f);
        player.closeInventory();
        player.beginConversation(new ConversationFactory(plugin).withFirstPrompt(prompt).withTimeout(60).withModality(false).withLocalEcho(false).buildConversation(player));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, new BackButton());
        buttons.put(10, button -> new ItemBuilder(server.getIcon()).setDisplayName(server.getDisplayName()).build());
        buttons.put(12, new Button() {
            @Override
            public ItemStack getIcon(Player player) {
                return new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName(ChatColor.GREEN + "Display Name")
                        .addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE)
                        .addLore(ChatColor.GRAY + "Changes the display name as shown")
                        .addLore(ChatColor.GRAY + "in the server selector.")
                        .addLore("")
                        .addLore(ChatColor.YELLOW + "Click to change the display name.")
                        .addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE)
                        .build();
            }

            @Override
            public void onClick(Player player, int slot, ClickType type, InventoryAction action) {
                new SignInputSession((lines) -> {
                    String first = lines[0], second = lines[1];
                    if (first.isEmpty() && second.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "You may not leave this as empty!");
                        return;
                    }

                    String displayName = ChatColor.translateAlternateColorCodes('&', first + second);
                    manager.editServer(server -> server.setDisplayName(displayName), server);
                    player.sendMessage(ChatColor.YELLOW + "Set display name of server " + ChatColor.BLUE + server.getName() + ChatColor.YELLOW + " to " + displayName + ChatColor.YELLOW + '.');
                    open(player, false);
                }, "", "", "^^^", "New display name!").start(player);
            }
        });
        buttons.put(13, new Button() {
            @Override
            public ItemStack getIcon(Player player) {
                return new ItemBuilder(Material.ITEM_FRAME)
                        .setDisplayName(ChatColor.GREEN + "Change Icon")
                        .addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE)
                        .addLore(ChatColor.GRAY + "Changes the icon as shown in the")
                        .addLore(ChatColor.GRAY + "server selector.")
                        .addLore("")
                        .addLore(ChatColor.YELLOW + "Drag an item to set as the icon.")
                        .addLore(ChatColor.YELLOW + "Shift + Click to reset the icon.")
                        .addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE)
                        .build();
            }

            @Override
            public void onClick(InventoryClickEvent event) {
                ItemStack item;

                if (event.isShiftClick() && server.hasIcon()) {
                    item = null;
                } else {
                    if ((item = event.getCursor()).getType() == Material.AIR || item.isSimilar(server.getIcon())) return;
                }

                manager.editServer(server -> server.setIcon(item), server);
                update(player);
                player.playSound(Sound.WOOD_CLICK, 1.0f, 1.0f);
            }
        });

        List<String> description = server.getDescription();
        int total = description.size();
        if (selectedLine == -1 && total != 0) {
            selectedLine = 0;
        }

        boolean next = total > selectedLine + 1, previous = selectedLine > 0;

        buttons.put(14, new Button() {
            @Override
            public ItemStack getIcon(Player player) {
                ItemBuilder builder = new ItemBuilder(Material.PAPER);
                builder.setDisplayName(ChatColor.GREEN + "Change Description");
                builder.addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE);

                if (total < 1) {
                    builder.addLore(ChatColor.RED + "This server has no description added!");
                } else {
                    builder.addLore(ChatColor.GRAY + "Description lines:");

                    for (int index = 0; index < description.size(); index++) {
                        String line = description.get(index);
                        builder.addLore((selectedLine == index ? ChatColor.BLUE : ChatColor.GRAY).toString() + ChatColor.BOLD + "▶ " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', line));
                    }
                }

                builder.addLore("");

                if (next) {
                    builder.addLore(ChatColor.YELLOW + "Left-Click to select next line.");
                }

                if (previous) {
                    builder.addLore(ChatColor.YELLOW + "Right-Click to select previous line.");
                }

                if (selectedLine != -1) {
                    builder.addLore(ChatColor.YELLOW + "Middle-Click to edit selected line.");
                    builder.addLore(ChatColor.YELLOW + "Control + Q to delete selected line.");
                }

                builder.addLore(ChatColor.YELLOW + "Shift + Click to add a new line.");
                builder.addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE);
                return builder.build();
            }

            @Override
            public void onClick(InventoryClickEvent event) {
                ClickType click = event.getClick();
                if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                    beginPrompt(player, new ServerAddDescriptionPrompt(server, manager, EditServerMenu.this));
                    return;
                }

                switch (click) {
                    case LEFT:
                    case DOUBLE_CLICK: {
                        if (next) {
                            selectedLine++;
                            update(player);
                            player.playSound(Sound.CLICK, 1.0f, 1.0f);
                        }

                        break;
                    }

                    case RIGHT: {
                        if (previous) {
                            selectedLine--;
                            update(player);
                            player.playSound(Sound.CLICK, 1.0f, 1.0f);
                        }

                        break;
                    }

                    case MIDDLE: {
                        if (selectedLine != -1) {
                            beginPrompt(player, new ServerEditDescriptionPrompt(server, selectedLine, manager, EditServerMenu.this));
                        }

                        break;
                    }

                    case CONTROL_DROP: {
                        if (selectedLine != -1) {
                            new ConfirmationMenu(result -> {
                                switch (result) {
                                    case ACCEPT: {
                                        manager.editServer(server -> server.getDescription().remove(selectedLine), server);
                                        selectedLine = -1;
                                        player.playSound(Sound.FIZZ, 1.0f, 2.0f);
                                    }

                                    case DENY: {
                                        open(player, false);
                                    }
                                }
                            }, "delete selected line.").open(player);
                        }
                    }
                }
            }
        });
        buttons.put(15, new Button() {
            @Override
            public ItemStack getIcon(Player player) {
                return new ItemBuilder(Material.CHEST)
                        .setDisplayName(ChatColor.GREEN + "Change Slot")
                        .addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE)
                        .addLore(ChatColor.GRAY + "Changes the slot as this server")
                        .addLore(ChatColor.GRAY + "icon is shown in the server selector.")
                        .addLore("")
                        .addLore(ChatColor.YELLOW + "Click to change icon slot.")
                        .addLore(ChatColor.GRAY + ITEM_LORE_STRAIGHT_LINE)
                        .build();
            }

            @Override
            public void onClick(Player player, int slot, ClickType type, InventoryAction action) {
                new EditServerSelectorMenu(server).open(player);
                player.playSound(Sound.CLICK, 1.0f, 1.0f);
            }
        });
        return buttons;
    }
}