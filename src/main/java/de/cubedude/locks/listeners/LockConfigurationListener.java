package de.cubedude.locks.listeners;

import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.List;

public class LockConfigurationListener implements Listener {

    private ConfigManager config;

    public LockConfigurationListener(ConfigManager config) { this.config = config; }

    // Called when a player interacts with a lock item
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK|| item == null || item.getType() != Material.IRON_INGOT ||
                item.getItemMeta() == null || !item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock"))
            return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        Inventory inventory = createInventory(player, lore, "");
        player.openInventory(inventory);
    }

    @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking() || event.getClickedBlock() == null || !Tag.DOORS.isTagged(event.getClickedBlock().getType())) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Getter getter = new Getter();
        Location location = getter.getDoorLocation(block);
        String path = "" + location.hashCode();

        if (!config.getConfig().contains(path)) return;

        List<String> players = (List<String>) config.getConfig().get(path + ".owners");

        assert players != null;
        if (!players.get(0).trim().equals(getter.getUUID(player.getName()).trim())) {
            event.setCancelled(true);
            return;
        }

        Inventory inventory = createInventory(player, players, "" + location.hashCode());
        player.openInventory(inventory);

        event.setCancelled(true);
    }

    public Inventory createInventory(Player player, List<String> lore, String mode) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Lock Configuration" + mode);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == player) continue;
            assert lore != null;
            ItemStack playerItem = getHead(onlinePlayer, lore);
            inventory.addItem(playerItem);
        }
        return inventory;
    }

    public static ItemStack getHead(Player player, List<String> lockLore) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(player.getName());
        if (lockLore.contains(player.getName())) {
            skull.setLore(Collections.singletonList("Click to remove this player from the lock."));
        } else {
            skull.setLore(Collections.singletonList("Click to add this player to the lock."));
        }

        skull.setOwner(player.getName());
        item.setItemMeta(skull);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String path = "";
        int mode;
        if (event.getView().getTitle().equals("Lock Configuration")) mode = 1;
        else if (event.getView().getTitle().startsWith("Lock Configuration")) {
            path = event.getView().getTitle().replace("Lock Configuration", "");
            mode = 2;
        } else return;

        Player player = (Player) event.getView().getPlayer();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack item;

        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {
            event.setCancelled(true);
            return;
        }
        if (mode == 1) {
            if (player.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT &&
                    player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock"))
                item = player.getInventory().getItemInMainHand();
            else item = player.getInventory().getItemInOffHand();
        } else {
            List<String> players = (List<String>) config.getConfig().get(path + ".owners");
            item = new ItemStack(Material.IRON_INGOT);
            player.sendMessage(path);
            player.sendMessage(players.get(0));
            item.setLore(players);
        }

        List<String> lore = item.getLore();
        String name = clickedItem.getItemMeta().getDisplayName();

        assert lore != null;
        if (lore.contains(name)) {
            lore.remove(name);
            clickedItem.setLore(Collections.singletonList("Click to add this player to the lock."));
        }
        else {
            lore.add(name);
            clickedItem.setLore(Collections.singletonList("Click to remove this player from the lock."));
        }

        item.setLore(lore);

        if (mode == 2) {
            config.getConfig().set(path + ".owners", item.getLore());
        }

        event.setCancelled(true);
    }
}
