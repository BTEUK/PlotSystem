package me.bteuk.plotsystem.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.Main;

public class RankValues {

	public static int plotLimit(Player p) {

		Main instance = Main.getInstance();
		FileConfiguration config = instance.getConfig();
		
		if (p.hasPermission("group.builder")) {
			return config.getInt("plot_limit.builder");
		} else if (p.hasPermission("group.jrbuilder")) {
			return config.getInt("plot_limit.jrbuilder");
		} else if (p.hasPermission("group.apprentice")) {
			return config.getInt("plot_limit.apprentice");
		} else {
			return config.getInt("plot_limit.guest");
		}
	}

	public static int maxDis(Player p) {

		Main instance = Main.getInstance();
		FileConfiguration config = instance.getConfig();
		
		if (p.hasPermission("group.builder")) {
			return config.getInt("plot_distance.builder");
		} else if (p.hasPermission("group.jrbuilder")) {
			return config.getInt("plot_distance.jrbuilder");
		} else if (p.hasPermission("group.apprentice")) {
			return config.getInt("plot_distance.apprentice");
		} else {
			return config.getInt("plot_distance.guest");
		}
	}
	
	public static int maxDis(String role) {

		Main instance = Main.getInstance();
		FileConfiguration config = instance.getConfig();
		
		if (role.equals("builder")) {
			return config.getInt("plot_distance.builder");
		} else if (role.equals("jrbuilder")) {
			return config.getInt("plot_distance.jrbuilder");
		} else if (role.equals("apprentice")) {
			return config.getInt("plot_distance.apprentice");
		} else {
			return config.getInt("plot_distance.guest");
		}
	}

}
