package me.bteuk.plotsystem.plots;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.bteuk.plotsystem.plots.Plots;
import me.bteuk.plotsystem.utils.Point;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.PlayerInventory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlotData;

/*
 * This class deals with the selection of plots by relevant players.
 * All data about the selection is stored here.
 */
public class PlotFunctions {
	
	//Stores a reference to the user for simplicity.
	private User u;

	//This vector of BlockVector2 (2d points) represent the selected points.
	private List<BlockVector2> vector;
	
	//The world where the selection is being made.
	private World world;
	
	//Create a new instance of plots.
	public PlotFunctions(User u) {
		
		this.u = u;
		vector = new ArrayList<BlockVector2>();
		
	}
	
	//Clear the selection.
	public void clear() {
		
		vector.clear();
		
	}

	public void startSelection(Block block) {

		//Since this is the start of a selection make sure the vector is empty.
		clear();
		
		//Set the world.
		world = block.getWorld();
		
		//Get the x,z of the block clicked and store it in the vector.
		BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());
		vector.add(bv2);

	}
	
	public World world() {
		
		return world;
		
	}

	public void addPoint(Block block) {

		//Add a point to the vector.
		BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());
		vector.add(bv2);

	}

	public void giveSelectionTool() {

		//Get the player inventory and check whether they already have the selection tool.
		PlayerInventory i = u.player.getInventory();

		//Check if the player already has the selection tool in their inventory.
		if (i.contains(Plots.selectionTool)) {
			
			//Get the selection tool from their inventory and swap it with the item in their hand.			
			i.setItem(i.first(Plots.selectionTool), i.getItemInMainHand());
			i.setItemInMainHand(Plots.selectionTool);
		
		} else {
			
			//If they don't have the selection tool already set it in their main hand.
			i.setItemInMainHand(Plots.selectionTool);
			
		}
	}
	
	//Return number of elements in vector.
	public int size() {
		
		return vector.size();
		
	}
	
	//Create a plot with the current selection.
	public String createPlot() {

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
}
