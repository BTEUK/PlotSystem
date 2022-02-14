package me.bteuk.plotsystem.utils.plugins;

import java.util.List;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;

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
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager saveRegions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(plotSQL.getSaveWorld())));
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(world.getName())));

        //Create region
        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion("test", vector, 1, 256);

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = saveRegions.getApplicableRegions(region);
        if (set.size() > 0) {

            p.sendMessage(Utils.chat("&cYour selection overlaps with an existing plot."));
            return false;

        }

        //Create an entry in the database for the plot.
        plotID = plotSQL.createPlot(size, difficulty, location);

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

        return true;
    }
}
