package me.bteuk.plotsystem.tutorial;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.gui.SwitchServerGUI;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TutorialGui {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Menu";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		if (u.tutorial.tutorial_type == 2 || u.tutorial.tutorial_type == 9) {

			Utils.createItem(inv, Material.LECTERN, 1, 4, ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Step Info",
					Utils.chat("&fSends a video link in chat for this tutorial step."));

		}

		Utils.createItem(inv, Material.GOLDEN_HORSE_ARMOR, 1, 25, ChatColor.AQUA + "" + ChatColor.BOLD + "Redo Previous Step",
				Utils.chat("&fWill take you back to the previous step of this tutorial."));

		Utils.createItem(inv, Material.DIAMOND_HORSE_ARMOR, 1, 24,  ChatColor.AQUA + "" + ChatColor.BOLD + "Previous Tutorial",
				Utils.chat("&fWill take you back to the previous tutorial, this includes lobbies."));

		Utils.createItem(inv, Material.WRITABLE_BOOK, 1, 21,  ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorials",
				Utils.chat("&fTeleport to a tutorial on the server."));

		Utils.createItem(inv, Material.BOOK, 1, 22,  ChatColor.AQUA + "" + ChatColor.BOLD + "Videos",
				Utils.chat("&fWatch a tutorial video to help progress."),
				Utils.chat("&fIncludes tutorial walkthroughs, will be expanded upon in the future."));

		Utils.createItem(inv, Material.ENDER_EYE, 1, 6,  ChatColor.AQUA + "" + ChatColor.BOLD + "Switch Server");

		if (u.previousGui.equals("main")) {
			Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", 
					Utils.chat("&fGo back to the building menu."));	
		}

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Step Info")) {

			TextComponent message = new TextComponent("Click here for a video tutorial!");
			message.setColor(ChatColor.GREEN);

			if (u.tutorial.tutorial_type == 2) {

				if (u.tutorial.tutorial_stage == 1) {
					message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/mMbfLjPSXis"));
					u.player.spigot().sendMessage(message);
				} else if (u.tutorial.tutorial_stage == 2) {
					message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/GMt4-hQWxBI"));
					u.player.spigot().sendMessage(message);
				} else if (u.tutorial.tutorial_stage == 3) {
					message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/CufiTDe8_80"));
					u.player.spigot().sendMessage(message);
				}

			} else if (u.tutorial.tutorial_type == 9) {
				message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/gl6G2fLgouk"));
				u.player.spigot().sendMessage(message);
			} else {
				u.player.sendMessage(ChatColor.RED + "This tutorial stage does not have additional support.");
			}			

		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Switch Server")) {
			p.closeInventory();
			u.previousGui = "tutorial";
			p.openInventory(SwitchServerGUI.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorials")) {
			if (u.tutorial.first_time) {
				u.player.sendMessage(ChatColor.RED + "You must complete the tutorial first!");
			} else {
				p.closeInventory();
				p.openInventory(TutorialSelectionGui.GUI(u));
			}
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Videos")) {
			p.closeInventory();
			p.openInventory(TutorialVideoGui.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			p.openInventory(MainGui.GUI(u));
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Redo Previous Step")) {
			p.closeInventory();

			if (u.tutorial.tutorial_stage == 1) {
				u.player.sendMessage(ChatColor.RED + "This is already the first step of this tutorial.");
			} else if (u.tutorial.tutorial_type == 10) {
				u.player.sendMessage(ChatColor.RED + "You are not in the tutorial.");
			} else {
				u.tutorial.tutorial_stage -= 1;
				u.tutorial.continueTutorial();
			}
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Previous Tutorial")) {
			p.closeInventory();

			if (u.tutorial.tutorial_type == 1) {
				u.player.sendMessage(ChatColor.RED + "This is already the first tutorial.");
			} else if (u.tutorial.tutorial_type == 10) {
				u.player.sendMessage(ChatColor.RED + "You are not in the tutorial.");
			} else {
				if (u.tutorial.tutorial_type == 9) {
					u.tutorial.tutorial_type = 3;
				} else {
					u.tutorial.tutorial_type -= 1;
				}
				u.tutorial.tutorial_stage = 1;
				u.tutorial.continueTutorial();
			}
		}
	}
}
