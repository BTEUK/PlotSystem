package me.bteuk.plotsystem.reviewing;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.BookData;
import me.bteuk.plotsystem.mysql.DenyData;
import me.bteuk.plotsystem.mysql.MessageData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.mysql.PointsData;
import me.bteuk.plotsystem.utils.ClaimFunctions;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.WorldEditor;
import me.bteuk.plotsystem.utils.WorldGuardFunctions;
import net.md_5.bungee.api.ChatColor;

public class DenyGui {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Deny Plot";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (Player p) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the review menu."));

		Utils.createItem(inv, Material.LIME_CONCRETE, 1, 12, ChatColor.AQUA + "" + ChatColor.BOLD + "Another Chance", Utils.chat("&fDeny the plot and return it to the builder."));

		Utils.createItem(inv, Material.YELLOW_CONCRETE, 1, 14, ChatColor.AQUA + "" + ChatColor.BOLD + "Resize plot",
				Utils.chat("&fDeny the plot and return it with a larger area."),
				Utils.chat("&fYour selection must include all of the existing area."));

		Utils.createItem(inv, Material.RED_CONCRETE, 1, 16, ChatColor.AQUA + "" + ChatColor.BOLD + "Remove Plot", Utils.chat("&fDeny the plot and return it to its orginal state."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;

		FileConfiguration config = Main.getInstance().getConfig();

		PlotData plotData = Main.getInstance().plotData;
		DenyData denyData = Main.getInstance().denyData;
		BookData bookData = Main.getInstance().bookData;
		MessageData messageData = Main.getInstance().messageData;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			//Open the review gui.
			p.closeInventory();
			p.openInventory(ReviewGui.GUI(u));
			return;

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Another Chance")) {

			p.closeInventory();

			//Get the feedback written in the book.
			List<String> book = u.review.bookMeta.getPages();
			int bookID = bookData.newBookID();
			int i = 1;

			//Insert all pages of feedback to the database so they can be retrieved later.
			for(String text: book) {
				if (!(bookData.addPage(bookID, i, text))) {
					p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
					return;
				}
				i++;
			}

			//If the feedback was stored successfully log the plot deny.
			if (denyData.insert(u.review.plot, plotData.getOwner(u.review.plot), u.uuid, bookID, "returned")) {
				//Add message for the plot owner.
				messageData.addMessage(plotData.getOwner(u.review.plot), u.review.plot, "returned");
				//Set the status back to claimed
				plotData.setStatus(u.review.plot, "claimed");
				//Set the last enter time to now to prevent inactivity delete.
				plotData.setLastVisit(u.review.plot);
				//Remove the reviewer from the plot
				WorldGuardFunctions.removeMember(u.review.plot, u.uuid);
				//Unregister the book listener and reset the feedback book inventory slot to previous item.
				u.review.editBook.unregister();				
				u.player.getInventory().setItem(4, u.review.previousItem);
				//Send feedback and clear review.
				p.sendMessage(ChatColor.GREEN + "Plot " + u.review.plot + " denied and returned to the plot owner.");	
				u.review = null;
				//If another plot is submitted tell the reviewer.
				if (plotData.reviewExists(u)) {
					if (plotData.reviewCount(u) == 1) {
						p.sendMessage(ChatColor.GREEN + "There is 1 plot available for review.");
					} else {
						p.sendMessage(ChatColor.GREEN + "There are " + plotData.reviewCount(u) + " plots available for review.");
					}
				}

				//Teleport back to the build world if in the save world
				if (p.getWorld().equals(Bukkit.getWorld(config.getString("worlds.save")))) {
					Location l = p.getLocation();
					l.setWorld(Bukkit.getWorld(config.getString("worlds.build")));
					p.teleport(l);
				}
			
			} else {
				p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
			}


		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Resize Plot")) {
			
			p.closeInventory();

			//Check if the selection for the resize is correct.
			if (!(ClaimFunctions.checkEdit(p, u.review.plot, plotData.getOwner(u.review.plot), u.plots.vector))) {
				return;
			}
			
			//Get the feedback written in the book.
			List<String> book = u.review.bookMeta.getPages();
			int bookID = bookData.newBookID();
			int i = 1;

			//Insert all pages of feedback to the database so they can be retrieved later.
			for(String text: book) {
				if (!(bookData.addPage(bookID, i, text))) {
					p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
					return;
				}
				i++;
			}

			//If the feedback was stored successfully log the plot deny.
			if (denyData.insert(u.review.plot, plotData.getOwner(u.review.plot), u.uuid, bookID, "resized")) {
				//Add message for the plot owner.
				messageData.addMessage(plotData.getOwner(u.review.plot), u.review.plot, "resized");
				//Set status back to claimed
				plotData.setStatus(u.review.plot, "claimed");
				//Set last enter to now to prevent inactivity deletion
				plotData.setLastVisit(u.review.plot);
				//Remove the reviewer from the plot.
				WorldGuardFunctions.removeMember(u.review.plot, u.uuid);
				
				//Attempt to resize the plot.
				if (!(ClaimFunctions.resizePlot(u.review.plot, plotData.getOwner(u.review.plot), u.plots.vector))) {
					p.sendMessage(ChatColor.RED + "An error occured while resizing the plot, please contact an admin.");
				} else {
					p.sendMessage(ChatColor.GREEN + "Plot resized successfully!");
				}
				
				//Reset plot selection.
				u.plots.vector.clear();
				//Remove book listener
				u.review.editBook.unregister();
				//Reset inventory slot where feedback book was.
				u.player.getInventory().setItem(4, u.review.previousItem);
				//Send feedback to player and clear review.
				p.sendMessage(ChatColor.GREEN + "Plot " + u.review.plot + " denied and returned to the plot owner.");	
				u.review = null;
				//If another plot is submitted tell the player.
				if (plotData.reviewExists(u)) {
					if (plotData.reviewCount(u) == 1) {
						p.sendMessage(ChatColor.GREEN + "There is 1 plot available for review.");
					} else {
						p.sendMessage(ChatColor.GREEN + "There are " + plotData.reviewCount(u) + " plots available for review.");
					}
				}

				//Teleport back to the build world if in the save world
				if (p.getWorld().equals(Bukkit.getWorld(config.getString("worlds.save")))) {
					Location l = p.getLocation();
					l.setWorld(Bukkit.getWorld(config.getString("worlds.build")));
					p.teleport(l);
				}
		
			} else {
				p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
			}

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Remove Plot")) {

			p.closeInventory();
			PointsData pointsData = Main.getInstance().pointsData;

			//Get the feedback written in the book.
			List<String> book = u.review.bookMeta.getPages();
			int bookID = bookData.newBookID();
			int i = 1;

			//Insert all pages of feedback to the database so they can be retrieved later.
			for(String text: book) {
				if (!(bookData.addPage(bookID, i, text))) {
					p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
					return;
				}
				i++;
			}

			//If the feedback was stored successfully log the plot deny.
			if (denyData.insert(u.review.plot, plotData.getOwner(u.review.plot), u.uuid, bookID, "deleted")) {
				messageData.addMessage(plotData.getOwner(u.review.plot), u.review.plot, "deleted");
				plotData.setStatus(u.review.plot, "deleted");
				plotData.setLastVisit(u.review.plot);
				
				
				List<BlockVector2> corners = WorldGuardFunctions.getPoints(u.review.plot);
				i = 1;
				//Log plot corners to the database
				for (BlockVector2 corner: corners) {
					pointsData.addPoint(u.review.plot, i, corner.getX(), corner.getZ());
					i++;
				}
				
				//Remove the plot contents
				WorldEditor.updateWorld(corners, Bukkit.getWorld(config.getString("worlds.save")), Bukkit.getWorld(config.getString("worlds.build")));
				ClaimFunctions.removeClaim(u.review.plot);
				
				u.review.editBook.unregister();
				u.player.getInventory().setItem(4, u.review.previousItem);
				p.sendMessage(ChatColor.GREEN + "Plot " + u.review.plot + " denied and deleted.");	
				u.review = null;
				if (plotData.reviewExists(u)) {
					if (plotData.reviewCount(u) == 1) {
						p.sendMessage(ChatColor.GREEN + "There is 1 plot available for review.");
					} else {
						p.sendMessage(ChatColor.GREEN + "There are " + plotData.reviewCount(u) + " plots available for review.");
					}
				}

				//Teleport back to the build world if in the save world
				if (p.getWorld().equals(Bukkit.getWorld(config.getString("worlds.save")))) {
					Location l = p.getLocation();
					l.setWorld(Bukkit.getWorld(config.getString("worlds.build")));
					p.teleport(l);
				}
				
			} else {
				p.sendMessage(ChatColor.RED + "An error occured, please notify an admin.");
			}

		}
	}

}
