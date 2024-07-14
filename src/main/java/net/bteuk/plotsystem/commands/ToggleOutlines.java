package net.bteuk.plotsystem.commands;

import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.utils.User;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to toggle the plot outlines for the current session of the player.
 */
public class ToggleOutlines implements CommandExecutor {

    private final PlotSystem instance;

    public ToggleOutlines(PlotSystem instance) {
        PluginCommand command = instance.getCommand("toggleoutlines");
        if (command == null) {
            instance.getLogger().warning(StringUtils.capitalize("toggleoutlines") + " command not added to plugin.yml, it will therefore not be enabled.");
        } else {
            command.setExecutor(this);
        }
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Get the user.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatUtils.error("This command can only be used by players."));
            return true;
        }

        User u = instance.getUser(p);

        if (u == null) {
            p.sendMessage(ChatUtils.error("An error has occurred, please rejoin and contact your server admin."));
            return true;
        }

        if (u.isDisableOutlines()) {
            // Enable outlines.
            u.setDisableOutlines(false);
            instance.getOutlines().addNearbyOutlines(u);
            p.sendMessage(ChatUtils.success("Enabled outlines"));
        } else {
            // Disable outlines.
            u.setDisableOutlines(true);
            p.sendMessage(ChatUtils.success("Disabled outlines for this session"));

            // Remove existing outlines.
            instance.getOutlines().removeOutlinesForPlayer(p);
        }
        return true;
    }
}