package de.cubedude.locks.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class LockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player is holding an item in their main hand
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem.getType() != Material.AIR) {
            player.sendMessage("You must have an empty hand to receive a new lock.");
            return true;
        }

        ItemStack lockItem = generateLock(player);
        player.getInventory().setItemInMainHand(lockItem);

        player.sendMessage("You have received a new lock.");
        return true;
    }

    public static ItemStack generateLock(Player player) {
        ItemStack lockItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta lockMeta = lockItem.getItemMeta();
        lockMeta.setDisplayName(ChatColor.YELLOW + "Lock");
        lockMeta.setLore(Arrays.asList(ChatColor.BOLD + "Owners:", player.getDisplayName()));
        lockItem.setItemMeta(lockMeta);
        return lockItem;
    }
}
