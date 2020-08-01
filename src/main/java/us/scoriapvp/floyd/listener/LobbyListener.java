package us.scoriapvp.floyd.listener;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.VisibilityRule;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import us.scoriapvp.floyd.Floyd;
import us.scoriapvp.floyd.server.menu.LobbySelectorMenu;
import us.scoriapvp.floyd.server.menu.ServerSelectorMenu;
import us.scoriapvp.floyd.user.User;
import us.scoriapvp.nightmare.NightmareConfiguration;
import us.scoriapvp.nightmare.tag.TagMenu;
import us.scoriapvp.nightmare.util.item.ItemBuilder;

public final class LobbyListener implements Listener {

    public static final ItemStack SERVER_SELECTOR_ITEM = new ItemBuilder(Material.COMPASS).setDisplayName(NightmareConfiguration.PRIMARY_COLOR + "Server Selector " + ChatColor.GRAY + "(Right Click)").build();
    public static final ItemStack CHAT_TAGS_ITEM = new ItemBuilder(Material.NAME_TAG).setDisplayName(NightmareConfiguration.PRIMARY_COLOR + "Chat Tags " + ChatColor.GRAY + "(Right Click)").build();
    public static final ItemStack LOBBY_SELECTOR_ITEM = new ItemBuilder(Material.NETHER_STAR).setDisplayName(NightmareConfiguration.PRIMARY_COLOR + "Lobby Selector " + ChatColor.GRAY + "(Right Click)").build();

    private final ImmutableMap<VisibilityRule, ItemStack> visibilityItems;

    private final Floyd plugin;

    public LobbyListener(Floyd plugin) {
        this.plugin = plugin;

        ImmutableMap.Builder<VisibilityRule, ItemStack> builder = ImmutableMap.builder();

        for (VisibilityRule rule : VisibilityRule.values()) {
            builder.put(rule, new ItemBuilder(Material.INK_SACK, 1, (short) rule.getDyeColor())
                    .setDisplayName(NightmareConfiguration.PRIMARY_COLOR + "Visibility" + ChatColor.GRAY + ": " + rule.getColor() + (rule == VisibilityRule.EVERYONE ? "All" : WordUtils.capitalizeFully(rule.name())) + ChatColor.GRAY + " (Right Click)")
                    .build());
        }

        visibilityItems = builder.build();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(event.getPlayer().getWorld().getSpawnLocation().add(0.5, 1.0, 0.5));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setGameMode(GameMode.ADVENTURE);
        player.setWalkSpeed(0.2f);
        player.spigot().setCollidesWithEntities(false);

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setHeldItemSlot(0);
        inventory.setItem(0, SERVER_SELECTOR_ITEM);
        inventory.setItem(4, CHAT_TAGS_ITEM);

        User user = plugin.getUserManager().getUser(player);
        if (user != null) {
            VisibilityRule rule = user.getVisibilityRule();
            player.setVisibilityRule(rule, false);
            inventory.setItem(7, visibilityItems.get(rule));
        }

        inventory.setItem(8, LOBBY_SELECTOR_ITEM);

        player.updateInventory();

        if (player.hasPermission("nightmare.command.flight")) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        inventory.setItem(0, SERVER_SELECTOR_ITEM);
        inventory.setItem(4, CHAT_TAGS_ITEM);
        inventory.setItem(7, visibilityItems.get(player.getVisibilityRule()));
        inventory.setItem(8, LOBBY_SELECTOR_ITEM);
        inventory.setHeldItemSlot(0);
        player.updateInventory();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (player.getFoodLevel() > event.getFoodLevel()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (!event.hasItem()) return;

            Player player = event.getPlayer();
            PlayerInventory inventory = player.getInventory();
            User user = plugin.getUserManager().getUser(player);

            ItemStack item = event.getItem();
            if (item.isSimilar(SERVER_SELECTOR_ITEM)) {
                event.setCancelled(true);

                new ServerSelectorMenu().open(player);
            } else if (item.isSimilar(CHAT_TAGS_ITEM)) {
                event.setCancelled(true);

                new TagMenu(user.getProfile()).open(player);
            } else if (visibilityItems.containsValue(item)) {
                event.setCancelled(true);

                VisibilityRule next = user.getVisibilityRule().next();
                user.setVisibilityRule(next);
                inventory.setItem(7, visibilityItems.get(next));
            } else if (item.isSimilar(LOBBY_SELECTOR_ITEM)) {
                event.setCancelled(true);

                new LobbySelectorMenu().open(player);
            }
        }
    }
}