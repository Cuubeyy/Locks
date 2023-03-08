package de.cubedude.locks.utils;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;

public class Getter {
    public Location getDoorLocation(Block block) {
        if (block.getLocation().add(0, 1, 0).getBlock().getType() == block.getType()) return block.getLocation().add(0, 1, 0);
        return block.getLocation();
    }

    public Location getChestLocations(Block block) {
        Chest chest = (Chest) block.getState();
        InventoryHolder holder = chest.getInventory().getHolder();
        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = ((DoubleChest) holder);

            return doubleChest.getLocation();
        }
        return block.getLocation();
    }

    public String getUUID(String name) {
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

    public static ItemStack getPlayerHead(Player player) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(player.getName());
        skull.setOwner(player.getName());
        item.setItemMeta(skull);
        return item;
    }
}
