package me.bteuk.plotsystem.commands;

import me.bteuk.network.commands.Plot;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.Multiverse;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DeleteCommand {

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    public DeleteCommand(GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

    }

    public void delete(CommandSender sender, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(Utils.chat("&c/plotsystem delete [plot, location, zone]"));
            return;

        }

        switch (args[1]) {

            case "plot":

                deletePlot(sender, args);
                break;

            case "location":

                deleteLocation(sender, args);
                break;

            case "zone":

                break;

            default:

                sender.sendMessage(Utils.chat("&c/plotsystem create [plot, location, zone]"));

        }

    }

    private void deletePlot(CommandSender sender, String[] args) {

        //Check if the sender has permission.
        //If there are no additional args then the sender must be a player.
        if (sender instanceof Player p) {

            if (!(p.hasPermission("uknet.plots.delete.plot"))) {
                p.sendMessage(Utils.chat("&cYou do not have permission to use this command."));
                return;
            }

        } else if (args.length < 3) {

            sender.sendMessage(Utils.chat("&c/plotsystem delete plot <plotID>"));
            return;

        }

        int plotID;

        if (args.length >= 3) {

            try {

                plotID = Integer.parseInt(args[2]);

            } catch (NumberFormatException e) {

                sender.sendMessage("&c/plotsystem delete plot <plotID>");
                return;

            }

        } else {

            Player p = (Player) sender;

            //Get plot that the player is standing in.
            User u = PlotSystem.getInstance().getUser(p);

            if (u.inPlot == 0) {

                p.sendMessage(Utils.chat("&cYou are not standing in a plot."));
                return;

            }

            plotID = u.inPlot;

        }

        if (plotID == 0) {
            return;
        }

        //Check if plot exists.
        if (!plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + ";")) {
            sender.sendMessage(Utils.chat("&cThis plot does not exist."));
        }

        //Check if plot is unclaimed
        if (!(plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='unclaimed'"))) {
            sender.sendMessage(Utils.chat("&cThis plot is claimed, you can only delete unclaimed plots."));
            return;
        }

        //Get world of plot.
        World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";"));

        //If world is null then the plot is not on this server.
        if (world == null) {
            sender.sendMessage(Utils.chat("&cThe plot is not on this server."));
            return;
        }

        //Delete plot.
        if (WorldGuardFunctions.deletePlot(plotID, world)) {

            //Set plot to deleted in database.
            plotSQL.update("UPDATE plot_data SET status='deleted' WHERE id=" + plotID + ";");
            sender.sendMessage(Utils.chat("&aPlot &3" + plotID + "&a deleted."));

        } else {

            sender.sendMessage(Utils.chat("&aAn error occured while deleting the plot."));
            PlotSystem.getInstance().getLogger().warning("An error occured while deleting plot &3" + plotID + "&a from WorldGuard.");

        }
    }

    private void deleteLocation(CommandSender sender, String[] args) {

        //If sender is a player, check for permission.
        if (sender instanceof Player p) {

            if (!(p.hasPermission("uknet.plots.delete.location"))) {
                p.sendMessage(Utils.chat("&cYou do not have permission to use this command."));
                return;
            }

        }

        //Check arg count.
        if (args.length < 3) {

            sender.sendMessage(Utils.chat("&c/plotsystem delete location [name]"));
            return;

        }

        //Check if location exists.
        if (!(plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + args[2] + "';"))) {

            sender.sendMessage(Utils.chat("&cThe location " + args[2] + " does not exist."));
            return;

        }

        //Check if the location is on this server.
        if (!(plotSQL.getString("SELECT server FROM location_data WHERE name='" + args[2] + "';").equals(PlotSystem.SERVER_NAME))) {

            sender.sendMessage(Utils.chat("&cThis location is not on this server."));
            return;

        }

        //If location has plots, cancel.
        if (plotSQL.hasRow("SELECT id FROM plot_data WHERE location='" + args[2] + "' AND status<>'completed' AND status<>'deleted';")) {

            sender.sendMessage(Utils.chat("&cThis location active has plots, all plots must be deleted or completed to remove the location."));
            return;

        }

        //Delete location.
        if (Multiverse.deleteWorld(args[2])) {

            //Delete location from database.
            plotSQL.update("DELETE FROM location_data WHERE name='" + args[2] + "';");
            sender.sendMessage(Utils.chat("&aDeleted location &3" + args[2] + "&a."));
            PlotSystem.getInstance().getLogger().info("Delete location " + args[2] + ".");

            //Get regions from database.
            ArrayList<String> regions = plotSQL.getStringList("SELECT region FROM regions WHERE location='" + args[2] + "';");

            //Delete regions from database.
            plotSQL.update("DELETE FROM regions WHERE location='" + args[2] + "';");

            //Iterate through regions to unlock them on Earth.
            for (String region : regions) {
                globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES(NULL,'network','"
                        + globalSQL.getString("SELECT name FROM server_data WHERE type='earth';") + "'," +
                        "'region set default " + region + "');");
            }

        } else {

            sender.sendMessage(Utils.chat("&cAn error occurred while deleting the world."));
            PlotSystem.getInstance().getLogger().warning("An error occurred while deleting world " + args[2] + ".");

        }
    }
}
