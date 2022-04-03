package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.utils.User;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.bteuk.plotsystem.PlotSystem;

public class Review {

	private ItemStack[] inventory;

	private UniqueGui reviewGui;
	private ReviewHotbar hotbarListener;

	public int plot;
	
	//Feedback book
	public ItemStack book;
	public BookMeta bookMeta;
	public EditBook editBook;
	
	public Review(int plot, User u) {
		
		this.plot = plot;

		//Save the users hotbar to revert to after reviewing.
		//Then clear their inventory and set it up for reviewing.
		inventory = u.player.getInventory().getContents();
		u.player.getInventory().clear();

		//Set review gui.
		reviewGui = ReviewGui.createReviewGui(u);

		//Create listener for review gui button in slot 1 of hotbar.
		hotbarListener = new ReviewHotbar(PlotSystem.getInstance(), this);

		//Feedback book details.
		book = new ItemStack(Material.WRITABLE_BOOK);
		bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setDisplayName(ChatColor.GREEN + "Feedback");
		editBook = new EditBook(PlotSystem.getInstance(), this);
		
	}

}
