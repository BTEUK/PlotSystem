package me.bteuk.plotsystem.events;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DeleteEvent {

    public static void event(String uuid, String[] event) {

        //Events for deleting
        if (event[1].equals("plot")) {

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get the plot bounds.
            List<BlockVector2> vector = WorldGuardFunctions.getPoints(id);

            //Get worlds of plot and save location.
            World copyWorld = Bukkit.getWorld(PlotSystem.getInstance().plotSQL.getString("SELECT name FROM world_data WHERE server=" + PlotSystem.SERVER_NAME + " AND type='save';"));
            World pasteWorld = Bukkit.getWorld(PlotSystem.getInstance().plotSQL.getString("SELECT world FROM location_data WHERE name=" +
                    PlotSystem.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")+ ";"));

            if (copyWorld == null || pasteWorld == null) {

                //Send error to console.
                Bukkit.getLogger().severe(Utils.chat("&cPlot delete event failed!"));
                Bukkit.getLogger().severe(Utils.chat("&cEvent details:" + Arrays.toString(event)));
                return;

            }

            //Revert plot to original state.
            WorldEditor.updateWorld(vector, copyWorld, pasteWorld);

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

                p.sendMessage(Utils.chat("&cPlot " + id + " deleted!"));

            } else {

                //Add the message to the database so it can be sent wherever they are currently.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES(" + uuid + ",'&cPlot " + id + "delete!');");

            }
        }
    }
}
