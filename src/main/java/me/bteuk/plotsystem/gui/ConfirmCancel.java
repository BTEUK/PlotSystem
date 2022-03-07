package me.bteuk.plotsystem.gui;

import java.util.List;

import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.md_5.bungee.api.ChatColor;

public class ConfirmCancel {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Confirm Plot Removal";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.LIME_CONCRETE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Confirm",
				Utils.chat("&fCancels your plot and removes your progress."),
				Utils.chat("&fWARNING will revert all blocks placed to how it was before claiming!"),
				Utils.chat("&fThis can not be reverted!"));

		Utils.createItem(inv, Material.RED_CONCRETE, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Cancel",
				Utils.chat("&fReturns to the plot info."),
				Utils.chat("&fYour plot will not be removed."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		//Get config.
		FileConfiguration config = PlotSystem.getInstance().getConfig();

		PlotData plotData = PlotSystem.getInstance().plotData;
		
		//Get player
		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Confirm")) {

			//Removes the plot.
			List<BlockVector2> vector = WorldGuardFunctions.getPoints(u.currentPlot);

			WorldEditor.updateWorld(vector, Bukkit.getWorld(config.getString("worlds.save")), Bukkit.getWorld(config.getString("worlds.build")));
			ClaimFunctions.removeClaim(u.currentPlot);
			plotData.setStatus(u.currentPlot, "cancelled");
			p.sendMessage(ChatColor.RED + "Plot " + u.currentPlot + " has been removed!");

			p.closeInventory();
			return;

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Cancel")) {

			//Returns the player to the plot info gui.
			p.closeInventory();
			p.openInventory(PlotInfo.GUI(u));
			return;
		}

	}
}
