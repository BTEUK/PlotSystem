package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

import java.util.Objects;

public class EditBook implements Listener {
	
	private final Review review;
	public boolean isEdited;
	
	public EditBook(PlotSystem plotSystem, Review review) {

		//Register listener.
		Bukkit.getServer().getPluginManager().registerEvents(this, plotSystem);

		//Set review.
		this.review = review;

		//Set isEdited to false to indicate the book has not been edited yet.
		isEdited = false;

	}

	@EventHandler
	public void onBookEdit(PlayerEditBookEvent e) {

		//If the player is not the same as the reviewer return.
		if (!review.isSamePlayer(e.getPlayer())) {
			return;
		}
		
		if (e.isSigning()) {
			e.getPlayer().closeInventory();
			review.reviewGui.open(Network.getInstance().getUser(e.getPlayer()));
		}
		
		if (Objects.equals(e.getNewBookMeta().displayName(), Utils.success("Feedback"))) {

			//Save editing of book.
			review.bookMeta = e.getNewBookMeta();
			review.book.setItemMeta(review.bookMeta);

			//Set isEdited to true to indicate the book has been edited.
			isEdited = true;

		}

	}
	
	public void unregister() {
		PlayerEditBookEvent.getHandlerList().unregister(this);
	}
}
