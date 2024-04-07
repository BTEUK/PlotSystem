package net.bteuk.plotsystem.commands;

import net.bteuk.network.Network;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.gui.ClaimGui;
import net.bteuk.plotsystem.utils.ParseUtils;
import net.bteuk.plotsystem.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.sql.Tutorials.TUTORIAL_REQUIRED_MESSAGE;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.TUTORIALS;

public class ClaimCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Get the user.
        User u = PlotSystem.getInstance().getUser(p);

        int plot = 0;
        boolean inPlot = false;
        if (args.length > 0) {
            plot = ParseUtils.toInt(args[0]);
        }
        if (plot == 0) {
            plot = u.inPlot;
            inPlot = true;
        }

        //If the plot is valid open the claim plot gui.
        if (validPlot(u, plot, inPlot)) {

            NetworkUser user = Network.getInstance().getUser(u.player);

            if (!hasClaimPermission(u, user)) {
                return true;
            }

            //Open claim gui.
            u.claimGui = new ClaimGui(u, plot);
            u.claimGui.open(user);

        }

        return true;

    }

    public boolean validPlot(User u, int plot, boolean inPlot) {

        // If the player is not in a plot tell them.
        if (plot == 0) {
            if (inPlot) {
                u.player.sendMessage(Utils.error("You are not standing in a plot."));
            } else {
                u.player.sendMessage(Utils.error("This is not a valid plot."));
            }
            return false;
        }

        // If the plot is already claimed tell them.
        // If they are the owner or a member tell them.
        if (u.plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + u.player.getUniqueId() + "' AND is_owner=1;")) {

            u.player.sendMessage(Utils.error("You are already the owner of this plot!"));
            return false;

        } else if (u.plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + u.player.getUniqueId() + "' AND is_owner=0;")) {

            u.player.sendMessage(Utils.error("You are already a member of this plot!"));
            return false;

        } else if (u.plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plot + " AND status='claimed';")) {

            u.player.sendMessage(Utils.error("This plot is already claimed!"));
            return false;

        }

        // Check if you do not already have the maximum number of plots.
        if (u.plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + u.uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

            u.player.sendMessage(Utils.error("You have reached the maximum number of plots."));
            return false;
        }

        // Check if the plot is on this server.
        if (!u.plotSQL.hasRow("SELECT pd.id FROM plot_data AS pd INNER JOIN location_data AS ld ON ld.name=pd.location WHERE pd.id=" + plot + " ld.server='" + SERVER_NAME + "';")) {
            u.player.sendMessage(Utils.error("This plot is on another server, unable to claim it from here."));
            return false;
        }

        //Checks passed, return true.
        return true;
    }

    public static boolean hasClaimPermission(User u, NetworkUser user) {

        //Make sure the player has permission to claim plots, else they must complete the tutorial first.
        //Only checked if tutorials are enabled.
        if (!(user.player.hasPermission("uknet.plots.claim.all") || user.player.hasPermission("uknet.plots.claim.easy")) && TUTORIALS) {

            user.player.sendMessage(TUTORIAL_REQUIRED_MESSAGE);
            return false;

        }

        //Check if the player has permission to claim a plot of this difficulty.
        if (!user.player.hasPermission("uknet.plots.claim.all")) {
            switch (u.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + u.inPlot + ";")) {

                case 1 -> {
                    if (!user.player.hasPermission("uknet.plots.claim.easy")) {
                        user.player.sendMessage(Utils.error("You do not have permission to claim an ")
                                .append(Component.text("Easy", NamedTextColor.DARK_RED))
                                .append(Utils.error(" plot.")));
                        return false;
                    }
                }

                case 2 -> {
                    if (!user.player.hasPermission("uknet.plots.claim.normal")) {
                        user.player.sendMessage(Utils.error("You do not have permission to claim a ")
                                .append(Component.text("Normal", NamedTextColor.DARK_RED))
                                .append(Utils.error(" plot.")));
                        return false;
                    }
                }

                case 3 -> {
                    if (!user.player.hasPermission("uknet.plots.claim.hard")) {
                        user.player.sendMessage(Utils.error("You do not have permission to claim a ")
                                .append(Component.text("Hard", NamedTextColor.DARK_RED))
                                .append(Utils.error(" plot.")));
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
