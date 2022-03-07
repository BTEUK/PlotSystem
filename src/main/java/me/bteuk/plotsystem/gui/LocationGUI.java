package me.bteuk.plotsystem.gui;

import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;

public class LocationGUI {
	
	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;
	
	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Locations";
		
		inv = Bukkit.createInventory(null, inv_rows);
		
	}
	
	public static Inventory GUI (User u) {
		
		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);
		
		inv.clear();
		
		Utils.createItem(inv, Material.SPRUCE_FENCE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "London, Cranham",
				Utils.chat("&fCranham is a residential area of east London"),
				Utils.chat("&fin the London Borough of Havering."));
		
		Utils.createItem(inv, Material.ACACIA_FENCE, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Solihull, Monkspath",
				Utils.chat("&fMonkspath is a residential area of Solihull."),
				Utils.chat("&fSituated near the M42/A34 junction"),
				Utils.chat("&fSolihull is a large town in the West Midlands."));
		
		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", 
				Utils.chat("&fGo back to the building menu."));	
		
		toReturn.setContents(inv.getContents());
		return toReturn;
	}
	
	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {
		
		Player p = u.player;
		
		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "London, Cranham")) {
			
			//Will teleport the player to the map for this location.
			p.teleport(PlotSystem.cranham);
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Solihull, Monkspath")) {
			
			//Will teleport the player to the map for this location.
			p.teleport(PlotSystem.monkspath);
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			p.openInventory(MainGui.GUI(u));
		}
	}

}
