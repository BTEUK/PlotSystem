package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

public class PlayerInteract implements Listener {

    PlotSQL plotSQL;

    public PlayerInteract(PlotSystem plugin, PlotSQL plotSQL) {

        //Register the listener.
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        //Set the reference to the plotSQL.
        this.plotSQL = plotSQL;

    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {

        User u = PlotSystem.getInstance().getUser(e.getPlayer());

        if (e.getPlayer().getOpenInventory().getType() != InventoryType.CRAFTING && e.getPlayer().getOpenInventory().getType() != InventoryType.CREATIVE) {
            return;
        }

        //Selection tool
        if (u.player.getInventory().getItemInMainHand().equals(PlotSystem.selectionTool)) {

            //You must have this permission to use the plot selection tool.
            //uknet = uknet plugins, plots = plotserver plugin, select = selection tool.
            if (!u.player.hasPermission("uknet.plots.select")) {

                e.setCancelled(true);
                u.player.sendMessage(Utils.chat("You do not have permission to use this tool!"));
                return;

            }

            //If the player left clicks this will (re)start the selection with the first point.
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {

                e.setCancelled(true);

                //Check if they are in a world where plots are allowed to be created.
                if (!plotSQL.hasRow("SELECT name FROM location_data WHERE name=" + e.getClickedBlock().getWorld() + ";")) {

                    u.player.sendMessage(Utils.chat("&cYou can't create plots in this world!"));

                }

                //If the selected point is in an existing plot cancel.
                if (WorldGuardFunctions.inRegion(e.getClickedBlock())) {

                    u.player.sendMessage(Utils.chat("&cThis point is in another plot!"));
                    return;

                }

                //Passed the checks, start a new selection at the clicked block.
                u.selectionTool.startSelection(e.getClickedBlock(), e.getClickedBlock().getWorld().toString());
                u.player.sendMessage(Utils.chat("&aStarted a new selection at " + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getZ()));

                //If the player right clicks then add a point to the existing selection.
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getHand().equals(EquipmentSlot.HAND)) {

                e.setCancelled(true);

                //Check if they are making their plot in the same world as their first point.
                if (!e.getClickedBlock().getWorld().equals(u.selectionTool.world())) {

                    u.player.sendMessage(Utils.chat("&cYou already started a selection in a different world, please create a new selection first."));

                }

                //If the player hasn't selected their first point cancel.
                if (u.selectionTool.size() == 0) {

                    u.player.sendMessage(Utils.chat("&cYou must first start your selection by left-clicking."));
                    return;

                }

                //If the selected point is in an existing plot cancel.
                if (WorldGuardFunctions.inRegion(e.getClickedBlock())) {

                    u.player.sendMessage(Utils.chat("&cThis point is in another plot!"));
                    return;

                }

                if (!u.selectionTool.addPoint(e.getClickedBlock())) {

                    return;

                }

                u.player.sendMessage(Utils.chat("&aAdded point at " + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getZ()));

            }


        }

    }
}