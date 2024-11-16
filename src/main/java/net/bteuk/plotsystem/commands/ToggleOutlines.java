package net.bteuk.plotsystem.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.utils.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to toggle the plot outlines for the current session of the player.
 */
public class ToggleOutlines extends AbstractCommand {

    private final PlotSystem instance;

    public ToggleOutlines(PlotSystem instance) {
        this.instance = instance;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        User u = instance.getUser(player);

        if (u == null) {
            player.sendMessage(ChatUtils.error("An error has occurred, please rejoin and contact your server admin."));
            return;
        }

        if (u.isDisableOutlines()) {
            // Enable outlines.
            u.setDisableOutlines(false);
            instance.getOutlines().addNearbyOutlines(u);
            player.sendMessage(ChatUtils.success("Enabled outlines"));
        } else {
            // Disable outlines.
            u.setDisableOutlines(true);
            player.sendMessage(ChatUtils.success("Disabled outlines for this session"));

            // Remove existing outlines.
            instance.getOutlines().removeOutlinesForPlayer(player);
        }
    }
}