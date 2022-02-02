package me.bteuk.plotsystem.tutorial;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class TutorialSelectionGui {
	
	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorials";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);
		
		inv.clear();
		
		Utils.createItem(inv, Material.WOODEN_AXE, 1, 13,  ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Tutorial",
				Utils.chat("&fTeleport to the outlines tutorial."));
		
		Utils.createItem(inv, Material.BLAZE_ROD, 1, 15,  ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Creation Tutorial",
				Utils.chat("&fTeleport to the plot creation tutorial."));
		
		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27,  ChatColor.AQUA + "" + ChatColor.BOLD + "Return",
				Utils.chat("&fGo back to the tutorial menu."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Tutorial")) {
			p.closeInventory();
			u.tutorial.complete = false;
			u.tutorial.tutorial_type = 2;
			u.tutorial.tutorial_stage = 1;
			u.tutorial.continueTutorial();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Creation Tutorial")) {
			p.closeInventory();
			u.tutorial.complete = false;
			u.tutorial.tutorial_type = 9;
			u.tutorial.tutorial_stage = 1;
			u.tutorial.continueTutorial();
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			p.openInventory(TutorialGui.GUI(u));
		}
	}

}
