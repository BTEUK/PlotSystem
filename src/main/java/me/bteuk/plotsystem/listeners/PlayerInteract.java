package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.plots.Plots;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.tutorial.TutorialGui;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.WorldGuardFunctions;

public class PlayerInteract implements Listener {

	PlotSQL plotSQL;

	public PlayerInteract(Main plugin, PlotSQL plotSQL) {

		//Register the listener.
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		//Set the reference to the plotSQL.
		this.plotSQL = plotSQL;		

	}

	@EventHandler
	public void interactEvent(PlayerInteractEvent e) {

		User u = Main.getInstance().getUser(e.getPlayer());

		if (e.getPlayer().getOpenInventory().getType() != InventoryType.CRAFTING && e.getPlayer().getOpenInventory().getType() != InventoryType.CREATIVE) {
		    return;
		}
		
		if (e.getPlayer().getInventory().getItemInMainHand().equals(Main.gui)) {
			e.setCancelled(true);
			e.getPlayer().closeInventory();
			e.getPlayer().openInventory(MainGui.GUI(u));
		} else if (e.getPlayer().getInventory().getItemInMainHand().equals(Main.tutorialGui)) {
			e.setCancelled(true);
			u.player.closeInventory();
			u.previousGui = "none";
			u.player.openInventory(TutorialGui.GUI(u));
		}
		
		//Selection tool
		if (u.player.getInventory().getItemInMainHand().equals(Plots.selectionTool)) {
			
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
				if (!plotSQL.buildable(e.getClickedBlock().getWorld().getName())) {
					
					u.player.sendMessage(Utils.chat("&cYou can't create plots in this world!"));
					
				}
				
				//If the selected point is in an existing plot cancel.
				if (WorldGuardFunctions.inRegion(e.getClickedBlock(), plotSQL)) {
					
					u.player.sendMessage(Utils.chat("&cThis point is in another plot!"));
					return;
					
				}
				
				//Passed the checks, start a new selection at the clicked block.
				u.plots.startSelection(e.getClickedBlock());
				u.player.sendMessage(Utils.chat("&aStarted a new selection at " + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getZ()));

				//If the player right clicks then add a point to the existing selection.
			} else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getHand().equals(EquipmentSlot.HAND)) {

				e.setCancelled(true);
				
				//Check if they are making their plot in the same world as their first point.
				if (!u.player.getWorld().equals(u.plots.world())) {
					
					u.player.sendMessage(Utils.chat("&cYou already started a selection in a different world, please create a new selection."));
					
				}
				
				//Check if they are in a world where plots are allowed to be created.
				if (!plotSQL.buildable(e.getClickedBlock().getWorld().getName())) {
					
					u.player.sendMessage(Utils.chat("&cYou can't create plots in this world!"));
					
				}

				//If the player hasn't selected their first point cancel.
				if (u.plots.size() == 0) {
					
					u.player.sendMessage(Utils.chat("&cYou must first begin your selection with left click!"));
					return;
					
				}				
				
				//If the selected point is in an existing plot cancel.
				if (WorldGuardFunctions.inRegion(e.getClickedBlock(), plotSQL)) {
					
					u.player.sendMessage(Utils.chat("&cThis point is in another plot!"));
					return;
					
				}

				u.plots.addPoint(e.getClickedBlock());
				u.player.sendMessage(Utils.chat("&aAdded point at " + e.getClickedBlock().getX() + ", " + e.getClickedBlock().getZ()));

			}


		}

	}
	
	@EventHandler
	public void swapHands(PlayerSwapHandItemsEvent e) {
		
		if (e.getOffHandItem().equals(Main.gui) || e.getOffHandItem().equals(Main.tutorialGui)) {
			e.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void dropItem(PlayerDropItemEvent e) {
		
		if (e.getItemDrop().getItemStack().equals(Main.gui) || e.getItemDrop().getItemStack().equals(Main.tutorialGui)) {
			e.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void moveItem(InventoryMoveItemEvent e) {
		if (e.getItem().equals(Main.gui) || e.getItem().equals(Main.tutorialGui)) {
			e.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void moveItem(InventoryDragEvent e) {
		if (e.getOldCursor().equals(Main.gui) || e.getOldCursor().equals(Main.tutorialGui)) {
			e.setCancelled(true);
		}
		if (e.getCursor().equals(Main.gui) || e.getCursor().equals(Main.tutorialGui)) {
			e.setCancelled(true);
		}
		
	}
}