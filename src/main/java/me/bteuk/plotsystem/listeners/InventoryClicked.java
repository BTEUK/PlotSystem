package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClicked implements Listener {

    PlotSystem instance;

    public InventoryClicked(PlotSystem plugin) {

        instance = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getCurrentItem() == null) {
            return;
        }

        User u = PlotSystem.getInstance().getUser((Player) e.getWhoClicked());
        if (e.getCurrentItem().equals(PlotSystem.gui)) {
            e.setCancelled(true);
            u.player.closeInventory();
            Bukkit.getScheduler().runTaskLater(instance, () -> u.player.openInventory(MainGui.GUI(u)), 1);
        }
    }
}
