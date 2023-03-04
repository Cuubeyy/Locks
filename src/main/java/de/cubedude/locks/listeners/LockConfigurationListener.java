package de.cubedude.locks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LockConfigurationListener implements Listener {

    // Called when a player interacts with a lock item
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player is trying to interact with a lock item
        if (item != null && item.getType() == Material.TRIPWIRE_HOOK) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();

            // Open the lock configuration GUI
            Inventory inventory = Bukkit.createInventory(null, 54, "Lock Configuration");

            // Add all online players to the GUI
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                ItemStack playerItem = getHead(onlinePlayer, lore);
                inventory.addItem(playerItem);
            }
            player.openInventory(inventory);
        }
    }

    public static ItemStack getHead(Player player, List<String> lockLore) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(player.getName());
        if (lockLore.contains(player.getName())) {
            skull.setLore(Arrays.asList("Click to remove this player from the lock."));
        } else {
            skull.setLore(Arrays.asList("Click to add this player to the lock."));
        }

        skull.setOwner(player.getName());
        item.setItemMeta(skull);
        return item;
    }

    // Called when a player clicks on an item in the lock configuration GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory inventory = event.getInventory();

        if (!event.getView().getType().equals("Lock Configuration") || clickedItem == null) {
            return;
        }

        if (clickedItem.getType() == Material.LEGACY_SKULL_ITEM) {
            ItemMeta clickedMeta = clickedItem.getItemMeta();
            String playerName = clickedMeta.getDisplayName();

            // Update the lock item's lore to add or remove the player
            ItemStack lockItem = player.getInventory().getItemInMainHand();
            ItemMeta lockMeta = lockItem.getItemMeta();
            List<String> lockLore = lockMeta.getLore();

            if (lockLore != null && lockLore.size() == 2 && lockLore.get(0).equals("Lock") && lockLore.get(1).startsWith("Owner: ")) {
                String owner = lockLore.get(1).substring(7);
                if (owner.equals(player.getName())) {
                    if (lockLore.contains(playerName)) {
                        lockLore.remove(playerName);
                    } else {
                        lockLore.add(playerName);
                    }
                    lockItem.setItemMeta(lockMeta);
                    player.getInventory().setItemInMainHand(lockItem);

                    // Update the lock configuration GUI to reflect the change
                    ItemStack newClickedItem = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
                    ItemMeta newClickedMeta = newClickedItem.getItemMeta();
                    newClickedMeta.setDisplayName(playerName);

                    if (lockLore.contains(playerName)) {
                        newClickedMeta.setLore(Arrays.asList("Click to remove this player from the lock."));
                    } else {
                        newClickedMeta.setLore(Arrays.asList("Click to add this player to the lock."));
                    }

                    newClickedItem.setItemMeta(newClickedMeta);
                    event.setCurrentItem(newClickedItem);
                }
            }

            event.setCancelled(true);
        }
    }
}
