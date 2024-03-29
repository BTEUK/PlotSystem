package me.bteuk.plotsystem.utils.plugins;

import java.util.List;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.sql.PlotSQL;
import org.bukkit.World;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import org.bukkit.entity.Player;

import static me.bteuk.network.utils.Constants.MAX_Y;
import static me.bteuk.network.utils.Constants.MIN_Y;

/*
This class adds the implementation of plot creation using worldguard.
 */
public class WGCreatePlot {

    public int plotID;

    //Create a new instance of plots.
    public WGCreatePlot() {
    }

    //Create a plot with the current selection.
    public boolean createPlot(Player p, World world, String location, List<BlockVector2> vector, PlotSQL plotSQL, int size, int difficulty) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionManager regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

        //Checking if regions isn't null, would indicate that the world doesn't exist.
        if (regions == null) {
            return false;
        }

        //Create region to test.
        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion("test", vector, MIN_Y, (MAX_Y-1));

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = regions.getApplicableRegions(region);
        if (set.size() > 0) {

            p.sendMessage(Utils.error("Your selection overlaps with an existing plot or zone."));
            return false;

        }

        //Create an entry in the database for the plot.
        plotID = plotSQL.createPlot(size, difficulty, location);

        //Create the region with valid name.
        region = new ProtectedPolygonalRegion(String.valueOf(plotID), vector, MIN_Y, (MAX_Y-1));

        //Add the regions to the world
        regions.addRegion(region);

        //Save the new region
        try {
            regions.save();
        } catch (
                StorageException e1) {
            e1.printStackTrace();
        }

        return true;
    }

    //Create a zone with the current selection.
    public boolean createZone(Player p, World world, String location, List<BlockVector2> vector, PlotSQL plotSQL, long expiration, boolean is_public) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionManager regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

        //Checking if regions isn't null, would indicate that the world doesn't exist.
        if (regions == null) {
            return false;
        }

        //Create region to test.
        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion("test", vector, MIN_Y, (MAX_Y-1));

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = regions.getApplicableRegions(region);
        if (set.size() > 0) {

            p.sendMessage(Utils.error("Your selection overlaps with an existing plot or zone."));
            return false;

        }

        //Create an entry in the database for the plot.
        plotID = plotSQL.createZone(location, expiration, is_public);

        //Create the region with valid name.
        region = new ProtectedPolygonalRegion("z" + plotID, vector, MIN_Y, (MAX_Y-1));

        //Add the owner to the region.
        region.getMembers().addPlayer(p.getUniqueId());

        //Add the regions to the world
        regions.addRegion(region);

        //Save the new region
        try {
            regions.save();
        } catch (
                StorageException e1) {
            e1.printStackTrace();
        }

        return true;
    }
}
