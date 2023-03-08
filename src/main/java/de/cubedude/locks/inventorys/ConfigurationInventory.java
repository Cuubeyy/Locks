package de.cubedude.locks.inventorys;

import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sun.security.krb5.Config;

import javax.swing.text.Position;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigurationInventory implements Listener {

    private final ConfigManager config;

    public ConfigurationInventory(ConfigManager config) {
        this.config = config;
    }

    public static Inventory createInventory(Player player, List<String> lore, Location location, int mode) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Lock Configuration");

        int numPlayers = Bukkit.getOnlinePlayers().size() - 1; // don't count the current player
        int numPages = (int) Math.ceil((double) numPlayers / 45); // calculate the number of pages needed
        int currentPage = 0; // start at the first page

        // add the player heads to the inventory
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == player) continue;
            assert lore != null;
            ItemStack skull = Getter.getPlayerHead(onlinePlayer);

            if (lore.contains(player.getName())) {
                skull.setLore(Collections.singletonList("Click to remove this player from the lock."));
            } else {
                skull.setLore(Collections.singletonList("Click to add this player to the lock."));
            }
            inventory.addItem(skull);
        }

        // add the navigation bar to the bottom row
        ItemStack prevArrow = new ItemStack(Material.ARROW, 1);
        ItemMeta prevMeta = prevArrow.getItemMeta();
        prevMeta.setDisplayName("Previous Page");
        prevArrow.setItemMeta(prevMeta);

        ItemStack nextArrow = new ItemStack(Material.ARROW, 1);
        ItemMeta nextMeta = nextArrow.getItemMeta();
        nextMeta.setDisplayName("Next Page");
        nextArrow.setItemMeta(nextMeta);

        ItemStack infoDiamond = new ItemStack(Material.DIAMOND, 1);
        ItemMeta infoMeta = nextArrow.getItemMeta();
        infoMeta.setDisplayName("Info");
        List<String> infoLore = new ArrayList<>();
        if (mode == 1) {
            infoLore.add("Bitte setzte das Schloss um mehr Infos zu sehen");
        }
        else if (mode == 2) {
            infoLore.add("Das Schloss ist auf einer " + location.getBlock().getType() + ".");
            infoLore.add("X: " + (int) location.x());
            infoLore.add("Y: " + (int) location.y());
            infoLore.add("Z: " + (int) location.z());
        }
        infoMeta.setLore(infoLore);
        infoDiamond.setItemMeta(infoMeta);

        ItemStack currentPageDisplay = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta currentPageMeta = currentPageDisplay.getItemMeta();
        currentPageMeta.setDisplayName("Page " + (currentPage + 1) + "/" + numPages);
        currentPageDisplay.setItemMeta(currentPageMeta);

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, currentPageDisplay);
        }
        inventory.setItem(5*9+2, prevArrow);
        inventory.setItem(5*9+6, nextArrow);
        inventory.setItem(5*9+4, infoDiamond);

        // return the inventory object
        return inventory;
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
