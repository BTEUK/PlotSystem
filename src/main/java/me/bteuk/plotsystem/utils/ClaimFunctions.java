package me.bteuk.plotsystem.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;

public class ClaimFunctions {

	public static String createClaim(User u, List<BlockVector2> vector) {

		//Get plugin instance and config.
		Main instance = Main.getInstance();
		FileConfiguration config = instance.getConfig();

		//Get worlds.
		World saveWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.save")));
		World buildWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.build")));

		//Get instance of WorldGuard.
		WorldGuard wg = WorldGuard.getInstance();

		//Get regions.
		RegionContainer container = wg.getPlatform().getRegionContainer();
		RegionManager saveRegions = container.get(saveWorld);
		RegionManager buildRegions = container.get(buildWorld);

		PlotData plotData = Main.getInstance().plotData;

		//Create new id
		int plotID = plotData.getNewID();

		//Create region
		ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(String.valueOf(plotID), vector, 1, 256);

		//Check whether the region overlaps an existing plot, if true stop the process.
		ApplicableRegionSet set = saveRegions.getApplicableRegions(region);
		if (set.size() > 0) {
			return (ChatColor.RED + "This region overlaps with an existing plot, please create a different plot.");
		}

		//Check if the player is allowed to create a plot in this location.
		//They need to be in a valid area and also have the minimum rank required to create a plot here.
		for (BlockVector2 bv : vector) {

			set = buildRegions.getApplicableRegions(BlockVector3.at(bv.getX(), 64, bv.getZ()));

			if ((!(set.testState(null, Main.CREATE_PLOT_GUEST))) &&
					(!(set.testState(null, Main.CREATE_PLOT_APPRENTICE))) &&
					(!(set.testState(null, Main.CREATE_PLOT_JRBUILDER)))) {
				return (ChatColor.RED + "You can not create a plot here!");
			}

			if (set.testState(null, Main.CREATE_PLOT_GUEST)) {
				continue;

			} else if (set.testState(null, Main.CREATE_PLOT_APPRENTICE) && !(u.player.hasPermission("group.apprentice"))) {
				return (ChatColor.RED + "You must be Apprentice or higher to create a plot here!");

			} else if (set.testState(null, Main.CREATE_PLOT_JRBUILDER) && !(u.player.hasPermission("group.jrbuilder"))) {
				return (ChatColor.RED + "You must be Jr.Builder or higher to create a plot here!");

			} 
		}	

		//Check if any plots are within 2 metre of the plot you're trying to create. 
		Point pt = new Point();
		ArrayList<Integer> nearby = WorldGuardFunctions.getNearbyPlots(region);
		ProtectedRegion rg;
		ArrayList<BlockVector2> pts = new ArrayList<BlockVector2>();
		BlockVector2 pt1;
		BlockVector2 pt2;
		BlockVector2 pt3;
		BlockVector2 pt4;
		int size;
		int size2 = vector.size();
		vector.add(vector.get(0));

		//Iterate through all nearby plots
		for (int i : nearby) {
			rg = saveRegions.getRegion(String.valueOf(i));
			pts.clear();
			pts.addAll(rg.getPoints());
			size = pts.size();
			pts.add(pts.get(0));

			//For each line between 2 points of that plot
			for (int j = 0; j<size; j++) {
				//Get the 2 points
				pt1 = pts.get(j);
				pt2 = pts.get(j+1);

				//Compare to all lines of the plot the player is trying to create
				for (int k = 0; k<size2; k++) {
					//Get the 2 points
					pt3 = vector.get(k);
					pt4 = vector.get(k+1);


					//If the shortest distance between the 2 lines is less than 2 metres then the plot is being
					//created too close to an existing plot and the plot creation process will be aborted.
					if (pt.getShortestDistance(pt1, pt2, pt3, pt4) <= 2) {
						return (ChatColor.RED + "Your plot is too close to an existing plot, please create a plot somewhere else.");
					}
				}
			}
		}

		//Create an entry in the database for the plot.
		if (!(plotData.createPlot(plotID, u.uuid))) {
			return (ChatColor.RED + "An error occured, please try again!");
		}

		//Set owner of the region
		DefaultDomain owners = new DefaultDomain();
		owners.addPlayer(UUID.fromString(u.uuid));
		region.setOwners(owners);

		//Set the region priority to 1
		region.setPriority(1);

		//Add the regions to the worlds
		saveRegions.addRegion(region);
		buildRegions.addRegion(region);

		//Save the new regions
		try {
			saveRegions.save();
			buildRegions.save();
		} catch (StorageException e1) {
			e1.printStackTrace();
		}

		u.plots.vector.clear();;
		return (ChatColor.GREEN + "Plot created with ID " + ChatColor.DARK_AQUA + plotID);

	}

