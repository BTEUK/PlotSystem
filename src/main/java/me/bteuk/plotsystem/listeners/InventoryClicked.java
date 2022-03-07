package me.bteuk.plotsystem.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.ConfirmCancel;
import me.bteuk.plotsystem.gui.LocationGUI;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.gui.PlotGui;
import me.bteuk.plotsystem.gui.PlotInfo;
import me.bteuk.plotsystem.gui.SwitchServerGUI;
import me.bteuk.plotsystem.reviewing.AcceptGui;
import me.bteuk.plotsystem.reviewing.DenyGui;
import me.bteuk.plotsystem.reviewing.FeedbackGui;
import me.bteuk.plotsystem.reviewing.ReviewGui;
import me.bteuk.plotsystem.utils.User;

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
		 
		String title = e.getView().getTitle();
		
		User u = PlotSystem.getInstance().getUser((Player) e.getWhoClicked());
		if (title.equals(MainGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(MainGui.inventory_name)) {
				MainGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(LocationGUI.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(LocationGUI.inventory_name)) {
				LocationGUI.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(ReviewGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(ReviewGui.inventory_name)) {
				ReviewGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(PlotGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(PlotGui.inventory_name)) {
				PlotGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(DenyGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(DenyGui.inventory_name)) {
				DenyGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(AcceptGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(AcceptGui.inventory_name)) {
				AcceptGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(PlotInfo.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(PlotInfo.inventory_name)) {
				PlotInfo.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(ConfirmCancel.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(ConfirmCancel.inventory_name)) {
				ConfirmCancel.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(SwitchServerGUI.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(SwitchServerGUI.inventory_name)) {
				SwitchServerGUI.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(TutorialGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(TutorialGui.inventory_name)) {
				TutorialGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(FeedbackGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(FeedbackGui.inventory_name)) {
				FeedbackGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(TutorialVideoGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(TutorialVideoGui.inventory_name)) {
				TutorialVideoGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (title.equals(TutorialSelectionGui.inventory_name)) {
			e.setCancelled(true);
			if (e.getCurrentItem() == null){
				return;
			}
			if (title.equals(TutorialSelectionGui.inventory_name)) {
				TutorialSelectionGui.clicked(u, e.getSlot(), e.getCurrentItem(), e.getInventory());
			}
		}
		else if (e.getCurrentItem().equals(PlotSystem.gui)) {
			e.setCancelled(true);
			u.player.closeInventory();
			Bukkit.getScheduler().runTaskLater (instance, () -> u.player.openInventory(MainGui.GUI(u)), 1);
		}
		else if (e.getCurrentItem().equals(PlotSystem.tutorialGui)) {
			e.setCancelled(true);
			u.player.closeInventory();
			u.previousGui = "none";
			Bukkit.getScheduler().runTaskLater (instance, () -> u.player.openInventory(TutorialGui.GUI(u)), 1);
		}
		else {
			
		}
	}

}
