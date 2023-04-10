package de.cubedude.locks.inventorys;

import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class ConfigurationInventory {

    private Player player;
    private ConfigManager config;
    private Inventory inventory;
    private Location location;

    public ConfigurationInventory(ConfigManager config, Player player, Location location) {
        this.config = config;
        this.location = location;
        this.player = player;

        inventory = createInventory();
    }

    private Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, "Configuration");
        inventory = fillInventory(inventory);
        return inventory;
    }

    private Inventory fillInventory(Inventory inv) {
        for (int i = 18; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
            inv.setItem(i, glass);
        }

        ItemStack infoItem = setLockInformation();
        inv.setItem(22, infoItem);

        inv = setPlayerHeads(inv, player);

        return inv;
    }

    private Inventory setPlayerHeads(Inventory inv, Player playerToSkip) {
        List<Player> players = (List<Player>) Bukkit.getServer().getOnlinePlayers();
        int slot = 0;
        for (Player player : players) {
            if (player.equals(playerToSkip)) {
                continue;
            }
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(player.getDisplayName());
            if (inv.getItem(22) != null && inv.getItem(22).hasItemMeta()) {
                ItemMeta itemMeta = inv.getItem(22).getItemMeta();
                if (itemMeta.hasLore() && itemMeta.getLore().contains(player.getDisplayName())) {
                    skullMeta.setLore(Arrays.asList("§cClick to remove the player from the lock"));
                } else {
                    skullMeta.setLore(Arrays.asList("§aClick to add the player to the lock"));
                }
            }
            skull.setItemMeta(skullMeta);
            inv.setItem(slot++, skull);
            if (slot >= 18) {
                break;
            }
        }
        return inv;
    }

    private ItemStack setLockInformation() {
        ItemStack item = new ItemStack(Material.DIAMOND, 1);
        List<String> lore = new ArrayList<String>();

        if (config.getConfig().contains("" + location.hashCode())) {
            List<String> owners = (List<String>) config.getConfig().get(location.hashCode() + ".owners");
            for (String owner : owners) {
                owner = Getter.getPlayerFromUUID(owner).getDisplayName();
                lore.add(owner);
            }
            lore.add("" + location.hashCode());

            item.setLore(lore);
        } else {
            List<String> owners = player.getItemInHand().getLore();
            owners.remove(0);
            for (String owner : owners) {
                lore.add(owner);
            }
            lore.add("" + location.hashCode());

            item.setLore(lore);
        }

        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