	public static String editClaim(int id, String uuid, List<BlockVector2> vector) {

		PlayerData playerData =  Main.getInstance().playerData;
		//Check whether the player has selected the corners correctly.
		//Has selected at least 3 points to create a polygon.
		if (vector.size() >= 3) {

			//Does not exceed the size.
			if (Plots.largestDistance(vector) > RankValues.maxDis(playerData.getRole(uuid))) {
				return(Utils.chat("&cYour selection is too large!"));
			}

			//Is not too small.
			if (Plots.largestDistance(vector) < 5) {
				return(Utils.chat("&cYour selection is too small!"));
			}

			//Get plugin instance and config.
			Main instance = Main.getInstance();
			FileConfiguration config = instance.getConfig();

			//Get worlds.
			World saveWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.save")));
			World buildWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.build")));

			//Get instance of WorldGuard.
			WorldGuard wg = WorldGuard.getInstance();

			//Get regions.
			RegionContainer container = wg.getPlatform().getRegionContainer();
			RegionManager saveRegions = container.get(saveWorld);
			RegionManager buildRegions = container.get(buildWorld);

			//Create region
			ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(String.valueOf(id + "-test"), vector, 1, 256);

			//Check if the plot includes all of the existing area
			if (!(WorldGuardFunctions.includesRegion(id, region))) {
				return(ChatColor.RED + "Your selection does not include all of the existing plot.");
			}

			//Check whether the region overlaps an existing plot, if true stop the process.
			ApplicableRegionSet set = saveRegions.getApplicableRegions(region);
			if (set.size() > 1) {
				return(ChatColor.RED + "Your selection overlaps with a different plot.");
			}

			//Remove existing region
			saveRegions.removeRegion(String.valueOf(id));
			buildRegions.removeRegion(String.valueOf(id));

			//Save the removed regions
			try {
				saveRegions.save();
				buildRegions.save();
			} catch (StorageException e1) {
				e1.printStackTrace();
			}

			//Create region
			region = new ProtectedPolygonalRegion(String.valueOf(id), vector, 1, 256);

			//Set owner of the region
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(UUID.fromString(uuid));
			region.setOwners(owners);

			//Set the region priority to 1
			region.setPriority(1);

			//Add the regions to the worlds
			saveRegions.addRegion(region);
			buildRegions.addRegion(region);

			//Save the new regions
			try {
				saveRegions.save();
				buildRegions.save();
			} catch (StorageException e1) {
				e1.printStackTrace();
			}

		} else {
			return(Utils.chat("&cYou must select a minimum of 3 points to create a plot!"));
		}

		return (ChatColor.GREEN + "Plot " + ChatColor.DARK_AQUA + id + ChatColor.GREEN + " updated with new area!");
	}

	public static boolean checkEdit(Player p, int id, String uuid, List<BlockVector2> vector) {

		PlayerData playerData =  Main.getInstance().playerData;
		//Check whether the player has selected the corners correctly.
		//Has selected at least 3 points to create a polygon.
		if (vector.size() >= 3) {

			//Does not exceed the size.
			if (Plots.largestDistance(vector) > RankValues.maxDis(playerData.getRole(uuid))) {
				p.sendMessage(Utils.chat("&cYour selection is too large!"));
				return false;
			}

			//Is not too small.
			if (Plots.largestDistance(vector) < 5) {
				p.sendMessage(Utils.chat("&cYour selection is too small!"));
				return false;
			}

			//Get plugin instance and config.
			Main instance = Main.getInstance();
			FileConfiguration config = instance.getConfig();

			//Get worlds.
			World saveWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.save")));

			//Get instance of WorldGuard.
			WorldGuard wg = WorldGuard.getInstance();

			//Get regions.
			RegionContainer container = wg.getPlatform().getRegionContainer();
			RegionManager saveRegions = container.get(saveWorld);

			//Create region
			ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(String.valueOf(id + "-test"), vector, 1, 256);

			//Check if the plot includes all of the existing area
			if (!(WorldGuardFunctions.includesRegion(id, region))) {
				p.sendMessage(ChatColor.RED + "Your selection does not include all of the existing plot.");
				return false;
			}

			//Check whether the region overlaps an existing plot, if true stop the process.
			ApplicableRegionSet set = saveRegions.getApplicableRegions(region);
			if (set.size() > 1) {
				p.sendMessage(ChatColor.RED + "Your selection overlaps with a different plot.");
				return false;
			}

			return true;

		} else {
			p.sendMessage(Utils.chat("&cYou must select a minimum of 3 points to create a plot!"));
			return false;
		}
	}

