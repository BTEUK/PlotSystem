package me.bteuk.plotsystem.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlayerData;
import net.md_5.bungee.api.ChatColor;

public class Ranks {

	/*
	public static void checkRankup(User u) {

		PlayerData playerData = Main.getInstance().playerData;

		int points = playerData.getPoints(u.uuid);
		FileConfiguration config = Main.getInstance().getConfig();

		if (points >= config.getInt("rankup.builder") && u.role.equals("jrbuilder")) {
			u.role = "builder";
		} else if (points >= config.getInt("rankup.jrbuilder") && u.role.equals("apprentice")) {
			u.role = "jrbuilder";
		} else if (points >= config.getInt("rankup.apprentice") && u.role.equals("applicant")) {
			u.role = "apprentice";
		} else {
			return;
		}

		//Promote the player
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

		String command = "lp user " + u.name + " promote builder";
		Bukkit.dispatchCommand(console, command);
		Bukkit.broadcastMessage(ChatColor.GREEN + u.name + " has been promoted to " + u.role);
		playerData.updateRole(u.uuid, u.role);
		checkRankup(u);
	}

	public static void applicant(User u) {

		PlayerData playerData = Main.getInstance().playerData;
		
		if (u.role.equals("guest")) {
			u.role = "applicant";
		} else {
			return;
		}

		//Promote the player
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

		String command = "lp user " + u.name + " promote builder";
		Bukkit.dispatchCommand(console, command);
		
		//Create a new announcement using the utils plugin
		//If the utils plugins is disabled in config then it'll default to console.
		if (Main.getInstance().getConfig().getBoolean("UKUtils")) {
			//Announcements.newAnnouncement(u.name + " has been promoted to " + u.role, "GREEN");
		} else {
			Bukkit.broadcastMessage(ChatColor.GREEN + u.name + " has been promoted to " + u.role);
		}
		
		playerData.updateRole(u.uuid, u.role);
		checkRankup(u);

	}
	 */
}
