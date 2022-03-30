package me.bteuk.plotsystem.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.CreatePlotGui;
import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.Multiverse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand {

    PlotSQL plotSQL;
    NavigationSQL navigationSQL;

    public CreateCommand(PlotSQL plotSQL, NavigationSQL navigationSQL) {

        this.plotSQL = plotSQL;
        this.navigationSQL = navigationSQL;

    }

    public void create(CommandSender sender, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(Utils.chat("&c/plotsystem create [plot, location, zone, world]"));
            return;

        }

        switch (args[1]) {

            case "plot":

                createPlot(sender);

            case "location":

                createLocation(sender, args);
                break;

            case "zone":

                break;

            case "world":

                createWorld(sender, args);
                break;

            default:

                sender.sendMessage(Utils.chat("&c/plotsystem create [plot, location, zone, world]"));

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

        //Check if the plot is valid
        if (u.selectionTool.size() < 3) {

            u.player.sendMessage(Utils.chat("&cYou must select at least 3 points for a valid plot!"));
            return;

        }

        //Open the plot creation menu
        u.selectionTool.area();
        u.selectionTool.setDefaultSize();

        //Get the user from the network plugin.
        NetworkUser user = Network.getInstance().getUser(u.player);
        user.uniqueGui = CreatePlotGui.createPlotGui(u);
        user.uniqueGui.open(user);

    }

    private void createLocation(CommandSender sender, String[] args) {

        //Check if the sender is a player.
        //If so, check if they have permission.
        if (sender instanceof Player) {

            Player p = (Player) sender;

            if (!p.hasPermission("uknet.plots.create.location")) {

                p.sendMessage(Utils.chat("&cYou to not have permission to use this command!"));
                return;

            }
        }

        //Check if they have enough args.
        if (args.length < 8) {

            sender.sendMessage(Utils.chat("&c/plotsystem create location [name] [world] <Xmin> <Zmin> <Xmax> <Zmax>"));
            return;

        }

        int xmin;
        int zmin;

        int xmax;
        int zmax;

        //Check if the coordinates are actual numbers.
        try {

            xmin = Integer.parseInt(args[4]);
            zmin = Integer.parseInt(args[5]);

            xmax = Integer.parseInt(args[6]);
            zmax = Integer.parseInt(args[7]);

        } catch (NumberFormatException e) {

            sender.sendMessage(Utils.chat("&c/plotsystem create location [name] [world] <Xmin> <Zmin> <Xmax> <Zmax>"));
            return;

        }

        //Check if the location name is unique.
        if (plotSQL.hasRow("SELECT name FROM location_data WHERE name=" + args[2] + ";")) {

            sender.sendMessage(Utils.chat("&cThe location " + args[2] + " already exists."));
            return;

        }

        //Check if the world exists.
        if (!plotSQL.hasRow("SELECT name FROM world_data WHERE name=" +args[3] + ", server= " + PlotSystem.SERVER_NAME + ";")) {

            sender.sendMessage(Utils.chat("&cThe world " + args[3] + " does not exist on this server."));
            return;

        }

        int coordMin = navigationSQL.addCoordinate(new Location(
                Bukkit.getWorld(args[3]),
                xmin, 0, zmin, 0, 0));

        int coordMax = navigationSQL.addCoordinate(new Location(
                Bukkit.getWorld(args[3]),
                xmax, 256, zmax, 0, 0));

        //Add the location to the database.
        if (plotSQL.update("INSERT INTO location_data(name, world, server, coordMin, coordMax) VALUES(" + args[2] + ", " + args[3] + ", " +  coordMin + ", " + coordMax + ");")) {

            sender.sendMessage(Utils.chat("&aAdded new location " + args[2] + " to world " + args[3]));

        } else {

            sender.sendMessage(Utils.chat("&cAn error occurred, please check the console for more info."));
            Bukkit.getLogger().warning("An error occured while adding new location!");

        }

    }

    private void createWorld(CommandSender sender, String[] args) {

        //Check if the sender is a player.
        //If so, check if they have permission.
        if (sender instanceof Player) {

            Player p = (Player) sender;

            if (!p.hasPermission("uknet.plots.create.world")) {

                p.sendMessage(Utils.chat("&cYou to not have permission to use this command!"));
                return;

            }
        }

        //Check if they have enough args.
        if (args.length < 3) {

            sender.sendMessage(Utils.chat("&c/plotsystem create location [name]"));
            return;

        }

        //Check for duplicate name.
        if (plotSQL.hasRow("SELECT name FROM world_data WHERE server=" + PlotSystem.SERVER_NAME + " AND name=" + args[2] + ";")) {

            sender.sendMessage(Utils.chat("&cA world with the name " + args[2] + " already exists."));
            return;

        }

        //Create world
        if (!Multiverse.createVoidWorld(args[2])) {

            sender.sendMessage(Utils.chat("&cWorld failed to create, please check the console!"));
            return;

        }

        //Add the world to the database.
        if (plotSQL.update("INSERT INTO world_data(name, type, server) VALUES(" + args[2] + ", 'build', " + PlotSystem.SERVER_NAME + ");")) {

            sender.sendMessage(Utils.chat("&aCreated new world " + args[2] + "."));

        } else {

            sender.sendMessage(Utils.chat("&cAn error occurred, please check the console for more info."));
            Bukkit.getLogger().warning("An error occurred while creating a new world!");

        }
    }
}
