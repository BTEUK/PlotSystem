package me.bteuk.plotsystem.reviewing;

import me.bteuk.plotsystem.utils.User;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.bteuk.plotsystem.PlotSystem;

public class Review {

    private final ItemStack[] inventory;

    //User instance.
    private final User u;

    //Review Gui and Listener.
    public ReviewGui reviewGui;
    private final ReviewHotbar hotbarListener;

    //Accept Gui and accept data.
    public AcceptGui acceptGui;

    //Previous feedback Gui.
    public PreviousFeedbackGui previousFeedbackGui;

    //Plot id.
    public final int plot;

    //Feedback book
    public ItemStack book;
    public BookMeta bookMeta;
    public EditBook editBook;

    public Review(int plot, User u) {

        this.u = u;
        this.plot = plot;

        //Save the users hotbar to revert to after reviewing.
        //Then clear their inventory and set it up for reviewing.
        inventory = u.player.getInventory().getContents();
        u.player.getInventory().clear();

        //Set review gui.
        reviewGui = new ReviewGui(u, plot);

        //Create listener for review gui button in slot 1 of hotbar.
        hotbarListener = new ReviewHotbar(PlotSystem.getInstance(), u);

        //Feedback book details.
        book = new ItemStack(Material.WRITABLE_BOOK);
        bookMeta = (BookMeta) book.getItemMeta();
        //noinspection deprecation
        bookMeta.setDisplayName(ChatColor.GREEN + "Feedback");
        book.setItemMeta(bookMeta);
        editBook = new EditBook(PlotSystem.getInstance(), this);

    }

    public void closeReview() {

        //Unregister Listeners
        hotbarListener.unregister();
        editBook.unregister();

        //Remove any existing guis.
        if (reviewGui != null) {
            reviewGui.delete();
        }
        if (acceptGui != null) {
            acceptGui.delete();
        }
        if (previousFeedbackGui != null) {
            previousFeedbackGui.delete();
        }

        //Convert inventory back to how it was pre-review.
        u.player.getInventory().setContents(inventory);

    }
}
