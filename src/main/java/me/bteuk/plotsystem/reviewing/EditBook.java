package me.bteuk.plotsystem.reviewing;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

import me.bteuk.plotsystem.Main;

public class EditBook implements Listener {
	
	Review review;
	
	public EditBook(Main main, Review review) {

		Bukkit.getServer().getPluginManager().registerEvents(this, main);
		this.review = review;
	}

	@EventHandler
	public void onBookEdit(PlayerEditBookEvent e) {
		
		if (Main.getInstance().getUser(e.getPlayer()).review.plot != review.plot) {
			return;
		}
		
		if (e.isSigning()) {
			e.getPlayer().closeInventory();
			e.getPlayer().openInventory(ReviewGui.GUI(Main.getInstance().getUser(e.getPlayer())));
		}
		
		if (e.getNewBookMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Feedback")) {
			review.bookMeta = e.getNewBookMeta();
		} else {
			return;
		}
		
	}
	
	public void unregister() {
		PlayerEditBookEvent.getHandlerList().unregister(this);
	}
}
