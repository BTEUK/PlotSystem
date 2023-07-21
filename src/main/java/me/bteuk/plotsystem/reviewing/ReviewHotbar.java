package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import static me.bteuk.plotsystem.PlotSystem.LOGGER;

public class ReviewHotbar implements Listener {

    //PlotSystem instance.
    private final PlotSystem plotSystem;

    //User.
    private final User u;

    //Review gui item.
    private final ItemStack reviewGuiItem;

    //Timer ID.
    private int taskID;

    //Itemstack for slot 1 and 2.
    private ItemStack slot1;
    private ItemStack slot2;

    public ReviewHotbar(PlotSystem plotSystem, User u) {

        //Set plotsystem.
        this.plotSystem = plotSystem;

        //Set user.
        this.u = u;

        //Create review gui item.
        reviewGuiItem = Utils.createItem(Material.EMERALD,1, Utils.title("Review Menu"), Utils.line("Click to open review menu."));

        //Register listeners.
        Bukkit.getServer().getPluginManager().registerEvents(this, plotSystem);

        //Start timer to keep review gui in slot 1.
        timer();

    }

    //If the player clicks on the review gui in their inventory, open the gui.
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getCurrentItem() == null) {
            return;
        }

        User u = PlotSystem.getInstance().getUser((Player) e.getWhoClicked());

        //If item is review gui then open the gui.
        if (e.getCurrentItem().equals(reviewGuiItem)) {
            e.setCancelled(true);

            //If item is not in slot 1, delete it.
            if (e.getSlot() != 0) {
                u.player.getInventory().clear(e.getSlot());
                return;
            }

            u.player.closeInventory();
            NetworkUser user = Network.getInstance().getUser(u.player);
            Bukkit.getScheduler().runTaskLater(plotSystem, () -> u.review.reviewGui.open(user), 1);
        }
    }

    //If the player interacts with the gui without opening their inventory, open the gui.
    @EventHandler
    public void interactEvent(PlayerInteractEvent e) {

        if (e.getPlayer().getOpenInventory().getType() != InventoryType.CRAFTING && e.getPlayer().getOpenInventory().getType() != InventoryType.CREATIVE) {
            return;
        }

        //If item is review gui then open the gui.
        if (e.getPlayer().getInventory().getItemInMainHand().equals(reviewGuiItem)) {
            e.setCancelled(true);
            u.player.closeInventory();
            NetworkUser user = Network.getInstance().getUser(u.player);
            if (u.review != null) {
                u.review.reviewGui.open(user);
            }
        }
    }

    //Timer to keep review gui and feedback book in inventory.
    private void timer() {

        //1 tick timer.
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plotSystem, () -> {

            //Get slot1 and slot2.
            slot1 = u.player.getInventory().getItem(0);
            slot2 = u.player.getInventory().getItem(1);

            //If slot1 is null set to review gui.
            if (slot1 == null) {

                //Set slot1 to gui.
                u.player.getInventory().setItem(0, reviewGuiItem);

                //If slot1 is not review gui set to review gui.
            } else if (!slot1.equals(reviewGuiItem)) {

                //Set slot to gui.
                u.player.getInventory().setItem(0, reviewGuiItem);

            }

            //If slot2 is null set to feedback book.
            if (slot2 == null) {

                //Set slot2 to feedback book.
                u.player.getInventory().setItem(1, u.review.book);

                //If slot2 is not feedback book set to feedback book.
            } else if (!slot2.equals(u.review.book)) {

                //Set slot to gui.
                u.player.getInventory().setItem(1, u.review.book);

            }

        }, 0L, 1L);
    }


    public void unregister() {

        //Stop timer.
        Bukkit.getScheduler().cancelTask(taskID);

        //Unregister listeners.
        PlayerEditBookEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        InventoryMoveItemEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerSwapHandItemsEvent.getHandlerList().unregister(this);

        //Send feedback in the console.
        LOGGER.info("Reset reviewing hotbar and unregistered listeners");

    }

    /*

    The following events are to prevent the gui being moved in the inventory,
    causing duplicate items which are difficult to remove.

     */

    @EventHandler
    public void swapHands(PlayerSwapHandItemsEvent e) {

        if (e.getOffHandItem() == null) {
            return;
        }

        e.setCancelled(cancelEvent(e.getOffHandItem()));

    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent e) {
        e.setCancelled(cancelEvent(e.getItemDrop().getItemStack()));
    }

    @EventHandler
    public void moveItem(InventoryMoveItemEvent e) {
        e.setCancelled(cancelEvent(e.getItem()));
    }

    @EventHandler
    public void dragItem(InventoryDragEvent e) {
        if (cancelEvent(e.getOldCursor())) {
            e.setCancelled(true);
            return;
        }

        if (e.getCursor() != null) {
            if (cancelEvent(e.getCursor())) {
                e.setCancelled(true);
            }
        }
    }

    public boolean cancelEvent(ItemStack item) {

        //Check if review is not null.
        if (u.review != null) {
            return item.equals(reviewGuiItem) || item.equals(u.review.book);
        }

        return false;
    }
}
