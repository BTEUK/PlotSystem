package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {


        PlotSystem.getInstance().getOutlines().refreshOutlinesForPlayer(event.getPlayer());

    }
}
