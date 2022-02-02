package me.bteuk.plotsystem.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class BuildGui {
	
	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Uknet Menu";

		inv = Bukkit.createInventory(null, inv_rows);
	}

	public static Inventory Gui(User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.BRICK, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Build",
				Utils.chat("&fClick here if you wish to build in the UK."));
		
		Utils.createItem(inv, Material.SPRUCE_BOAT, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Visit",
				Utils.chat("&fClick here if you wish to visit various locations in the UK."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {
		
		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Build")) {
			p.closeInventory();
			p.openInventory(BuildGui.Gui(u));
			return;
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Visit")) {
			p.closeInventory();
			p.openInventory(VisitGui.Gui(u));
			return;
		}
	}	

}
