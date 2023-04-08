package me.bteuk.plotsystem.events;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.reviewing.Review;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReviewEvent {

    public static void event(String uuid, String[] event) {

        //Events for claiming
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            if (p == null) {

                Bukkit.getLogger().warning("Player " + uuid + " is not on the server the event was sent to!");
                return;

            }

            User u = PlotSystem.getInstance().getUser(p);

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get plotsql.
            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Get world of plot.
            World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";"));

            //Check if the plot is still submitted.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                //Set the plot to under review.
                plotSQL.update("UPDATE plot_data SET status='reviewing' WHERE id=" + id + ";");

                //Create new review instance for user.
                u.review = new Review(id, u);

                //Add the reviewer to the plot.
                WorldGuardFunctions.addMember(id, uuid, world);

                //Teleport the reviewer to the plot.
                p.teleport(WorldGuardFunctions.getCurrentLocation(event[2], world));

            } else {

                p.sendMessage(Utils.error("The plot is not submitted."));

            }
        }
    }
}
