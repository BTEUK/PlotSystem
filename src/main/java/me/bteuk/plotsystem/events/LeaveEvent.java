package me.bteuk.plotsystem.events;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class LeaveEvent {

    public static void event(String uuid, String[] event) {

        //Events for leaving
        if (event[1].equals("plot")) {

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get worlds of plot.
            World world = Bukkit.getWorld(PlotSystem.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";"));

            if (world == null) {

                //Send error to console.
                Bukkit.getLogger().severe(Utils.chat("&cPlot leave event failed!"));
                Bukkit.getLogger().severe(Utils.chat("&cEvent details:" + Arrays.toString(event)));
                return;

            }

            //Remove member from plot.
            WorldGuardFunctions.removeMember(id, uuid, world);

            //Remove members from plot in database.
            PlotSystem.getInstance().plotSQL.update("DELETE FROM plot_members WHERE id=" + id + " AND uuid='" + uuid + "';");

            //Send message to plot owner.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //If the player is on this server send them a message.
            if (p != null) {

                p.sendMessage(Utils.chat("&cYou have left plot &3" + id));

            } else {

                //Add the message to the database so it can be sent wherever they are currently.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&cYou have left plot &4" + id + "');");

            }
        }
    }
}
