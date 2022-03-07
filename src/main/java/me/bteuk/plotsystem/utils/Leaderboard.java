package me.bteuk.plotsystem.utils;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.mysql.PlayerData;	

public class Leaderboard {

	public String[] uuids = new String[9];
	public int[] points = new int[9];
	public int[] position = new int[9];
	
	public Leaderboard() {
		
	}
	
	public static void printLeaderboard(User u) {

		PlayerData playerData = PlotSystem.getInstance().playerData;
		
		Leaderboard lead = playerData.pointsAboveBelow(u.uuid, u.name);

		if (lead == null) {
			u.player.sendMessage(ChatColor.RED + "Not enough entries to create a leaderboard!");
			return;

		}

		if (lead.points[0] == 0) {
			u.player.sendMessage(ChatColor.RED + "Nobody has points!");
			return;
		}

		u.player.sendMessage(String.format("%-6s%-8s%-16s", "#", "Points" , "Username"));
		u.player.sendMessage("------------------------");

		for (int i = 0; i < lead.points.length; i++) {

			if (lead.uuids[i] == null) {
				break;
			}
			u.player.sendMessage(String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , lead.uuids[i]));			
		}
	}
	
	public static void printLeaderboardElse(User u, String uuid, String name) {

		PlayerData playerData = PlotSystem.getInstance().playerData;
		Leaderboard lead = playerData.pointsAboveBelow(uuid, name);

		if (lead == null) {
			u.player.sendMessage(ChatColor.RED + "Not enough entries to create a leaderboard!");
			return;

		}

		if (lead.points[0] == 0) {
			u.player.sendMessage(ChatColor.RED + "Nobody has points!");
			return;
		}

		u.player.sendMessage(String.format("%-6s%-8s%-16s", "#", "Points" , "Username"));
		u.player.sendMessage("------------------------");

		for (int i = 0; i < lead.points.length; i++) {

			if (lead.uuids[i] == null) {
				break;
			}
			u.player.sendMessage(String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , lead.uuids[i]));			
		}
	}
	
	public static void printLeaderboardTop(User u) {

		PlayerData playerData = PlotSystem.getInstance().playerData;
		LinkedHashMap<String, Integer> lead = playerData.pointsTop();

		if (lead == null) {
			u.player.sendMessage(ChatColor.RED + "Not enough entries to create a leaderboard!");
			return;

		}

		if (lead.size() == 0) {
			u.player.sendMessage(ChatColor.RED + "Nobody has points!");
			return;
		}

		u.player.sendMessage(String.format("%-6s%-8s%-16s", "#", "Points" , "Username"));
		u.player.sendMessage("------------------------");

		int i = 1;
		
		for (Entry<String, Integer> e : lead.entrySet()) {

			u.player.sendMessage(String.format("%-6s%-8s%-16s", i, e.getValue() , e.getKey()));
			i += 1;
			
		}		
		
	}
}
