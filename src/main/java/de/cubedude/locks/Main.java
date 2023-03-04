package de.cubedude.locks;

import de.cubedude.locks.commands.LockCommand;
import de.cubedude.locks.listeners.LockConfigurationListener;
import de.cubedude.locks.listeners.LockPlaceListener;
import de.cubedude.locks.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public ConfigManager config;

    @Override
    public void onEnable() {
        config = new ConfigManager(this);

        startup_loader();
    }

    private void startup_loader() {
        getServer().getPluginManager().registerEvents(new LockPlaceListener(config), this);
        getServer().getPluginManager().registerEvents(new LockConfigurationListener(), this);

        getCommand("lock").setExecutor(new LockCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
