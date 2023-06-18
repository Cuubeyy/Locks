package de.cubedude.locks.listeners;

import de.cubedude.locks.commands.LockCommand;
import de.cubedude.locks.inventorys.ConfigurationInventory;
import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockConfigurationListener implements Listener {

    private ConfigManager config;

    public LockConfigurationListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    private void openLockConfiguration(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
            if (!config.getConfig().contains(String.valueOf(Getter.getClickedBlockLocation(event.getClickedBlock()).hashCode()))) return;
            String owner = (String) config.getConfig().get(Getter.getClickedBlockLocation(event.getClickedBlock()).hashCode() + ".owner");
            if (owner == null) return;
            if (!owner.equals(Getter.getUUID(player.getName()))) return;
            ConfigurationInventory inventory = new ConfigurationInventory(config, player, Getter.getClickedBlockLocation(event.getClickedBlock()));
            Inventory inv = inventory.getInventory();
            if (inv == null) return;
            player.openInventory(inv);
            event.setCancelled(true);
        } else if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getItem() != null) {
            if (!event.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock")) return;
            ConfigurationInventory inventory = new ConfigurationInventory(config, player, player.getLocation());
            Inventory inv = inventory.getInventory();
            if (inv != null) player.openInventory(inv);
        }
    }

    @EventHandler
    private void configurationInventoryHandler(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Configuration")) return;
        ItemStack item = event.getInventory().getItem(22);
        if (item == null) return;
        List<String> owners = item.getLore();
        String location = owners.get(owners.size()-1);
        owners.remove(owners.size()-1);

        if (event.getRawSlot() >= 19 || event.getCurrentItem() == null) { event.setCancelled(true); return; }

        if (event.getCurrentItem().getType().equals(Material.BARRIER) && event.getRawSlot() == 18) {
            ItemStack lockItem = LockCommand.generateLock((Player) event.getWhoClicked());

            // Do something with the lockIngot, such as adding it to a player's inventory
            event.getWhoClicked().getInventory().addItem(lockItem);
            event.setCancelled(true);
            event.getInventory().close();

            config.getConfig().set(location, null);
            config.saveConfig();
            return;
        }

        ItemStack skull = event.getCurrentItem();
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        Player owner = Bukkit.getPlayer(skullMeta.getOwner());

        if (config.getConfig().contains(location)) {
            // Lock is placed => change in config
            String path = location + ".owners";
            List<String> configOwners = (List<String>) config.getConfig().get(path);

            if (!configOwners.contains(Getter.getUUID(owner.getName()))) {
                owners.add(owner.getDisplayName()); owners.add(location);
                item.setLore(owners);

                ((List<String>) config.getConfig().get(path)).add(Getter.getUUID(owner.getName()));

                skullMeta.setLore(Arrays.asList("§cClick to remove the player from the lock"));
                skull.setItemMeta(skullMeta);
            } else {
                owners.remove(owner.getDisplayName()); owners.add(location);
                item.setLore(owners);

                ((List<String>) config.getConfig().get(path)).remove(Getter.getUUID(owner.getName()));

                skullMeta.setLore(Arrays.asList("§aClick to add the player to the lock"));
                skull.setItemMeta(skullMeta);
            }
            config.saveConfig();

        } else {
            // Lock is in Hand => change on Item in Hand
            Player player = (Player) event.getWhoClicked();
            ItemStack itemInHand = player.getItemInHand();
            List<String> lore = itemInHand.getLore();

            if (!owners.contains(owner.getName())) {
                owners.add(owner.getDisplayName()); owners.add(location);
                item.setLore(owners);

                lore.add(owner.getDisplayName());

                skullMeta.setLore(Arrays.asList("§cClick to remove the player from the lock"));
                skull.setItemMeta(skullMeta);
            } else {
                owners.remove(owner.getDisplayName()); owners.add(location);
                item.setLore(owners);

                lore.remove(owner.getName());

                skullMeta.setLore(Arrays.asList("§aClick to add the player to the lock"));
                skull.setItemMeta(skullMeta);
            }

            itemInHand.setLore(lore);
            player.setItemInHand(itemInHand);
        }
        event.setCancelled(true);
    }
}
