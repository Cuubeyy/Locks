package de.cubedude.locks;

import de.cubedude.locks.commands.LockCommand;
import de.cubedude.locks.listeners.LockConfigurationListener;
import de.cubedude.locks.listeners.LockPlacementListener;
import de.cubedude.locks.listeners.LockedOpeningListener;
import de.cubedude.locks.utils.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin {

    public ConfigManager config;

    @Override
    public void onEnable() {
        config = new ConfigManager(this);

        startup_loader();
    }

    private void startup_loader() {
        getServer().getPluginManager().registerEvents(new LockPlacementListener(config, this), this);
        getServer().getPluginManager().registerEvents(new LockConfigurationListener(), this);
        getServer().getPluginManager().registerEvents(new LockedOpeningListener(config, this), this);

        getCommand("lock").setExecutor(new LockCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
