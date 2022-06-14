package me.bteuk.plotsystem.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Utils {

	public static String chat (String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static ItemStack createItem(Material material, int amount, String displayName, String... loreString) {

		ItemStack item;

		List<String> lore = new ArrayList<String>();

		item = new ItemStack(material, amount);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(me.bteuk.network.utils.Utils.chat(displayName));
		for (String s : loreString) {
			lore.add(me.bteuk.network.utils.Utils.chat(s));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;

	}
	
	public static int getHighestYAt(World w, int x, int z) {
		
		for (int i = 255; i >= 0; i--) {
			if (w.getBlockAt(x, i, z).getType() != Material.AIR) {
				return i+1;
			}
		}		
		return 0;			
	}
}