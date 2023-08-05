package me.bteuk.plotsystem.events;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

import static me.bteuk.plotsystem.PlotSystem.LOGGER;

public class CloseEvent {

    public static void event(String uuid, String[] event) {

        //Event for save and closing.
        if (event[1].equals("zone")) {

            //PlotSQL
            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;
            FileConfiguration config = PlotSystem.getInstance().getConfig();

            //Convert the string id to int id.
            int zone = Integer.parseInt(event[2]);

            //Get zone location.
            String location = plotSQL.getString("SELECT location FROM zones WHERE id=" + zone + ";");

            //Check if the zone is on this server.
            if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + location +
                    "' AND server='" + PlotSystem.SERVER_NAME + "';")) {

                //Get worlds of plot and save location.
                String save_world = config.getString("save_world");
                if (save_world == null) {
                    LOGGER.warning("Save World is not defined in config, plot delete event has therefore failed!");
                    return;
                }

                World copyWorld = Bukkit.getWorld(location);
                World pasteWorld = Bukkit.getWorld(save_world);

                assert(copyWorld != null);

                int minusXTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                int minusZTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                //Get the zone bounds.
                List<BlockVector2> copyVector;
                try {
                    copyVector = WorldGuardFunctions.getPoints("z" + zone, copyWorld);
                } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                    PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('&cAn error occurred while closing the zone, please contact an admin.');");
                    e.printStackTrace();
                    return;
                }

                //Create the copyVector by transforming the points in the paste vector with the negative transform.
                //The negative transform is used because the coordinates by default are transformed from the save to the paste world, which in this case it reversed.
                List<BlockVector2> pasteVector = new ArrayList<>();
                for (BlockVector2 bv : copyVector) {
                    pasteVector.add(BlockVector2.at(bv.getX() + minusXTransform, bv.getZ() + minusZTransform));
                }

                //Remove entities in de existing region, except players.
                WorldEditor.deleteEntities(pasteVector, pasteWorld);

                Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getInstance(), () -> {
                    //Save the zone by copying from the building world to the save world.
                    WorldEditor.updateWorld(copyVector, pasteVector, copyWorld, pasteWorld);

                    //Delete the worldguard region.
                    try {
                        WorldGuardFunctions.delete("z" + zone, copyWorld);
                    } catch (RegionManagerNotFoundException e) {
                        PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('&cAn error occurred while closing the zone, please contact an admin.');");
                        e.printStackTrace();
                        return;
                    }

                    //Remove all members of zone in database.
                    plotSQL.update("DELETE FROM zone_members WHERE id=" + zone + ";");

                    //Set the zone status to closed.
                    plotSQL.update("UPDATE zones SET status='closed' WHERE id=" + zone + ";");

                    //Add message for the plot owner to the database to notify them that their zone was closed.
                    PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aClosed Zone &3" + zone + "&a, its content has been saved.');");

                    //Log plot removal to console.
                    LOGGER.info("Zone " + zone + " has been closed.");
                });
            }
        }
    }
}
