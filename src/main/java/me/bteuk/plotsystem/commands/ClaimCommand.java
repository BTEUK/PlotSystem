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

        //If the player is not in a plot tell them.
        if (u.inPlot == 0) {

            u.player.sendMessage(Utils.chat("&cYou are not in a plot!"));
            return false;

        }

        //If the plot is already claimed tell them.
        //If they are the owner or a member tell them.
        if (u.plotOwner) {

            u.player.sendMessage(Utils.chat("&cYou are already the owner of this plot!"));
            return false;

        } else if (u.plotMember) {

            u.player.sendMessage(Utils.chat("&cYou are already a member of this plot!"));
            return false;

        } else if (u.isClaimed) {

            u.player.sendMessage(Utils.chat("&cThis plot is already claimed!"));
            return false;

        }

        //Check if you do not already have the maximum number of plots.
        if (u.plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + u.uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

            u.player.sendMessage(Utils.chat("&cYou have reached the maximum number of plots."));
            return false;

        }

        //Check if the player is allowed to claim this plot.
        int difficulty = u.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + u.inPlot + ";");

        //Check by difficulty, with the required role.
        if (difficulty == 3 && !(u.player.hasPermission("group.jrbuilder"))) {

            u.player.sendMessage(Utils.chat("&cYou must be at least Jr.Builder or higher to claim a 'hard difficulty' plot."));
            return false;

        } else if (difficulty == 2 && !(u.player.hasPermission("group.apprentice"))) {

            u.player.sendMessage(Utils.chat("&cYou must be at least Apprentice or higher to claim a 'medium difficulty' plot."));
            return false;

        } else if (difficulty == 1 && !(u.player.hasPermission("group.applicant"))) {

            u.player.sendMessage(Utils.chat("&cYou must complete the tutorial to claim a plot."));
            return false;

        }

        //Checks passed, return true.
        return true;
    }
}
