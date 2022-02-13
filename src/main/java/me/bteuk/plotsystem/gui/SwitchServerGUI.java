package me.bteuk.plotsystem.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;

public class SwitchServerGUI {

	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Servers";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

		inv.clear();

		Utils.createItem(inv, Material.BEACON, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Lobby Server",
				Utils.chat("&fTeleports you back to the lobby server."));

		Utils.createItem(inv, Material.DIAMOND_BLOCK, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Earth Server",
				ChatColor.WHITE + "Teleport to the earth server.",
				ChatColor.WHITE + "If you wish to build you must be Jr.Builder+",
				ChatColor.WHITE + "It is recommended to use the modpack for building.",
				ChatColor.WHITE + "Supports 1.12.2 - 1.18.1 without the modpack.");

		/*Utils.createItem(inv, Material.CARVED_PUMPKIN, 1, 16, ChatColor.AQUA + "" + ChatColor.BOLD + "Minigames Server",
				ChatColor.WHITE + "Teleport to the minigames server.",
				ChatColor.WHITE + "Current minigames: Hide'n'Seek.",
				ChatColor.WHITE + "Supports 1.12.2 - 1.16.5.");	
				*/

		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Return", 
				Utils.chat("&fGo back to the building menu."));	

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Lobby Server")) {

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF("lobby");

			p.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			if (u.previousGui.equals("main")) {
				p.openInventory(MainGui.GUI(u));
			} else if (u.previousGui.equals("tutorial")) {
				p.openInventory(TutorialGui.GUI(u));
			} else {
				p.openInventory(MainGui.GUI(u));
			}
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Earth Server")) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF("earth");

			p.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Minigames Server")) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF("minigames");

			p.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
		}
	}

}
