package me.bteuk.plotsystem.utils;

import java.util.ArrayList;
import java.util.List;

import me.bteuk.network.utils.Time;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.math.BlockVector2;

import static me.bteuk.plotsystem.PlotSystem.LOGGER;

public class Inactive {

    public static void cancelInactivePlots() {

        //Get config.
        FileConfiguration config = PlotSystem.getInstance().getConfig();

        //Get all plots claimed by inactive players.
        long time = Time.currentTime();
        long timeCap = config.getLong("plot_inactive_cancel") * 24 * 60 * 60 * 1000;
        long timeDif = time - timeCap;

        //Get plot sql.
        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

        //Get inactive plots.
        List<Integer> inactivePlots = plotSQL.getIntList("SELECT id FROM plot_members WHERE is_owner=1 AND last_enter<" + timeDif + ";");

        //If there are no inactive plots, end the method.
        if (inactivePlots == null || inactivePlots.isEmpty()) {
            return;
        }

        //Iterate through all inactive plots and cancel them.
        for (int plot : inactivePlots) {

            //Get plot location.
            String location = plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plot + ";");

            //Check if the plot is on this server and that it is claimed, rather than submitted.
            if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + location +
                    "' AND server='" + PlotSystem.SERVER_NAME + "';") && plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plot + " AND status='claimed';")) {

                //Get worlds of plot and save location.
                String save_world = config.getString("save_world");
                if (save_world == null) {
                    LOGGER.warning("Save World is not defined in config, plot delete event has therefore failed!");
                    continue;
                }

                World copyWorld = Bukkit.getWorld(save_world);
                World pasteWorld = Bukkit.getWorld(location);

                int minusXTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                int minusZTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                //Get the plot bounds.
                List<BlockVector2> pasteVector = WorldGuardFunctions.getPoints(String.valueOf(plot), pasteWorld);

                if (pasteVector == null) {
                    continue;
                }

                //Create the copyVector by transforming the points in the paste vector with the negative transform.
                //The negative transform is used because the coordinates by default are transformed from the save to the paste world, which in this case it reversed.
                List<BlockVector2> copyVector = new ArrayList<>();
                for (BlockVector2 bv : pasteVector) {
                    copyVector.add(BlockVector2.at(bv.getX() + minusXTransform, bv.getZ() + minusZTransform));
                }

                //Revert plot to original state.
                Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getInstance(), () -> {
                    WorldEditor.updateWorld(copyVector, pasteVector, copyWorld, pasteWorld);

                    //Remove all members from the worldguard plot.
                    WorldGuardFunctions.clearMembers(String.valueOf(plot), pasteWorld);

                    //Get the uuid of the plot owner.
                    String uuid = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plot + " AND is_owner=1;");

                    //Remove all members of plot in database.
                    plotSQL.update("DELETE FROM plot_members WHERE id=" + plot + ";");

                    //Set plot status to unclaimed.
                    plotSQL.update("UPDATE plot_data SET status='unclaimed' WHERE id=" + plot + ";");

                    //Add message for the plot owner to the database to notify them that their plot was removed.
                    PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&cPlot &4" + plot + " &c has been removed due to inactivity!');");

                    //Log plot removal to console.
                    LOGGER.info("Plot " + plot + " removed due to inactivity!");

                });
            }
        }
    }

    public static void closeExpiredZones() {

        //Get config.
        FileConfiguration config = PlotSystem.getInstance().getConfig();

        //Get current time, this will be compared with the expiration time.
        long time = Time.currentTime();

        //Get plot sql.
        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

        //Get active zones that have expired.
        List<Integer> expiredZones = plotSQL.getIntList("SELECT id FROM zones WHERE status='open' AND expiration<" + time + ";");

        //If there are no inactive plots, end the method.
        if (expiredZones == null || expiredZones.isEmpty()) {
            return;
        }

        //Iterate through all expired zones, save and close them.
        for (int zone : expiredZones) {

            //Get zone location.
            String location = plotSQL.getString("SELECT location FROM zones WHERE id=" + zone + ";");

            //Check if the zone is on this server.
            if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + location +
                    "' AND server='" + PlotSystem.SERVER_NAME + "';")) {

                //Get worlds of plot and save location.
                String save_world = config.getString("save_world");
                if (save_world == null) {
                    LOGGER.warning("Save World is not defined in config, plot delete event has therefore failed!");
                    continue;
                }

                World copyWorld = Bukkit.getWorld(location);
                World pasteWorld = Bukkit.getWorld(save_world);

                int minusXTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                int minusZTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                //Get the zone bounds.
                List<BlockVector2> copyVector = WorldGuardFunctions.getPoints("z" + zone, pasteWorld);

                if (copyVector == null) {
                    continue;
                }

                //Create the copyVector by transforming the points in the paste vector with the negative transform.
                //The negative transform is used because the coordinates by default are transformed from the save to the paste world, which in this case it reversed.
                List<BlockVector2> pasteVector = new ArrayList<>();
                for (BlockVector2 bv : copyVector) {
                    pasteVector.add(BlockVector2.at(bv.getX() + minusXTransform, bv.getZ() + minusZTransform));
                }

                //Save the zone by copying from the building world to the save world.
                Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getInstance(), () -> {
                    WorldEditor.updateWorld(copyVector, pasteVector, copyWorld, pasteWorld);

                    //Delete the worldguard region.
                    WorldGuardFunctions.delete("z" + zone, copyWorld);

                    //Get the uuid of the zone owner.
                    String uuid = plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + zone + " AND is_owner=1;");

                    //Remove all members of zone in database.
                    plotSQL.update("DELETE FROM zone_members WHERE id=" + zone + ";");

                    //Set the zone status to closed.
                    plotSQL.update("UPDATE zones SET status='closed' WHERE id=" + zone + ";");

                    //Add message for the plot owner to the database to notify them that their zone was closed.
                    PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aZone &3" + zone + " &ahas expired, its content has been saved.');");

                    //Log plot removal to console.
                    LOGGER.info("Zone " + zone + " has expired.");

                });
            }
        }
    }
}
