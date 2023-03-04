package de.cubedude.locks.listeners;

import de.cubedude.locks.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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

public class LockPlacementListener implements Listener {

    private ConfigManager config;

    public LockPlacementListener(ConfigManager configManager, JavaPlugin plugin) {
        this.config = configManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if ((block == null || item == null || item.lore() == null) || (!Tag.DOORS.isTagged(block.getType()) && block.getType() != Material.CHEST))
            return;


        if (item.getType() == Material.IRON_INGOT && item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock")) {
            Location location = block.getLocation().add(0, getDoorHeight(block), 0);
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

    private int getDoorHeight(Block block) {
        if (block.getLocation().add(0, 1, 0).getBlock().getType() == block.getType()) return 1;
        return 0;
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
