package de.cubedude.locks.listeners;

import de.cubedude.locks.utils.ConfigManager;
import de.cubedude.locks.utils.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class LockInteractionListener implements Listener {

    private final ConfigManager config;

    public LockInteractionListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    private void onLockInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Location location = event.getClickedBlock().getLocation();
        Player player = event.getPlayer();
        if (!config.getConfig().contains("" + location.hashCode())) return;
        List<String> owners = (List<String>) config.getConfig().get("" + location.hashCode() + ".owners");
        if (owners.contains(Getter.getUUID(player.getName()))) return;
        event.setCancelled(true);
    }
}
