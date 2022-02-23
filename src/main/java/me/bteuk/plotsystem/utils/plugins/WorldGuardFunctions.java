package me.bteuk.plotsystem.utils.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.bteuk.plotsystem.utils.PlotValues;
import me.bteuk.plotsystem.utils.math.Point;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.sql.PlotSQL;

public class WorldGuardFunctions {

    public static Location getCurrentLocation(int plot) {

        //Get instance of plugin and config
        Main instance = Main.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds from config
        World buildWorld = Bukkit.getServer().getWorld(config.getString("worlds.build"));

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(buildWorld));

        //Get the worldguard region and teleport to player to one of the corners.
        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(String.valueOf(plot));

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        Location l = new Location(buildWorld, bv.getX(), Utils.getHighestYAt(buildWorld, bv.getX(), bv.getZ()), bv.getZ());

        return (l);

    }

    public static Location getBeforeLocation(int plot) {

        //Get instance of plugin and config
        Main instance = Main.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds from config
        World saveWorld = Bukkit.getServer().getWorld(config.getString("worlds.save"));

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(saveWorld));

        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(String.valueOf(plot));

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        Location l = new Location(saveWorld, bv.getX(), Utils.getHighestYAt(saveWorld, bv.getX(), bv.getZ()), bv.getZ());

        return (l);

    }

    public static List<BlockVector2> getPoints(int plot) {

        //Get instance of plugin and config
        Main instance = Main.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds from config
        World saveWorld = Bukkit.getServer().getWorld(config.getString("worlds.save"));

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(saveWorld));

        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(String.valueOf(plot));

        return region.getPoints();

    }

    public static boolean inRegion(Block block, PlotSQL plotSQL) {

        //Get the saveWorld from database.
        String worldName = plotSQL.getSaveWorld();

        //If the world is null return true and post error in discord.
        if (worldName == null) {

            //Insert discord warning message in #dev-chat
            return false;

        }

        //Get the saveWorld.
        World saveWorld = Bukkit.getServer().getWorld(plotSQL.getSaveWorld());

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager saveRegions = container.get(BukkitAdapter.adapt(saveWorld));


        //Get the blockvector3 at the block.
        BlockVector3 v = BlockVector3.at(block.getX(), block.getY(), block.getZ());

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = saveRegions.getApplicableRegions(v);

        if (set.size() > 0) {

            return true;

        } else {

            return false;

        }
    }

    public static ApplicableRegionSet getPlots(BlockVector3 min, BlockVector3 max, int radius) {

        //Get plugin instance and config.
        Main instance = Main.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds.
        World saveWorld = Bukkit.getWorld(config.getString("worlds.save"));

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager saveRegions = container.get(BukkitAdapter.adapt(saveWorld));

        //Create region
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("check", BlockVector3.at(min.getX() - radius, 1, min.getZ() - radius), BlockVector3.at(max.getX() + radius, 256, max.getZ() + radius));

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = saveRegions.getApplicableRegions(region);

        return set;
    }

    public static ArrayList<Integer> getNearbyPlots(User u) {

        //Create HashMap
        ArrayList<Integer> list = new ArrayList<Integer>();

        BlockVector3 pos = BlockVector3.at(u.player.getLocation().getX(), u.player.getLocation().getY(), u.player.getLocation().getZ());

        ApplicableRegionSet set = getPlots(pos, pos, 100);

        if (set.size() == 0) {
            return list;
        }

        for (ProtectedRegion entry : set) {
            if (!(entry.getOwners().contains(UUID.fromString(u.uuid)))) {
                list.add(Integer.parseInt(entry.getId()));
            }
        }

        return list;
    }

    public static ArrayList<Integer> getNearbyPlots(ProtectedPolygonalRegion check) {

        //Create HashMap
        ArrayList<Integer> list = new ArrayList<Integer>();

        ApplicableRegionSet set = getPlots(check.getMinimumPoint(), check.getMaximumPoint(), 5);

        if (set.size() == 0) {
            return list;
        }

        for (ProtectedRegion entry : set) {
            list.add(Integer.parseInt(entry.getId()));
        }

        return list;
    }

    public static boolean includesRegion(int plot, ProtectedPolygonalRegion region) {

        //Get the points of the old region.
        List<BlockVector2> points = WorldGuardFunctions.getPoints(plot);

        //If any of the points of the old region are not contained in the new region return false.
        //This would mean the plot doesn't guarantee to include all of their previous plot.
        for (BlockVector2 bv : points) {
            if (!(region.contains(bv))) {
                return false;
            }
        }

        //Get the points of the new region.
        points = region.getPoints();

        //Get instance of plugin and config
        Main instance = Main.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds from config
        World saveWorld = Bukkit.getServer().getWorld(config.getString("worlds.save"));

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(saveWorld));

        //Get the old region.
        ProtectedPolygonalRegion region_old = (ProtectedPolygonalRegion) buildRegions.getRegion(String.valueOf(plot));

        //If any of the points in the new region are contained in the old region return false;
        //This means that the plot does not completely surround the old plot, meaning some of their work could be excluded.
        for (BlockVector2 bv : points) {
            if (region_old.contains(bv)) {
                return false;
            }
        }

        //Return true after all the check have been run.
        return true;

    }

    public static boolean addMember(int plot, String uuid, World world) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        //Add the member to the region.
        buildRegions.getRegion(String.valueOf(plot)).getMembers().addPlayer(UUID.fromString(uuid));

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public static boolean removeMember(int plot, String uuid, World world) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        //Check if the member is in the region.
        if (buildRegions.getRegion(String.valueOf(plot)).getMembers().contains(UUID.fromString(uuid))) {
            //Remove the member to the region.
            buildRegions.getRegion(String.valueOf(plot)).getMembers().removePlayer(UUID.fromString(uuid));
        } else {
            return false;
        }

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    //Get the location of a plot.
    public static Location plotLocation(int plot) {

        //Get the world the plot is in.
        String world = Main.getInstance().plotSQL.getString("SELECT world FROM location_data WHERE location=" + Main.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plot + ";") + ";");

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(world)));

        //Get the worldguard region and teleport to player to one of the corners.
        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) regions.getRegion(String.valueOf(plot));

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        Location l = new Location(Bukkit.getWorld(world), bv.getX(), Utils.getHighestYAt(Bukkit.getWorld(world), bv.getX(), bv.getZ()), bv.getZ());

        return (l);

    }

}
