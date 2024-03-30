package net.bteuk.plotsystem.commands;

import net.bteuk.network.Network;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.gui.ClaimGui;
import net.bteuk.plotsystem.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.sql.Tutorials.TUTORIAL_REQUIRED_MESSAGE;
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

        //If the plot is valid open the claim plot gui.
        if (validPlot(u)) {

            NetworkUser user = Network.getInstance().getUser(u.player);

            if (!hasClaimPermission(u, user)) {
                return true;
            }

            //Open claim gui.
            u.claimGui = new ClaimGui(u);
            u.claimGui.open(user);

        }

        return true;

    }

    public boolean validPlot(User u) {

        //If the player is not in a plot tell them.
        if (u.inPlot == 0) {

            u.player.sendMessage(Utils.error("You are not in a plot!"));
            return false;

        }

        //If the plot is already claimed tell them.
        //If they are the owner or a member tell them.
        if (u.plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + " AND uuid='" + u.player.getUniqueId() + "' AND is_owner=1;")) {

            u.player.sendMessage(Utils.error("You are already the owner of this plot!"));
            return false;

        } else if (u.plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + " AND uuid='" + u.player.getUniqueId() + "' AND is_owner=0;")) {

            u.player.sendMessage(Utils.error("You are already a member of this plot!"));
            return false;

        } else if (u.plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + u.inPlot + " AND status='claimed';")) {

            u.player.sendMessage(Utils.error("This plot is already claimed!"));
            return false;

        }

        //Check if you do not already have the maximum number of plots.
        if (u.plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + u.uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

            u.player.sendMessage(Utils.error("You have reached the maximum number of plots."));
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
