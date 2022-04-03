package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.gui.UniqueGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.md_5.bungee.api.ChatColor;

public class ReviewGui {

	public static UniqueGui createReviewGui(User u) {

		UniqueGui gui = new UniqueGui(27, Component.text("Review xyz", NamedTextColor.AQUA, TextDecoration.BOLD));

		return gui;

	}

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Review Menu";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		PlayerData playerData = PlotSystem.getInstance().playerData;
		PlotData plotData = PlotSystem.getInstance().plotData;
		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the building menu."));
		
		Utils.createItem(inv, Material.BOOK, 1, 5, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Info",
				Utils.chat("&fPlot ID: " + u.review.plot),
				Utils.chat("&fPlot Owner: " + playerData.getName(plotData.getOwner(u.review.plot))));
		
		Utils.createItem(inv, Material.GRASS_BLOCK, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Before View",
				Utils.chat("&fTeleport to the plot before it was claimed."));
		Utils.createItem(inv, Material.STONE_BRICKS, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Current View",
				Utils.chat("&fTeleport to the current view of the plot."));
		Utils.createItem(inv, Material.LIME_CONCRETE, 1, 11, ChatColor.AQUA + "" + ChatColor.BOLD + "Accept Plot",
				Utils.chat("&fOpens the accept gui."));
		Utils.createItem(inv, Material.RED_CONCRETE, 1, 17, ChatColor.AQUA + "" + ChatColor.BOLD + "Deny Plot",
				Utils.chat("&fOpens the deny gui."));
		
		Utils.createItem(inv, Material.WRITABLE_BOOK, 1, 23, ChatColor.AQUA + "" + ChatColor.BOLD + "Feedback",
				Utils.chat("&fOpens the feedback book."),
				Utils.chat("&fWrite your feedback in here."),
				Utils.chat("&fFor denying this is manditory,"),
				Utils.chat("&fwhen accepting it is optional."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;
		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			p.openInventory(MainGui.GUI(u));
			return;
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Before View")) {
			//Get the plot that is being reviewed and teleport the player do the plot in the saveWorld.
			p.teleport(WorldGuardFunctions.getBeforeLocation(u.review.plot));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Current View")) {
			//Get the plot that is being reviwed and teleport the player to the plot in the buildWorld.
			p.teleport(WorldGuardFunctions.getCurrentLocation(u.review.plot));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Accept Plot")) {
			//Open the acceptgui with default values.
			p.closeInventory();
			p.openInventory(AcceptGui.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Deny Plot")) {
			p.closeInventory();
			
			if (u.review.bookMeta.hasPages()) {
				p.openInventory(DenyGui.GUI(p));
			} else {
				p.sendMessage(ChatColor.RED + "You must give feedback before you can deny the plot.");
			}
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Feedback")) {
					
			u.review.book.setItemMeta(u.review.bookMeta);
			u.review.previousItem = p.getInventory().getItem(4);
			
			p.getInventory().setItem(4, u.review.book);
			p.closeInventory();
			p.sendMessage(ChatColor.GREEN + "Please write the feedback in the book.");
			
		} else {}
	}

}
