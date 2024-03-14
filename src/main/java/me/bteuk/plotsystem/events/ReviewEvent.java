package me.bteuk.plotsystem.events;

import me.bteuk.network.Network;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.reviewing.Review;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.bteuk.plotsystem.PlotSystem.LOGGER;

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

            //If user is null stop.
            if (u == null) {
                LOGGER.severe(String.format("User for player %s is null, this should not be possible!!!", p.getName()));
                return;
            }

            //If the user is already reviewing, prevent this was happening.
            if (u.review != null) {
                u.player.sendMessage(Utils.error("You are already reviewing a plot, please complete this one first!"));
                return;
            }

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get plotsql.
            PlotSQL plotSQL = Network.getInstance().getPlotSQL();

            //Get world of plot.
            World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";"));

            //Check if the plot is still submitted.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                //Set the plot to under review.
                plotSQL.update("UPDATE plot_data SET status='reviewing' WHERE id=" + id + ";");

                //Create new review instance for user.
                u.review = new Review(id, u);

                //Add the reviewer to the plot.
                try {
                    WorldGuardFunctions.addMember(String.valueOf(id), uuid, world);
                } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                    u.player.sendMessage(Component.text("Unable to add you as a member of the plot you are reviewing, please contact an admin to report this issue."));
                    e.printStackTrace();
                }

                //Teleport the reviewer to the plot.
                try {
                    Location l = WorldGuardFunctions.getCurrentLocation(event[2], world);
                    p.teleport(l);
                } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                    u.player.sendMessage(Component.text("Unable to teleport you to the plot, please contact an admin to report this issue."));
                    e.printStackTrace();
                }

            } else {

                p.sendMessage(Utils.error("The plot is no longer submitted."));

            }
        }
    }
}
