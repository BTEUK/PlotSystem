package me.bteuk.plotsystem.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.ClaimGui;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimCommand implements CommandExecutor {

    PlotSQL plotSQL;

    public ClaimCommand(PlotSQL plotSQL) {

        this.plotSQL = plotSQL;

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be used by a player."));
            return true;

        }

        //Get the user.
        User u = PlotSystem.getInstance().getUser(p);

        //If the plot is valid open the claim plot gui.
        if (validPlot(u)) {

            NetworkUser user = Network.getInstance().getUser(u.player);

            //Open claim gui.
            u.claimGui = new ClaimGui(u);
            u.claimGui.open(user);

        }

        return true;

    }

    public boolean validPlot(User u) {

        //If the player is not in a plot return false.
        if (u.inPlot == 0) {

            u.player.sendMessage(Utils.chat("&cYou are not in a plot."));
            return false;

        }

        //If you already own the plot, return.
        if (u.plotOwner) {

            u.player.sendMessage(Utils.chat("&cYou already own this plot."));
            return false;

        } else if (u.plotMember) {

            u.player.sendMessage(Utils.chat("&cYou are a member of this plot."));
            return false;

        }

        //If the plot is already claimed, tell the player they can not claim it, but they can be invited by the owner.
        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + ";")) {

            u.player.sendMessage(Utils.chat("&cThis plot is already claimed, if you wish to build in this plot ask the plot owner to invite you."));
            return false;

        }

        //Checks passed, return true.
        return true;
    }
}
