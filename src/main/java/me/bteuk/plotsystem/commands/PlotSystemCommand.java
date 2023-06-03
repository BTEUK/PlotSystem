package me.bteuk.plotsystem.commands;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotSystemCommand implements CommandExecutor {

    //SQL
    final PlotSQL plotSQL;
    final GlobalSQL globalSQL;


    public PlotSystemCommand(GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.plotSQL = plotSQL;
        this.globalSQL = globalSQL;

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        //If there are no arguments return.
        if (args.length == 0) {

            sender.sendMessage(Utils.error("/plotsystem help"));
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
                    sender.sendMessage(Utils.error("/plotsystem setalias [location] [alias]"));
                }
                break;

            default:

                sender.sendMessage(Utils.error("/plotsystem help"));

        }

        return true;

    }

    private void help(CommandSender sender) {

        sender.sendMessage(Component.text("/plotsystem setalias [location] [alias]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/plotsystem selectiontool ", NamedTextColor.GRAY)
                .append(Utils.line("- Get the selection tool to create plots.")));
        sender.sendMessage(Component.text("/plotsystem create plot ", NamedTextColor.GRAY)
                .append(Utils.line("- Create a plot for your current selection.")));
        sender.sendMessage(Component.text("/plotsystem delete plot <plotID> ", NamedTextColor.GRAY)
                .append(Utils.line("- Delete an unclaimed plot.")));
        sender.sendMessage(Component.text("/plotsystem create location [name] <Xmin> <Zmin> <Xmax> <Zmax>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/plotsystem delete location [name]", NamedTextColor.GRAY));

    }

    private void selectionTool(CommandSender sender) {

        //Check if the sender is a player.
        if (!(sender instanceof Player)) {

            sender.sendMessage(Utils.error("You must be a player to use this command."));
            return;

        }

        //Get the user.
        User u = PlotSystem.getInstance().getUser((Player) sender);

        //Check if the user has permission.
        if (!u.player.hasPermission("uknet.plots.select")) {

            u.player.sendMessage(Utils.error("You do not have permission to do this."));
            return;

        }

        //Give the player a selection tool.
        u.selectionTool.giveSelectionTool();

    }

    private void setAlias(CommandSender sender, String location, String alias) {

        if (sender instanceof Player p) {

            if (!p.hasPermission("uknet.plots.setalias")) {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return;
            }

        }

        if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + location + "';")) {

            plotSQL.update("UPDATE location_data SET alias='" + alias.replace("'", "\\'") + "' WHERE name='" + location + "';");
            sender.sendMessage(Utils.success("Set alias of location ")
                    .append(Component.text(location, NamedTextColor.DARK_AQUA))
                    .append(Utils.success(" to "))
                    .append(Component.text(alias, NamedTextColor.DARK_AQUA)));

        } else {
            sender.sendMessage(Utils.error("The location ")
                    .append(Component.text(location, NamedTextColor.DARK_RED))
                    .append(Utils.error(" does not exist.")));
        }
    }
}
