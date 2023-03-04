package de.cubedude.locks.listeners;

import de.cubedude.locks.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LockPlaceListener implements Listener {

    private ConfigManager config;

    public LockPlaceListener(ConfigManager configManager) {
        this.config = configManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if ((block == null || item == null || item.lore() == null) || (!Tag.DOORS.isTagged(block.getType()) && block.getType() != Material.CHEST))
            return;


        if (item.getType() == Material.IRON_INGOT && item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock")) {
            List<Location> locations;
            if (Tag.DOORS.isTagged(block.getType())) {
                locations = getDoorHeight(block);
            } else
                locations = getChestLocations(block);

            for (Location location : locations) {
                String path = location.getWorld().getName() + "." + location.hashCode();

                if (config.getConfig().contains(path + ".X")) {
                    event.setCancelled(true);
                    return;
                }

                config.getConfig().set(path + ".X", location.getBlockX());
                config.getConfig().set(path + ".Y", location.getBlockY());
                config.getConfig().set(path + ".Z", location.getBlockZ());

                List<String> playerNames = new ArrayList<>();
                for (String user : Objects.requireNonNull(item.getLore())) {
                    if (!user.startsWith(ChatColor.BOLD + "Owners:")) {
                        playerNames.add(getUUID(user.trim()));
                    }
                }
                config.getConfig().set(path + ".owners", playerNames);
                config.saveConfig();

                event.setCancelled(true);

                if (player.getInventory().getItemInMainHand().getType() == item.getType()) {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                } else if (player.getInventory().getItemInOffHand().getType() == item.getType()) {
                    player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                }
            }
        }
    }

    private List<Location> getDoorHeight(Block block) {
        List<Location> locations = new ArrayList<>();
        if (block.getLocation().add(0, 1, 0).getBlock().getType() == block.getType()) {
            locations.add(block.getLocation());
            locations.add(block.getLocation().add(0, 1, 0));
            return locations;
        } else {
            locations.add(block.getLocation());
            locations.add(block.getLocation().add(0, -1, 0));
            return locations;
        }
    }

    public List<Location> getChestLocations(Block block) {
        List<Location> chestLocations = new ArrayList<>();
        Chest chest = (Chest) block.getState();
        InventoryHolder holder = chest.getInventory().getHolder();
        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = ((DoubleChest) holder);

            chestLocations.add(doubleChest.getLocation());
            return  chestLocations;
        }
        chestLocations.add(block.getLocation());
        return chestLocations;
    }

    private String getUUID(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            @SuppressWarnings("deprecation")
            String UUIDJson = IOUtils.toString(new URL(url));
            if (UUIDJson.isEmpty()) return "invalid name";
            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);
            return UUIDObject.get("id").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