	public static boolean resizePlot(int id, String uuid, List<BlockVector2> vector) {

		PlayerData playerData =  Main.getInstance().playerData;
		//Check whether the player has selected the corners correctly.
		//Has selected at least 3 points to create a polygon.
		if (vector.size() >= 3) {

			//Does not exceed the size.
			if (Plots.largestDistance(vector) > RankValues.maxDis(playerData.getRole(uuid))) {
				return false;
			}

			//Is not too small.
			if (Plots.largestDistance(vector) < 5) {
				return false;
			}

			//Get plugin instance and config.
			Main instance = Main.getInstance();
			FileConfiguration config = instance.getConfig();

			//Get worlds.
			World saveWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.save")));
			World buildWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.build")));

			//Get instance of WorldGuard.
			WorldGuard wg = WorldGuard.getInstance();

			//Get regions.
			RegionContainer container = wg.getPlatform().getRegionContainer();
			RegionManager saveRegions = container.get(saveWorld);
			RegionManager buildRegions = container.get(buildWorld);

			//Create region
			ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(String.valueOf(id + "-test"), vector, 1, 256);

			//Check if the plot includes all of the existing area
			if (!(WorldGuardFunctions.includesRegion(id, region))) {
				return false;
			}

			//Check whether the region overlaps an existing plot, if true stop the process.
			ApplicableRegionSet set = saveRegions.getApplicableRegions(region);
			if (set.size() > 1) {
				return false;
			}

			//Remove existing region
			saveRegions.removeRegion(String.valueOf(id));
			buildRegions.removeRegion(String.valueOf(id));

			//Save the removed regions
			try {
				saveRegions.save();
				buildRegions.save();
			} catch (StorageException e1) {
				e1.printStackTrace();
			}

			//Create region
			region = new ProtectedPolygonalRegion(String.valueOf(id), vector, 1, 256);

			//Set owner of the region
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(UUID.fromString(uuid));
			region.setOwners(owners);

			//Set the region priority to 1
			region.setPriority(1);

			//Add the regions to the worlds
			saveRegions.addRegion(region);
			buildRegions.addRegion(region);

			//Save the new regions
			try {
				saveRegions.save();
				buildRegions.save();
			} catch (StorageException e1) {
				e1.printStackTrace();
			}

		} else {
			return false;
		}

		return true;
	}

	public static String removeClaim(int id) {

		//Get instance of plugin and config
		Main instance = Main.getInstance();
		FileConfiguration config = instance.getConfig();

		//Get worlds.
		World saveWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.save")));
		World buildWorld = BukkitAdapter.adapt(Bukkit.getWorld(config.getString("worlds.build")));

		//Get instance of WorldGuard.
		WorldGuard wg = WorldGuard.getInstance();

		//Get regions.
		RegionContainer container = wg.getPlatform().getRegionContainer();
		RegionManager saveRegions = container.get(saveWorld);
		RegionManager buildRegions = container.get(buildWorld);

		//If the regions exist continue
		if (!(saveRegions.hasRegion(String.valueOf(id)))) {
			return (ChatColor.RED + "This region does not exist!");
		}

		//Remove the regions from the worlds
		saveRegions.removeRegion(String.valueOf(id));
		buildRegions.removeRegion(String.valueOf(id));

		//Save the removed regions
		try {
			saveRegions.save();
			buildRegions.save();
		} catch (StorageException e1) {
			e1.printStackTrace();
		}

		return (ChatColor.GREEN + "Plot " + ChatColor.DARK_AQUA + id + ChatColor.GREEN + " removed!");
	}
	
	

}
