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
import java.util.Objects;

public class LockInteractionListener implements Listener {

    private final ConfigManager config;

    public LockInteractionListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    private void onLockInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() == null) return;
        Location location = Getter.getClickedBlockLocation(Objects.requireNonNull(event.getClickedBlock()));
        if (location == null) return;
        Player player = event.getPlayer();
        if (config.getConfig().get(String.valueOf(location.hashCode())) == null) return;
        if (!config.getConfig().contains(String.valueOf(location.hashCode()))) return;
        List<String> owners = (List<String>) config.getConfig().get("" + location.hashCode() + ".owners");
        if (owners == null) return;
        if (!owners.contains(Getter.getUUID(player.getName()))) event.setCancelled(true);
    }
    @EventHandler
    private void onLockDestroy(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Location location = Getter.getClickedBlockLocation(Objects.requireNonNull(event.getClickedBlock()));
        if (location == null) return;
        if (config.getConfig().get(String.valueOf(location.hashCode())) == null) return;
        if (!config.getConfig().contains(String.valueOf(location.hashCode()))) return;
        String owner = (String) config.getConfig().get(location.hashCode() + ".owner");
        if (owner == null) return;
        event.setCancelled(true);
    }
}
