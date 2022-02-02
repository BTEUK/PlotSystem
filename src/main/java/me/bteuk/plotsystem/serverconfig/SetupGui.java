package me.bteuk.plotsystem.serverconfig;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class SetupGui {
	
	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;
	
	private static int stage;
	
	private static boolean tutorial_only;
	private static boolean plots_only;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Server Configuration Menu";

		inv = Bukkit.createInventory(null, inv_rows);

		stage = 1;
	}

	public static Inventory Gui(Player p) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();
		
		if (stage == 1) {
			
			//Select server type, plots only, tutorial only, both.
			Utils.createItem(inv, Material.LIME_CONCRETE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Plots Only",
					Utils.chat("&fThis will setup the server to only allow plot worlds."));
			
			Utils.createItem(inv, Material.LIME_CONCRETE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Only",
					Utils.chat("&fThis will setup the server for the tutorial, but nothing else."));
			
			Utils.createItem(inv, Material.LIME_CONCRETE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Plots and Tutorial",
					Utils.chat("&fThis will setup the server to have both plots and the tutorial on the same server."));
			
		} else if (stage == 2) {
			
			//Setup the starting worlds, depending on the selected setup.
			if (tutorial_only) {
				
				Utils.createItem(inv, Material.LIME_CONCRETE, 1, 14, ChatColor.AQUA + "" + ChatColor.BOLD + "Create Tutorial World",
						Utils.chat("&fClick here to create the tutorial world."));
				
			} else if (plots_only) {
				
				Utils.createItem(inv, Material.LIME_CONCRETE, 1, 14, ChatColor.AQUA + "" + ChatColor.BOLD + "Create New Plot World",
						Utils.chat("&fClick here to create the tutorial world."));
				
			} else {
				
				Utils.createItem(inv, Material.LIME_CONCRETE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Create Tutorial World",
						Utils.chat("&fClick here to create the tutorial world."));
				
				Utils.createItem(inv, Material.LIME_CONCRETE, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Create New Plot World",
						Utils.chat("&fClick here to create the tutorial world."));
				
			}			
		}

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv, NavigationSQL navigationSQL, PlotSQL plotSQL) {
		
		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plots Only")) {
			plots_only = true;
			tutorial_only = false;
			stage = 2;
			p.getInventory().setContents(SetupGui.inv.getContents());
			p.updateInventory();
			return;
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Only")) {
			plots_only = false;
			tutorial_only = true;
			stage = 2;
			p.getInventory().setContents(SetupGui.inv.getContents());
			p.updateInventory();
			return;
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plots and Tutorial")) {
			plots_only = false;
			tutorial_only = false;
			stage = 2;
			p.getInventory().setContents(SetupGui.inv.getContents());
			p.updateInventory();
			return;
		}
		
		
	}	

}
