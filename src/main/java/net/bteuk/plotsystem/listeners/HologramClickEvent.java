package net.bteuk.plotsystem.listeners;

import net.bteuk.network.utils.enums.PlotStatus;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.utils.ParseUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HologramClickEvent implements Listener {

    public HologramClickEvent(PlotSystem instance) {
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onHologramClick(eu.decentsoftware.holograms.event.HologramClickEvent e) {
        // Get the click event based on the hologram name.
        String name = e.getHologram().getName();
        String[] args = name.split(",");
        if (args.length == 2) {
            int plot = ParseUtils.toInt(args[0]);
            if (plot != 0) {
                if (args[0].equals(PlotStatus.UNCLAIMED.name())) {
                    e.getPlayer().performCommand("claim " + plot);
                } else {
                    e.getPlayer().performCommand("plot info " + plot);
                }
            }
        }
    }
}
