package me.bteuk.plotsystem.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.CreatePlotGui;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.Multiverse;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CreateCommand {

    GlobalSQL globalSQL;
    PlotSQL plotSQL;

    public CreateCommand(GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

    }

    public void create(CommandSender sender, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(Utils.chat("&c/plotsystem create [plot, location, zone]"));
            return;

        }

        switch (args[1]) {

            case "plot":

                createPlot(sender);
                break;

            case "location":

                createLocation(sender, args);
                break;

            case "zone":

                break;

            default:

                sender.sendMessage(Utils.chat("&c/plotsystem create [plot, location, zone]"));

        }


    }

    private void createPlot(CommandSender sender) {

        //Check if the sender is a player
        if (!(sender instanceof Player)) {

            sender.sendMessage(Utils.chat("&cThis command can only be used by players!"));
            return;

        }

        //Get the user
        User u = PlotSystem.getInstance().getUser((Player) sender);

        //Check if the user has permission to use this command
        if (!u.player.hasPermission("uknet.plots.create.plot")) {

            u.player.sendMessage(Utils.chat("&cYou do not have permission to use this command!"));
            return;

        }

        //Check if the plot is valid, meaning that at least 3 points are selected with the selection tool.
        if (u.selectionTool.size() < 3) {

            u.player.sendMessage(Utils.chat("&cYou must select at least 3 points for a valid plot!"));
            return;

        }

        //Open the plot creation menu
        //Calculate the area of the plot and set a default size estimate.
        u.selectionTool.area();
        u.selectionTool.setDefaultSize();

        //Get the user from the network plugin, this plugin handles all guis.
        NetworkUser user = Network.getInstance().getUser(u.player);
        //UniqueGui allows the creation of a gui that is unique to this player.
        user.uniqueGui = CreatePlotGui.createPlotGui(u);
        user.uniqueGui.open(user);

    }

    private void createLocation(CommandSender sender, String[] args) {

        //Check if the sender is a player.
        //If so, check if they have permission.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be executed by a player."));
            return;

        }

        if (!p.hasPermission("uknet.plots.create.location")) {

            p.sendMessage(Utils.chat("&cYou do not have permission to use this command!"));
            return;

        }

        //Check if they have enough args.
        if (args.length < 7) {

            sender.sendMessage(Utils.chat("&c/plotsystem create location [name] <Xmin> <Zmin> <Xmax> <Zmax>"));
            return;

        }

        int xmin;
        int zmin;

        int xmax;
        int zmax;

        //Check if the coordinates are actual numbers.
        try {

            xmin = Integer.parseInt(args[3]);
            zmin = Integer.parseInt(args[4]);

            xmax = Integer.parseInt(args[5]);
            zmax = Integer.parseInt(args[6]);

        } catch (NumberFormatException e) {

            sender.sendMessage(Utils.chat("&c/plotsystem create location [name] <Xmin> <Zmin> <Xmax> <Zmax>"));
            return;

        }

        //Check if the location name is unique.
        if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + args[2] + "';")) {

            sender.sendMessage(Utils.chat("&cThe location " + args[2] + " already exists."));
            return;

        }

        //Get the exact regions of the selected coordinates.
        int regionXMin = Math.floorDiv(xmin, 512);
        int regionZMin = Math.floorDiv(zmin, 512);

        int regionXMax = Math.floorDiv(xmax, 512);
        int regionZMax = Math.floorDiv(zmax, 512);

        //Calculate the coordinate transformation.
        int xTransform = -(regionXMin * 512);
        int zTransform = -(regionZMin * 512);

        //Create the world and add the regions.
        Multiverse.createVoidWorld(args[2]);

        //Copy regions from save world and add them to build world with transformed coordinates.
        File copy = new File(PlotSystem.getInstance().getConfig().getString("save_world")).getAbsoluteFile();
        File paste = new File(args[2]).getAbsoluteFile();

        //Worlds must be unloaded before copying terrain to prevent corrupted files.
        //TODO: unload worlds

        //Iterate through regions.
        try {
            for (int i = regionXMin; i <= regionXMax; i++) {

                for (int j = regionZMin; j <= regionZMax; j++) {

                    FileUtils.copyFile(new File(copy + "/region/r." + i + "." + j + ".mca"),
                            new File(paste + "/region/r." + (i + xTransform / 512) + "." + (j + zTransform / 512) + ".mca"));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            p.sendMessage(Utils.chat("&cAn error occurred while copying the terrain."));
            p.sendMessage(Utils.chat("&cMake sure all the region files exist."));

            //Since file-copy failed remove world from server.
            Multiverse.deleteWorld(args[2]);

            return;
        }


        int coordMin = globalSQL.addCoordinate(new Location(
                Bukkit.getWorld(args[2]),
                (regionXMin * 512), 0, (regionZMin * 512), 0, 0));

        int coordMax = globalSQL.addCoordinate(new Location(
                Bukkit.getWorld(args[2]),
                ((regionXMax * 512) + 511), 256, ((regionZMax * 512) + 511), 0, 0));

        //Add the location to the database.
        if (plotSQL.update("INSERT INTO location_data(name, server, coordMin, coordMax, xTransform, zTransform) VALUES('"
                + args[2] + "','" + PlotSystem.SERVER_NAME + "'," + coordMin + "," + coordMax + "," + xTransform + "," + zTransform + ");")) {

            sender.sendMessage(Utils.chat("&aCreated new location " + args[2]));

            //Set the status of all effected regions in the region database.
            for (int i = regionXMin; i <= regionXMax; i++) {

                for (int j = regionZMin; j <= regionZMax; j++) {

                    //Change region status in region database.
                    //If it already exists remove members.
                    globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + p.getUniqueId() + "','network','"
                            + globalSQL.getString("SELECT name FROM server_data WHERE type='earth';") + "'," +
                            "'region set plotsystem " + i + " " + j + "');");

                    //Add region to database.
                    String region = i + "," + j;
                    plotSQL.update("INSERT INTO regions(region,server,location) VALUES('" + region + "','" + PlotSystem.SERVER_NAME + "','" + args[2] + "');");

                }
            }

        } else {

            sender.sendMessage(Utils.chat("&cAn error occurred, please check the console for more info."));
            Bukkit.getLogger().warning("An error occured while adding new location!");

        }

    }
}
