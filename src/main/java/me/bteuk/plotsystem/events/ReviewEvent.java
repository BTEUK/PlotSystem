package me.bteuk.plotsystem.events;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.reviewing.Review;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ReviewEvent {

    public static void event(String uuid, String[] event) {

        //Events for claiming
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(uuid);

            if (p == null) {

                Bukkit.getLogger().warning(Utils.chat("Player " + uuid + " is not on the server the event was sent to!"));
                return;

            }

            User u = PlotSystem.getInstance().getUser(p);

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get plotsql.
            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Get world of plot.
            World world = Bukkit.getWorld(plotSQL.getString("SELECT world FROM location_data WHERE name=" +
                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                    + ";"));

            //Create new review instance for user.
            u.review = new Review(id, u);

            //Teleport the reviewer to the plot.
            p.teleport(WorldGuardFunctions.getCurrentLocation(id, world));

        }
    }
}
