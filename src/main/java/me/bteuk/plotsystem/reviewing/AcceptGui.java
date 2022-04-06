package me.bteuk.plotsystem.reviewing;

import java.util.List;

import me.bteuk.network.Network;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.mysql.AcceptData;
import me.bteuk.plotsystem.mysql.BookData;
import me.bteuk.plotsystem.mysql.MessageData;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.mysql.PointsData;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

public class AcceptGui {

	public static UniqueGui createAcceptGui(User user) {

		UniqueGui gui = new UniqueGui(27, Component.text("Review Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

		GlobalSQL globalSQL = PlotSystem.getInstance().globalSQL;
		PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

		gui.setItem(12, Utils.createItem(Material.BOOK, 1,
				Utils.chat("&b&lPlot Info"),
				Utils.chat("&fPlot ID: " + user.review.plot),
				Utils.chat("&fPlot Owner: " + globalSQL.getString("SELECT name FROM player_data WHERE uuid=" +
						plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + user.review.plot + " AND is_owner=1;")
						+ ";"))));

		return gui;

	}

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 6 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Accept Plot";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();
		Accept ac = u.review.accept;

		if (ac.type == 1) {
			int i;
			int j;

			for (j = 1; j<=3; j++) {			
				for (i = 1; i<=5; i++) {

					if (j == 1) {
						if (ac.size<i) {
							Utils.createItem(inv, Material.RED_CONCRETE, 1, 2*9+i+2, ChatColor.AQUA + "" + ChatColor.BOLD + "Size: " + i);
						} else {
							Utils.createItem(inv, Material.LIME_CONCRETE, 1, 2*9+i+2, ChatColor.AQUA + "" + ChatColor.BOLD + "Size: " + i);
						}
					}

					if (j == 2) {
						if (ac.accuracy<i) {
							Utils.createItem(inv, Material.RED_CONCRETE, 1, 3*9+i+2, ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: " + i);
						} else {
							Utils.createItem(inv, Material.LIME_CONCRETE, 1, 3*9+i+2, ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: " + i);
						}
					}

					if (j == 3) {
						if (ac.quality<i) {
							Utils.createItem(inv, Material.RED_CONCRETE, 1, 4*9+i+2, ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: " + i);
						} else {
							Utils.createItem(inv, Material.LIME_CONCRETE, 1, 4*9+i+2, ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: " + i);
						}
					}

				}
			}

			Utils.createItem(inv, Material.MAGENTA_GLAZED_TERRACOTTA, 1, 46, ChatColor.AQUA + "" + ChatColor.BOLD + "Other Plot",
					Utils.chat("&fSwitches accept menu to that of plot"),
					Utils.chat("&fthat does not include at least 1 building"),
					Utils.chat("&fthe size of a small house."));
			Utils.createItem(inv, Material.DIAMOND, 1, 5, ChatColor.AQUA + "" + ChatColor.BOLD + "Submit", Utils.chat("&fAccept the plot with the current settings."));
			Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 54, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the review gui."));
		} else if (ac.type == 2) {

			int i = 1;
			int slot = 21;

			while (i <= 10) {

				if (ac.points<i) {
					Utils.createItem(inv, Material.RED_CONCRETE, 1, slot, ChatColor.AQUA + "" + ChatColor.BOLD + "Points: " + i);
				} else {
					Utils.createItem(inv, Material.LIME_CONCRETE, 1, slot, ChatColor.AQUA + "" + ChatColor.BOLD + "Points: " + i);
				}

				if (i == 5) {
					slot = 30;
					i += 1;
				} else {
					slot += 1;
					i += 1;
				}
			}

			Utils.createItem(inv, Material.MAGENTA_GLAZED_TERRACOTTA, 1, 46, ChatColor.AQUA + "" + ChatColor.BOLD + "Building Plot",
					Utils.chat("&fSwitches accept menu to that of plot"),
					Utils.chat("&fthat includes at least 1 building"),
					Utils.chat("&fthe size of a small house."));
			Utils.createItem(inv, Material.DIAMOND, 1, 5, ChatColor.AQUA + "" + ChatColor.BOLD + "Submit", Utils.chat("&fAccept the plot with the current settings."));
			Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 54, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the review gui."));
		} else if (ac.type == 0) {

			Utils.createItem(inv, Material.BRICKS, 1, 21, ChatColor.AQUA + "" + ChatColor.BOLD + "Building Plot",
					Utils.chat("&fThe plot includes at least 1 building"),
					Utils.chat("&fthe size of a small house."));

			Utils.createItem(inv, Material.OAK_LEAVES, 1, 25, ChatColor.AQUA + "" + ChatColor.BOLD + "Other Plot",
					Utils.chat("&fThe plot does not include a building"),
					Utils.chat("&fat least the size of a small house."));
			Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 54, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the review gui."));
		}


		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;
		Accept ac = u.review.accept;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			//Open the review gui.
			p.closeInventory();
			u.review.reviewGui.open(Network.getInstance().getUser(u.player));
			return;
			//Set the value in the acceptgui based on the button that is clicked.
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Building Plot")) {
			ac.type = 1;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Other Plot")) {
			ac.type = 2;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Size: 1")) {
			ac.size = 1;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Size: 2")) {	
			ac.size = 2;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Size: 3")) {
			ac.size = 3;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Size: 4")) {
			ac.size = 4;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Size: 5")) {
			ac.size = 5;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: 1")) {
			ac.accuracy = 1;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: 2")) {	
			ac.accuracy = 2;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: 3")) {
			ac.accuracy = 3;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: 4")) {
			ac.accuracy = 4;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Accuracy: 5")) {
			ac.accuracy = 5;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: 1")) {
			ac.quality = 1;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: 2")) {
			ac.quality = 2;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: 3")) {
			ac.quality = 3;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: 4")) {
			ac.quality = 4;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Quality: 5")) {
			ac.quality = 5;
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 1")) {
			if (ac.points == 1) {
				ac.points = 0;
			} else {
				ac.points = 1;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 2")) {
			if (ac.points == 2) {
				ac.points = 0;
			} else {
				ac.points = 2;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 3")) {
			if (ac.points == 3) {
				ac.points = 0;
			} else {
				ac.points = 3;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 4")) {
			if (ac.points == 4) {
				ac.points = 0;
			} else {
				ac.points = 4;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 5")) {
			if (ac.points == 5) {
				ac.points = 0;
			} else {
				ac.points = 5;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 6")) {
			if (ac.points == 6) {
				ac.points = 0;
			} else {
				ac.points = 6;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 7")) {
			if (ac.points == 7) {
				ac.points = 0;
			} else {
				ac.points = 7;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 8")) {
			if (ac.points == 8) {
				ac.points = 0;
			} else {
				ac.points = 8;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 9")) {
			if (ac.points == 9) {
				ac.points = 0;
			} else {
				ac.points = 9;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Points: 10")) {
			if (ac.points == 10) {
				ac.points = 0;
			} else {
				ac.points = 10;
			}
			p.getOpenInventory().getTopInventory().setContents(AcceptGui.GUI(u).getContents());
			p.updateInventory();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Submit")) {

			PlayerData playerData = PlotSystem.getInstance().playerData;
			PlotData plotData = PlotSystem.getInstance().plotData;
			BookData bookData = PlotSystem.getInstance().bookData;
			AcceptData acceptData = PlotSystem.getInstance().acceptData;
			MessageData messageData = PlotSystem.getInstance().messageData;
			PointsData pointsData = PlotSystem.getInstance().pointsData;

			FileConfiguration config = PlotSystem.getInstance().getConfig();

			p.closeInventory();

			int i;
			int buildingPoints;

			//Calculate building points
			if (ac.type == 1) {
				buildingPoints = config.getInt("points_base") + ac.size*config.getInt("size_multiplier") + ac.accuracy*config.getInt("accuracy_multiplier") + ac.quality*config.getInt("quality_multiplier");
			} else {
				ac.size = 0;
				ac.accuracy = 0;
				ac.quality = 0;
				buildingPoints = ac.points;
			}
			
			if (u.review.bookMeta.hasPages()) {
				//Get the feedback written in the book.
				List<String> book = u.review.bookMeta.getPages();
				int bookID = bookData.newBookID();
				i = 1;

				//Insert all pages of feedback to the database so they can be retrieved later.
				for(String text: book) {
					if (!(bookData.addPage(bookID, i, text))) {
						p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
						return;
					}
					i++;
				}

				if (!(acceptData.insert(u.review.plot, plotData.getOwner(u.review.plot), u.uuid, bookID, ac.size, ac.accuracy, ac.quality, buildingPoints))) {
					p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
					return;
				}

			} else {
				if (!(acceptData.insert(u.review.plot, plotData.getOwner(u.review.plot), u.uuid, 0, ac.size, ac.accuracy, ac.quality, buildingPoints))) {
					p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
					return;
				}
			}

			messageData.addMessage(plotData.getOwner(u.review.plot), u.review.plot, "accepted");			

			//Add building points and normal points
			playerData.addPoints(plotData.getOwner(u.review.plot), buildingPoints);
			//If points are enabled
			/*
			if (config.getBoolean("points_enabled")) {
				me.bteuk.btepoints.utils.Points.addPoints(plotData.getOwner(u.review.plot), buildingPoints*config.getInt("points_multiplier"));
			}
			*/

			//Remove reviewing status
			plotData.setStatus(u.review.plot, "completed");

			//Add plot to saveWorld
			List<BlockVector2> corners = WorldGuardFunctions.getPoints(u.review.plot);
			WorldEditor.updateWorld(corners, Bukkit.getWorld(config.getString("worlds.build")), Bukkit.getWorld(config.getString("worlds.save")));

			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Plot " + ChatColor.DARK_AQUA + u.review.plot + ChatColor.GREEN + " accepted for " + ChatColor.DARK_AQUA + buildingPoints + ChatColor.GREEN + " building points.");

			i = 1;
			//Log plot corners to the database
			for (BlockVector2 corner: corners) {
				pointsData.addPoint(u.review.plot, i, corner.getX(), corner.getZ());
				i++;
			}

			//Remove plot from worldguard
			ClaimFunctions.removeClaim(u.review.plot);

			p.sendMessage(ChatColor.GREEN + "Plot " + u.review.plot + " accepted for " + buildingPoints + " building points.");
			if (plotData.reviewExists(u)) {
				if (plotData.reviewCount(u) == 1) {
					p.sendMessage(ChatColor.GREEN + "There is 1 plot available for review.");
				} else {
					p.sendMessage(ChatColor.GREEN + "There are " + plotData.reviewCount(u) + " plots available for review.");
				}
			}
			u.review.editBook.unregister();
			u.player.getInventory().setItem(4, u.review.previousItem);
			u.review = null;

			//If in the save world teleport back to the build world.
			if (p.getWorld().equals(Bukkit.getWorld(config.getString("worlds.save")))) {
				Location l = p.getLocation();
				l.setWorld(Bukkit.getWorld(config.getString("worlds.build")));
				p.teleport(l);
			}

		} else {}

	}

}
