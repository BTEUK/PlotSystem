package me.bteuk.plotsystem.tutorial;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TutorialVideoGui {
	
	public static Inventory inv;
	public static String inventory_name;
	public static int inv_rows = 3 * 9;

	public static void initialize() {
		inventory_name = ChatColor.AQUA + "" + ChatColor.BOLD + "Videos";

		inv = Bukkit.createInventory(null, inv_rows);

	}

	public static Inventory GUI (User u) {

		Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);
		
		inv.clear();
		
		Utils.createItem(inv, Material.LIME_CONCRETE, 1, 4, ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Step 1, /ll",
				Utils.chat("&fSends a link to a video of the first step of the outlines tutorial."));
		
		Utils.createItem(inv, Material.YELLOW_CONCRETE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Step 2, /tpll",
				Utils.chat("&fSends a link to a video of the second step of the outlines tutorial."));
		
		Utils.createItem(inv, Material.RED_CONCRETE, 1, 22, ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Step 3, //line",
				Utils.chat("&fSends a link to a video of the third step of the outlines tutorial."));
		
		Utils.createItem(inv, Material.LIME_CONCRETE, 1, 6, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Creation",
				Utils.chat("&fSends a link to a video of the plot creation tutorial."));
		
		Utils.createItem(inv, Material.SPRUCE_DOOR, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Return",
				Utils.chat("&fGo back to the tutorial menu."));

		toReturn.setContents(inv.getContents());
		return toReturn;
	}

	public static void clicked(User u, int slot, ItemStack clicked, Inventory inv) {

		Player p = u.player;

		if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Step 1, /ll")) {
			
			TextComponent message = new TextComponent("Click here for the outlines step 1 video tutorial!");
			message.setColor(ChatColor.GREEN);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/mMbfLjPSXis"));
			u.player.spigot().sendMessage(message);
		
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Step 2, /tpll")) {
			
			TextComponent message = new TextComponent("Click here for the outlines step 2 video tutorial!");
			message.setColor(ChatColor.GREEN);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/GMt4-hQWxBI"));
			u.player.spigot().sendMessage(message);
		
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Step 3, //line")) {
			
			TextComponent message = new TextComponent("Click here for the outlines step 3 video tutorial!");
			message.setColor(ChatColor.GREEN);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/CufiTDe8_80"));
			u.player.spigot().sendMessage(message);
		
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Creation")) {
			
			TextComponent message = new TextComponent("Click here for the plot creation video tutorial!");
			message.setColor(ChatColor.GREEN);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtu.be/gl6G2fLgouk"));
			u.player.spigot().sendMessage(message);
			
		} else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Return")) {
			p.closeInventory();
			p.openInventory(TutorialGui.GUI(u));
		}
	}

}
