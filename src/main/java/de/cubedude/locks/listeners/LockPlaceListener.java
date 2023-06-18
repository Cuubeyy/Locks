package de.cubedude.locks.listeners;

import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LockPlaceListener implements Listener {

    private ConfigManager config;
    private Getter getter;


    public LockPlaceListener(ConfigManager configManager) {
        this.config = configManager;
        getter = new Getter();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if (event.getClickedBlock() == null || event.getItem() == null) return;
        if (item.getType() == Material.IRON_INGOT && item.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lock")) {
            Location location;
            location = Getter.getClickedBlockLocation(block);
            if (location == null) return;
            String path = "" + location.hashCode();

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
                    playerNames.add(getter.getUUID(user.trim()));
                }
            }
            config.getConfig().set(path + ".owner", playerNames.get(0));
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
