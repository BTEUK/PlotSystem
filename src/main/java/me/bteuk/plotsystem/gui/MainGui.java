package me.bteuk.plotsystem.gui;

import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.reviewing.FeedbackGui;
import me.bteuk.plotsystem.reviewing.Review;
import me.bteuk.plotsystem.reviewing.ReviewGui;
import me.bteuk.plotsystem.utils.Ranks;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.md_5.bungee.api.ChatColor;

public class MainGui {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Building Menu";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Player p = u.player;
		PlotData plotData = PlotSystem.getInstance().plotData;
		PlayerData playerData = PlotSystem.getInstance().playerData;

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.DIAMOND_PICKAXE, 1, 5, ChatColor.AQUA + "" + ChatColor.BOLD + "Build",
				Utils.chat("&fChoose a location you would like to build."),
				Utils.chat("&fThen you can create a plot and start building."),
				Utils.chat("&fAll ranks can build here."));	

		Utils.createItem(inv, Material.ENDER_EYE, 1, 25, ChatColor.AQUA + "" + ChatColor.BOLD + "Switch Server",
				Utils.chat("&fTeleport to a different server."));	

		Utils.createItem(inv, Material.GRASS_BLOCK, 1, 1, ChatColor.AQUA + "" + ChatColor.BOLD + "Spawn",
				Utils.chat("&fTeleport to spawn."));

		Utils.createItem(inv, Material.LECTERN, 1, 24, ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Menu", 
				Utils.chat("&fPick a tutorial you want to do."),
				Utils.chat("&fYou can leave it at any time."));	

		Utils.createItem(inv, Material.EMERALD, 1, 10, ChatColor.AQUA + "" + ChatColor.BOLD + "Create Plot",
				Utils.chat("&fWill create a plot with the points you have selected."),
				Utils.chat("&fA minimum of 3 points are required for a valid plot."));

		Utils.createItem(inv, Material.BLAZE_ROD, 1, 19, ChatColor.AQUA + "" + ChatColor.BOLD + "Selection Tool", 
				Utils.chat("&fGives you the selection tool."),
				Utils.chat("&fIt is used to create your plot outline."));

		Utils.createItem(inv, Material.CHEST, 1, 22, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Menu", 
				Utils.chat("&fShows all your current active plots."),
				Utils.chat("&fThis is where you can submit, remove"),
				Utils.chat("&fand teleport to your plot."));

		Utils.createPlayerSkull(inv, p, 1, 9, ChatColor.AQUA + "" + ChatColor.BOLD + "Stats", 
				Utils.chat("&fBuilding Points: " + playerData.getPoints(u.uuid)),
				Utils.chat("&fCompleted Plots: " + plotData.completedPlots(u.uuid)));

		Utils.createItem(inv, Material.WHITE_BANNER, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Banner Maker", 
				Utils.chat("&fOpens the Banner Maker!"));

		Utils.createItem(inv, Material.SKELETON_SKULL, 1, 18, ChatColor.AQUA + "" + ChatColor.BOLD + "Head Database", 
				Utils.chat("&fOpens the Head Database!"));

		if (Utils.isPlayerInGroup(u.player, "reviewer") && (u.review != null)) {
			Utils.createItem(inv, Material.ORANGE_STAINED_GLASS_PANE, 1, 23, ChatColor.AQUA + "" + ChatColor.BOLD + "Review Plot", 
					Utils.chat("&fOpens the review menu."),
					Utils.chat("&fAllows you to accept and deny"),
					Utils.chat("&fas well as teleport to the before and after view."));
		}

		else if (Utils.isPlayerInGroup(u.player, "reviewer") && plotData.reviewExists(u)) {
			Utils.createItem(inv, Material.LIME_STAINED_GLASS_PANE, 1, 23, ChatColor.AQUA + "" + ChatColor.BOLD + "New Review", 
					Utils.chat("&fStart reviewing a new plot."),
					Utils.chat("&fWill instantly open the review menu."));
		}

		Utils.createItem(inv, Material.BOOK, 1, 21, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Feedback", 
				Utils.chat("&fOpens the feedback menu for"),
				Utils.chat("&fyour 5 most recently accepted and"),
				Utils.chat("&f5 most recenty denied plots."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;
		PlotData plotData = PlotSystem.getInstance().plotData;


		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Build")) {

			if (u.tutorial.tutorial_type <= 9 && u.tutorial.first_time) {
				u.player.sendMessage(ChatColor.RED + "Please continue the tutorial first!");
				return;
			}

			p.closeInventory();
			p.openInventory(LocationGUI.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Switch Server")) {
			p.closeInventory();
			u.previousGui = "main";
			p.openInventory(SwitchServerGUI.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Spawn")) {
			if (u.tutorial.tutorial_type <= 9 && u.tutorial.first_time) {
				u.player.sendMessage(ChatColor.RED + "Please continue the tutorial first!");
				return;
			}
			p.closeInventory();
			p.teleport(PlotSystem.spawn);
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Menu")) {
			p.closeInventory();
			u.previousGui = "main";
			p.openInventory(TutorialGui.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Selection Tool")) {
			u.plotFunctions.giveSelectionTool();
			p.closeInventory();

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Menu")) {
			//Open the plot gui.
			p.closeInventory();
			if (u.tutorial.tutorial_type <= 9 && u.tutorial.first_time) {
				u.player.sendMessage(ChatColor.RED + "Please continue the tutorial first!");
				return;
			}
			p.openInventory(PlotGui.GUI(u));

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Create Plot")) {

			//If they are in the last stage of the tutorial check if the plot is valid.
			if (u.tutorial.tutorial_type <= 8 && u.tutorial.first_time) {
				u.player.sendMessage(ChatColor.RED + "Please continue the tutorial first!");
				return;
			}
			if (u.tutorial.tutorial_type == 9) {

				if (u.plots.vector.size() < 3) {
					p.sendMessage(Utils.chat("&cYou must select a minimum of 3 points!"));
					return;
				}

				if (u.tutorial.stage9Corners()) {

					Utils.spawnFireWork(u.player);
					u.player.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Complete", "Good luck building!", 10, 100, 50);
					u.tutorial.tutorial_type = 10;
					u.tutorial.tutorial_stage = 0;
					u.tutorial.first_time = false;
					u.tutorial.complete = true;
					Ranks.applicant(u);
					u.player.teleport(PlotSystem.spawn);
					//u.plots = new Plots();
					return;
				} else {
					p.sendMessage(Utils.chat("&cYour selection does not include all of the property, please try again!"));
					return;
				}
			}

			//Check whether the player has a plot.
			if (plotData.hasPlot(p.getUniqueId().toString())) {
				//Count all active plots, if they exceed the limit then end the method.
				if (plotData.activePlotCount(p.getUniqueId().toString()) >= RankValues.plotLimit(p)) {
					p.sendMessage(Utils.chat("&cYou have reached your plot limit, please complete and existing plot to create a new one!"));
					p.closeInventory();
					return;
				}

				//Count total plots, this includes claimed, submitted and under review, if they exceed the limit then end the method.
				if (plotData.totalPlotCount(p.getUniqueId().toString()) >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {
					p.sendMessage(Utils.chat("&cYou have too many plots submitted/claimed, please wait for at least 1 to be reviewed!"));
					p.closeInventory();
					return;
				}
			}

			//Check whether the player has selected the corners correctly.
			//Has selected at least 3 points to create a polygon.
			if (u.plotFunctions.size() < 3) {

				p.sendMessage(Utils.chat("&cYou must select a minimum of 3 points to create a plot!"));
				p.closeInventory();
				return;

			}

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Head Database")) {

			p.closeInventory();
			p.performCommand("headdb");

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Banner Maker")) {

			p.closeInventory();
			p.performCommand("bm");
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Feedback")) {
			//Open the feedback gui			
			p.closeInventory();
			p.openInventory(FeedbackGui.GUI(u));
			return;

		}
	}

}
