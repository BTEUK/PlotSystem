package me.bteuk.plotsystem.plots;

import java.util.ArrayList;
import java.util.List;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.PlayerInventory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

/*
 * This class deals with the selection of plots by relevant players.
 * All data about the selection is stored here.
 */
public class PlotFunctions {

    //Stores a reference to the user for simplicity.
    private final User u;

    //This vector of BlockVector2 (2d points) represent the selected points.
    private final List<BlockVector2> vector;

    //The world where the selection is being made.
    private World world;

    //The location where the plot is.
    private String location;

    //Size and difficulty of the plot when creating the plot.
    public int size;
    public int difficulty;

    //Create a new instance of plots.
    public PlotFunctions(User u) {

        this.u = u;
        vector = new ArrayList<>();

    }

    //Clear the selection.
    public void clear() {

        world = null;
        location = null;
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

    public boolean addPoint(Block block) {

        //Add a point to the vector.
        BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());

        //If the distance in a plot exceeds 500 blocks it's too large.
        if (bv2.distance(vector.get(0)) > 500) {

            return false;

        } else {

            vector.add(bv2);
            return true;

        }

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

    //Sets size as the area of the selection
    public void area() {

        //If the vector has less than 3 points you can't get an area.
        if (size() < 3) {
            size = 0;
        }

        int sumX = 0;
        int sumZ = 0;

        for (int i = 0; i < size() - 1; i++) {

            sumX += vector.get(i).getX() * vector.get(i + 1).getZ();
            sumZ += vector.get(i + 1).getX() * vector.get(i).getZ();

        }

        size = Math.abs((sumX - sumZ) / 2);

    }

    //Create a plot with the current selection.
    public String createPlot(PlotSQL plotSQL) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager saveRegions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(plotSQL.getSaveWorld())));
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(world.getName())));

        //Create region
        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion("test", vector, 1, 256);

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = saveRegions.getApplicableRegions(region);
        if (set.size() > 0) {

            return (ChatColor.RED + "This region overlaps with an existing plot, please create a different plot.");

        }

        ArrayList<Location> locations = plotSQL.getLocations(world.getName());

        //Check if this location is a valid area for a plot, store the area name.
        for (Location location : locations) {

            if (location.inLocation(vector.get(0))) {
                this.location = location.getName();
            }

        }

        if (location == null) {
            return (Utils.chat("&cThis selection is not in a valid location."));
        }

        //Create an entry in the database for the plot.
        int plotID = plotSQL.createPlot(size, difficulty, location);

        //Set the region name.
        region = new ProtectedPolygonalRegion(String.valueOf(plotID), vector, 1, 256);

        //Set the region priority to 1
        region.setPriority(1);

        //Add the regions to the worlds
        saveRegions.addRegion(region);
        buildRegions.addRegion(region);

        //Save the new regions
        try {
            saveRegions.save();
            buildRegions.save();
        } catch (
                StorageException e1) {
            e1.printStackTrace();
        }

        clear();

        return (Utils.chat("&aPlot created with ID &3" + plotID +
                " &awith difficulty &3" + difficultyName()) +
                " &aand size &3" + sizeName());

    }

    public PlotDifficulty difficultyName() {

        switch(difficulty) {

            case 1:
                return PlotDifficulty.EASY;
            case 2:
                return PlotDifficulty.NORMAL;
            case 3:
                return PlotDifficulty.HARD;
            default:
                return null;

        }
    }

    public PlotSize sizeName() {

        if (size < 600) {

            return PlotSize.SMALL;

        } else if (size < 1500) {

            return PlotSize.MEDIUM;

        } else {

            return PlotSize.LARGE;

        }
    }
}
