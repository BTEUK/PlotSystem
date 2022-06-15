package me.bteuk.plotsystem.commands;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotSystemCommand implements CommandExecutor {

    //SQL
    PlotSQL plotSQL;

    //Commands
    CreateCommand createCommand;


    public PlotSystemCommand(GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.plotSQL = plotSQL;

        createCommand = new CreateCommand(globalSQL,plotSQL);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        //If there are no arguments return.
        if (args.length == 0) {

            sender.sendMessage(Utils.chat("&c/plotsystem help"));

        }

        switch (args[0]) {

            case "selectiontool":

                selectionTool(sender);
                break;

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

    private void selectionTool(CommandSender sender) {

        //Check if the sender is a player.
        if (!(sender instanceof Player)) {

            sender.sendMessage(Utils.chat("&cYou must be a player to use this command."));

        }

        //Get the user.
        User u = PlotSystem.getInstance().getUser((Player) sender);

        //Check if the user has permission.
        if (!u.player.hasPermission("uknet.plots.select")) {

            u.player.sendMessage(Utils.chat("&cYou do not have permission to use this command."));

        }

        //Give the player a selection tool.
        u.selectionTool.giveSelectionTool();

    }

}
