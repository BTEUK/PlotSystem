package me.bteuk.plotsystem.reviewing;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.mysql.AcceptData;
import me.bteuk.plotsystem.mysql.DenyData;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class FeedbackGui {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 5 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Feedback Menu";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		AcceptData acceptData = Main.getInstance().acceptData;
		DenyData denyData = Main.getInstance().denyData;
		PlayerData playerData = Main.getInstance().playerData;

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 45, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", Utils.chat("&fGo back to the building menu."));
		int i;
		Material mat = Material.RED_CONCRETE;

		if (acceptData.hasEntry(u.uuid)) {

			ArrayList<Integer> accept = acceptData.getLatest(new ArrayList<Integer>(), u.uuid);
			i = 12;

			for (int plot: accept) {

				if (acceptData.isBuilding(plot) && acceptData.hasFeedback(plot)) {
					Utils.createItem(inv, Material.LIME_CONCRETE, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + String.valueOf(plot),
							Utils.chat("&fAccepted at: " + Time.getDate(acceptData.getTime(plot))),
							Utils.chat("&fAccepted by: " + playerData.getName(acceptData.getReviewer(plot))),
							Utils.chat("&fBuilding points received: " + acceptData.getPoints(plot)),
							"",
							Utils.chat("&fSize: &a" + acceptData.getSize(plot) + " &f/&a5"),
							Utils.chat("&fAccuracy: &a" + acceptData.getAccuracy(plot) + " &f/&a5"),
							Utils.chat("&fQuality: &a" + acceptData.getQuality(plot) + " &f/&a5"),
							"",
							ChatColor.WHITE + "" + ChatColor.BOLD + "Click to view written feedback!");
				} else if (acceptData.isBuilding(plot)) {
					Utils.createItem(inv, Material.LIME_CONCRETE, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + String.valueOf(plot),
							Utils.chat("&fAccepted at: " + Time.getDate(acceptData.getTime(plot))),
							Utils.chat("&fAccepted by: " + playerData.getName(acceptData.getReviewer(plot))),
							Utils.chat("&fBuilding points received: " + acceptData.getPoints(plot)),
							"",
							Utils.chat("&fSize: &a" + acceptData.getSize(plot) + " &f/&a5"),
							Utils.chat("&fAccuracy: &a" + acceptData.getAccuracy(plot) + " &f/&a5"),
							Utils.chat("&fQuality: &a" + acceptData.getQuality(plot) + " &f/&a5"));
				} else if (acceptData.hasFeedback(plot)) {
					Utils.createItem(inv, Material.LIME_CONCRETE, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + String.valueOf(plot),
							Utils.chat("&fAccepted at: " + Time.getDate(acceptData.getTime(plot))),
							Utils.chat("&fAccepted by: " + playerData.getName(acceptData.getReviewer(plot))),
							Utils.chat("&fBuilding points received: " + acceptData.getPoints(plot)),
							"",
							ChatColor.WHITE + "" + ChatColor.BOLD + "Click to view written feedback!");
				} else {
					Utils.createItem(inv, Material.LIME_CONCRETE, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + String.valueOf(plot),
							Utils.chat("&fAccepted at: " + Time.getDate(acceptData.getTime(plot))),
							Utils.chat("&fAccepted by: " + playerData.getName(acceptData.getReviewer(plot))),
							Utils.chat("&fBuilding points received: " + acceptData.getPoints(plot)));
				}
				i++;
			}
		}

		if (denyData.hasEntry(u.uuid)) {

			ArrayList<Integer> deny = denyData.getLatest(new ArrayList<Integer>(), u.uuid);
			i = 30;

			for (int id: deny) {

				switch (denyData.getType(id)) {
				case "returned":
					mat = Material.ORANGE_CONCRETE;
					Utils.createItem(inv, mat, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + denyData.getPlot(id) + " attempt " + denyData.getAttempt(id),
							Utils.chat("&fThe plot was denied and returned to you."),
							Utils.chat("&fDenied at: " + Time.getDate(denyData.getTime(id))),
							Utils.chat("&fDenied by: " + playerData.getName(denyData.getReviewer(id))),
							"",
							ChatColor.WHITE + "" + ChatColor.BOLD + "Click to view feedback!");
					break;
				case "resized":
					mat = Material.YELLOW_CONCRETE;
					Utils.createItem(inv, mat, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + denyData.getPlot(id) + " attempt " + denyData.getAttempt(id),
							Utils.chat("&fThe plot was denied, resized and returned to you."),
							Utils.chat("&fDenied at: " + Time.getDate(denyData.getTime(id))),
							Utils.chat("&fDenied by: " + playerData.getName(denyData.getReviewer(id))),
							"",
							ChatColor.WHITE + "" + ChatColor.BOLD + "Click to view feedback!");
					break;
				case "deleted":
					mat = Material.RED_CONCRETE;
					Utils.createItem(inv, mat, 1, i, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot: " + denyData.getPlot(id) + " attempt " + denyData.getAttempt(id),
							Utils.chat("&fThe plot was denied and removed."),
							Utils.chat("&fDenied at: " + Time.getDate(denyData.getTime(id))),
							Utils.chat("&fDenied by: " + playerData.getName(denyData.getReviewer(id))),
							"",
							ChatColor.WHITE + "" + ChatColor.BOLD + "Click to view feedback!");
					break;				

				}
		
				i++;

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
		
		AcceptData acceptData = Main.getInstance().acceptData;
		DenyData denyData = Main.getInstance().denyData;
		
		int plot;
		int attempt;
		String[] split;
		String title = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
		
		if (clicked.getType().equals(Material.LIME_CONCRETE)) {
			
			split = title.split(" ");
			plot = Integer.parseInt(split[1]);
			
			if (acceptData.hasFeedback(plot)) {
				p.closeInventory();
				p.openBook(acceptData.getBook(plot));
			}
			
		} else {
			
			split = title.split(" ");
			plot = Integer.parseInt(split[1]);
			attempt = Integer.parseInt(split[3]);
			
			p.closeInventory();
			p.openBook(denyData.getBook(plot, attempt));
			
		}
	}		
}
