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
    GlobalSQL globalSQL;


    public PlotSystemCommand(GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.plotSQL = plotSQL;
        this.globalSQL = globalSQL;

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        //If there are no arguments return.
        if (args.length == 0) {

            sender.sendMessage(Utils.chat("&c/plotsystem help"));
            return true;

        }

        switch (args[0]) {

            case "selectiontool":

                selectionTool(sender);
                break;

            case "create":

                CreateCommand createCommand = new CreateCommand(globalSQL, plotSQL);
                createCommand.create(sender, args);
                break;

            case "delete":

                DeleteCommand deleteCommand = new DeleteCommand(globalSQL, plotSQL);
                deleteCommand.delete(sender, args);
                break;

            case "help":

                help(sender);
                break;

            case "setalias":

                if (args.length == 3) {
                    setAlias(sender, args[1], args[2]);
                } else {
                    sender.sendMessage(Utils.chat("&c/plotsystem setalias [location] [alias]"));
                }
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
            return;

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

    private void setAlias(CommandSender sender, String location, String alias) {

        if (sender instanceof Player p) {

            if (!p.hasPermission("uknet.plots.setalias")) {
                p.sendMessage(Utils.chat("&cYou do not have permission to use this command."));
            }

        }

        if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + location + "';")) {

            plotSQL.update("UPDATE location_data SET alias='" + alias + "' WHERE name='" + location+ "';");
            sender.sendMessage(Utils.chat("&aSet alias of location &3" + location + "&a to &3" + alias + "&a."));

        } else {
            sender.sendMessage(Utils.chat("&cThe location " + location + " does not exist."));
        }
    }
}
