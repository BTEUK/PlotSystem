package me.bteuk.plotsystem.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class PlotGui {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 5 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Menu";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		PlotData plotData = PlotSystem.getInstance().plotData;
		
		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 45, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the building menu."));



		//Get all plots owned by the player.
		HashMap<Integer, String> plots = plotData.getPlots(u.uuid);

		if (plots == null) {

			toReturn.setContents(inv.getContents());
			return toReturn;
		}

		//Add all the plots to the gui in rows of 7.
		int i = 2;
		int j = 2;

		for (Map.Entry<Integer, String> entry : plots.entrySet()) {

			//If the plot is claimed or submitted add it to the gui.
			if (entry.getValue().equalsIgnoreCase("claimed")) {			
				Utils.createItem(inv, Material.LIME_CONCRETE, 1, (j-1)*9+i, ChatColor.AQUA + "" + ChatColor.BOLD + String.valueOf(entry.getKey()),
						Utils.chat("&fClick to open plot functions."),
						Utils.chat("&fThis plot is currently claimed."),
						Utils.chat("&fYou can submit, remove"),
						Utils.chat("&fand teleport to this plot."));
			}

			if (entry.getValue().equalsIgnoreCase("submitted")) {
				Utils.createItem(inv, Material.LIGHT_BLUE_CONCRETE, 1, (j-1)*9+i, ChatColor.AQUA + "" + ChatColor.BOLD + String.valueOf(entry.getKey()),
						Utils.chat("&fClick to open plot functions."),
						Utils.chat("&fThis plot is currently submitted."),
						Utils.chat("&fYou can retract, remove"),
						Utils.chat("&fand teleport to this plot."));
			}
			
			i += 1;

			if (i>8) {
				i = 2;
				j += 1;
			}				
		}

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {
		
		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			p.openInventory(MainGui.GUI(u));
			return;
		}

		//Get plot id and status of the clicked plot in the gui, set that as the players current plot.
		//Open the plot info gui of that plot.
		String plotid = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
		int id = Integer.parseInt(plotid);
		u.currentPlot = id;

		if (clicked.getType() == Material.LIME_CONCRETE) {
			u.currentStatus = "claimed";
		} else if (clicked.getType() == Material.LIGHT_BLUE_CONCRETE){
			u.currentStatus = "submitted";
		}

		p.closeInventory();
		p.openInventory(PlotInfo.GUI(u));
	}		
}
