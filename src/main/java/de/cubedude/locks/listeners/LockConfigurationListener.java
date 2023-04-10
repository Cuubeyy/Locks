package de.cubedude.locks.listeners;

import de.cubedude.locks.inventorys.ConfigurationInventory;
import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class LockConfigurationListener implements Listener {

    private ConfigManager config;

    public LockConfigurationListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    private void openLockConfiguration(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            ConfigurationInventory inventory = new ConfigurationInventory(config, player, Getter.getClickedBlockLocation(event.getClickedBlock()));
            player.openInventory(inventory.getInventory());
        } else if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock")) {
            ConfigurationInventory inventory = new ConfigurationInventory(config, player, player.getLocation());
            player.openInventory(inventory.getInventory());
        }
    }

    @EventHandler
    private void configurationInventoryHandler(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Configuration")) return;

        event.setCancelled(true);
    }
}
