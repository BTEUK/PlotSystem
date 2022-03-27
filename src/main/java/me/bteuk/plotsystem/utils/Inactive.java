package me.bteuk.plotsystem.utils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.mysql.PointsData;
import org.bukkit.entity.Player;

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

            //Check if the plot is on this server.
            if (plotSQL.hasRow("SELECT name FROM location_data WHERE name=" + plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plot + ";") + " AND server=" + PlotSystem.SERVER_NAME + ";")) {

                //Get worlds of plot and save location.
                World copyWorld = Bukkit.getWorld(plotSQL.getString("SELECT name FROM world_data WHERE server=" + PlotSystem.SERVER_NAME + " AND type='save';"));
                World pasteWorld = Bukkit.getWorld(plotSQL.getString("SELECT world FROM location_data WHERE name=" +
                        plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plot + ";") + ";"));

                //Get the plot bounds.
                List<BlockVector2> vector = WorldGuardFunctions.getPoints(plot, pasteWorld);

                //Revert plot to original state.
                WorldEditor.updateWorld(vector, copyWorld, pasteWorld);

                //Remove all members from the worldguard plot.
                WorldGuardFunctions.clearMembers(plot, pasteWorld);

                //Get the uuid of the plot owner.
                String uuid = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plot + " AND is_owner=1;");

                //Remove all members of plot in database.
                plotSQL.update("DELETE FROM plot_members WHERE id=" + plot + ";");

                //Set plot status to unclaimed.
                plotSQL.update("UPDATE plot_data SET status='unclaimed' WHERE id=" + plot + ";");

                //Add message for the plot owner to the database to notify them that their plot was removed.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES(" + uuid + ",'&cPlot " + plot + "removed due to inactivity!');");

                //Log plot removal to console.
                PlotSystem.getInstance().getLogger().info(Utils.chat("&cPlot " + plot + " removed due to inactivity!"));

            }
        }
    }
}
