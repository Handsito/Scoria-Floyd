package us.scoriapvp.floyd.listener;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import us.scoriapvp.nightmare.util.BukkitUtils;

public final class EnvironmentListener implements Listener {

    private static final ImmutableSet<Material> BLOCK_INTERACTABLES = Sets.immutableEnumSet(Material.BED, Material.BED_BLOCK, Material.BEACON, Material.FENCE_GATE, Material.IRON_DOOR, Material.TRAP_DOOR, Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.WORKBENCH, Material.BURNING_FURNACE, Material.BREWING_STAND, Material.HOPPER, Material.DROPPER, Material.DISPENSER, Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.LEVER, Material.FIRE);

    public boolean attemptBuild(Entity entity, String denyMessage) {
        boolean allowed = false;

        Player player = entity instanceof Player ? (Player) entity : null;
        if (player != null) {
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("floyd.build")) {
                allowed = true;
            }

            if (!allowed && !Strings.isNullOrEmpty(denyMessage)) {
                player.sendMessage(denyMessage);
            }
        }

        return allowed;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Player player = event.getPlayer();

        Action action = event.getAction();
        if (action == Action.PHYSICAL) {
            if (!attemptBuild(player, null)) {
                event.setCancelled(true);
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            if (BLOCK_INTERACTABLES.contains(event.getClickedBlock().getType()) && !attemptBuild(player, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!attemptBuild(player, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!attemptBuild(player, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (!attemptBuild(player, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (!attemptBuild(player, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (remover instanceof Player) {
            if (!attemptBuild(remover, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (!attemptBuild(player, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Hanging) {
            Player attacker = BukkitUtils.getFinalAttacker(event, false);
            if (!attemptBuild(attacker, ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingInteractByPlayer(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Hanging) {
            if (!attemptBuild(event.getPlayer(), ChatColor.RED + "You may not build or replace blocks in the lobby.")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWitherChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Wither || entity instanceof EnderDragon) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSkullInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            BlockState state = event.getClickedBlock().getState();
            if (state instanceof Skull) {
                Skull skull = (Skull) state;
                player.sendMessage(ChatColor.YELLOW + "This head belongs to " + ChatColor.WHITE + (skull.getSkullType() == SkullType.PLAYER && skull.hasOwner() ? skull.getOwner() : "a " + WordUtils.capitalizeFully(skull.getSkullType().name()) + " skull") + ChatColor.YELLOW + '.');
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                entity.teleport(entity.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        switch (event.getBlock().getType()) {
            case ICE:
            case SNOW:
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }
}