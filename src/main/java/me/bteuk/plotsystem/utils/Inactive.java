package me.bteuk.plotsystem.utils;

import java.util.List;

import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.mysql.PointsData;

public class Inactive {

	public static void cancelInactivePlots() {

		//Get config.
		FileConfiguration config = Main.getInstance().getConfig();	
		
		PointsData pointsData = Main.getInstance().pointsData;

		//Get all plots claimed by inactive players.

		long time = Time.currentTime();
		long timeCap = config.getLong("plot_inactive_cancel")*24*60*60*1000;
		long timeDif = time - timeCap; 
		
		PlotData plotData = Main.getInstance().plotData;
		
		List<Integer> inactivePlots = plotData.getInactivePlots(timeDif);

		//If there are no inactive plots, end the method.
		if (inactivePlots == null || inactivePlots.isEmpty()) {
			return;
		}
		
		int i;

		//Iterate through all inactive plots and cancel them.
		for (int plot : inactivePlots) {

			List<BlockVector2> corners = WorldGuardFunctions.getPoints(plot);
			i = 1;
			//Log plot corners to the database
			for (BlockVector2 corner: corners) {
				pointsData.addPoint(plot, i, corner.getX(), corner.getZ());
				i++;
			}
			
			WorldEditor.updateWorld(corners, Bukkit.getWorld(config.getString("worlds.save")), Bukkit.getWorld(config.getString("worlds.build")));
			ClaimFunctions.removeClaim(plot);
			plotData.setStatus(plot, "cancelled");
			Bukkit.broadcastMessage(ChatColor.RED + "Plot " + plot + " has been removed due to inactivity!");
		}


	}

}
