package me.bteuk.plotsystem.commands;

import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlotSystemCommand implements CommandExecutor {

    //SQL
    PlotSQL plotSQL;
    NavigationSQL navigationSQL;

    //Commands
    CreateCommand createCommand;


    public PlotSystemCommand(PlotSQL plotSQL, NavigationSQL navigationSQL) {

        this.plotSQL = plotSQL;

        createCommand = new CreateCommand(plotSQL, navigationSQL);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        //If there are no arguments return.
        if (args.length == 0) {

            sender.sendMessage(Utils.chat("&c/plotsystem help"));

        }

        switch (args[0]) {

            case "create":

                createCommand.create(sender, args);
                break;

            case "delete":

                break;

            case "help":

                help(sender);
                break;

            default:

                sender.sendMessage(Utils.chat("&c/plotsystem help"));

        }

        return true;

    }

    private void help(CommandSender sender) {

    }

}
