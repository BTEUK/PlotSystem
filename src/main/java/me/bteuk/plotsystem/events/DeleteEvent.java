package me.bteuk.plotsystem.events;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.network.commands.Plot;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DeleteEvent {

    public static void event(String uuid, String[] event) {

        //Events for deleting
        if (event[1].equals("plot")) {

            //PlotSQL
            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get location which is the world.
            String location = plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";");

            //Get worlds of plot and save location.
            World copyWorld = Bukkit.getWorld(PlotSystem.getInstance().getConfig().getString("save_world"));
            //Location name is the same as the world name.
            World pasteWorld = Bukkit.getWorld(location);

            if (copyWorld == null || pasteWorld == null) {

                //Send error to console.
                Bukkit.getLogger().severe(Utils.chat("&cPlot delete event failed!"));
                Bukkit.getLogger().severe(Utils.chat("&cEvent details:" + Arrays.toString(event)));
                return;

            }

            int minusXTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
            int minusZTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

            //Get the plot bounds.
            List<BlockVector2> pasteVector = WorldGuardFunctions.getPoints(id, pasteWorld);

            //Create the copyVector by transforming the points in the paste vector with the negative transform.
            //The negative transform is used because the coordinates by default are transformed from the save to the paste world, which in this case it reversed.
            List<BlockVector2> copyVector = new ArrayList<>();
            for (BlockVector2 bv : pasteVector) {
                copyVector.add(BlockVector2.at(bv.getX() + minusXTransform, bv.getZ() + minusZTransform));
            }

            //Revert plot to original state.
            WorldEditor.updateWorld(copyVector, pasteVector, copyWorld, pasteWorld);

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getInstance(), () -> {

                for (User u : PlotSystem.getInstance().getUsers()) {

                    //Update outlines for all users in this world.
                    if (pasteWorld.equals(u.player.getWorld())) {
                        PlotSystem.getInstance().getLogger().info("Updating outlines for " + u.player.getName());
                        PlotSystem.getInstance().claimEnter.updateOutlines(u);
                    }

                }

            }, 20L);

            //Remove all members from the worldguard plot.
            WorldGuardFunctions.clearMembers(id, pasteWorld);

            //Remove all members of plot in database.
            PlotSystem.getInstance().plotSQL.update("DELETE FROM plot_members WHERE id=" + id + ";");

            //Set plot status to unclaimed.
            PlotSystem.getInstance().plotSQL.update("UPDATE plot_data SET status='unclaimed' WHERE id=" + id + ";");

            //Send message to plot owner.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //If the player is on this server send them a message.
            if (p != null) {

                p.sendMessage(Utils.chat("&cPlot &4" + id + " &cdeleted"));

            } else {

                //Add the message to the database so it can be sent wherever they are currently.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&cPlot &4" + id + "&cdeleted');");

            }
        }
    }
}
