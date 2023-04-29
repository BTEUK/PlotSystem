package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

import java.util.List;

public class ReviewGui extends Gui {

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    private final User user;

    private final String plotOwner;
    private final World world;

    private final int plotID;

    public ReviewGui(User user, int plotID) {

        super(27, Component.text("Review Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        globalSQL = PlotSystem.getInstance().globalSQL;
        plotSQL = PlotSystem.getInstance().plotSQL;

        this.plotID = plotID;

        //Get plot owner.
        plotOwner = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;");

        //Get world of plot.
        world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";"));

        createGui();

    }

    private void createGui() {

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Plot Info"),
                Utils.line("Plot ID: " + plotID),
                Utils.line("Plot Owner: " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotOwner + "';"))));

        setItem(12, Utils.createItem(Material.GRASS_BLOCK, 1,
                        Utils.title("Before View"),
                        Utils.line("Teleport to the plot before it was claimed.")),
                u -> {

                    //Teleport to plot in original state.
                    u.player.closeInventory();

                    Location l = WorldGuardFunctions.getBeforeLocation(String.valueOf(user.review.plot), world);

                    if (l == null) {
                        PlotSystem.getInstance().getLogger().warning("Could not find before view of plot " + user.review.plot);
                        return;
                    }

                    u.player.teleport(l);

                });

        setItem(14, Utils.createItem(Material.STONE_BRICKS, 1,
                        Utils.title("Current View"),
                        Utils.line("Teleport to the current view of the plot.")),
                u -> {

                    //Teleport to plot in current state.
                    u.player.closeInventory();

                    Location l = WorldGuardFunctions.getCurrentLocation(String.valueOf(user.review.plot), world);

                    if (l == null) {
                        PlotSystem.getInstance().getLogger().warning("Could not find current view of plot " + user.review.plot);
                        return;
                    }

                    u.player.teleport(l);

                });

        setItem(10, Utils.createItem(Material.LIME_CONCRETE, 1,
                        Utils.title("Accept Plot"),
                        Utils.line("Opens the accept gui.")),
                u -> {

                    //Open accept gui, create a new one if it is null.
                    if (user.review.acceptGui == null) {

                        user.review.acceptGui = new AcceptGui(user);

                    }

                    //Open accept gui.
                    u.player.closeInventory();
                    user.review.acceptGui.open(u);

                });

        setItem(16, Utils.createItem(Material.RED_CONCRETE, 1,
                        Utils.title("Deny Plot"),
                        Utils.line("Deny the plot and return it to the plot owner.")),
                u -> {

                    //Close inventory.
                    u.player.closeInventory();

                    //Check if the feedback book has been edited.
                    if (!user.review.editBook.isEdited) {

                        u.player.sendMessage(Utils.error("You must provide feedback to deny the plot."));
                        return;

                    }

                    //Get the feedback written in the book.
                    //noinspection deprecation
                    List<String> book = user.review.bookMeta.getPages();
                    //Create new book id.
                    int bookID = 1 + plotSQL.getInt("SELECT id FROM book_data ORDER BY id DESC;");

                    //Iterate through all pages and store them in database.
                    int i = 1;

                    for (String text : book) {
                        //Add escape characters to '
                        if (!(plotSQL.update("INSERT INTO book_data(id,page,contents) VALUES(" + bookID + "," + i + ",'" + text.replace("'", "\\'") + "');"))) {
                            u.player.sendMessage(Utils.error("An error occurred, please notify an admin."));
                            return;
                        }
                        i++;
                    }

                    //Update deny data.
                    if (plotSQL.update("INSERT INTO deny_data(id,uuid,reviewer,book_id,attempt,deny_time) VALUES(" + user.review.plot + ",'" +
                            plotOwner + "','" + u.player.getUniqueId() + "'," + bookID + "," +
                            (1 + plotSQL.getInt("SELECT attempt FROM deny_data WHERE id=" + user.review.plot + " AND uuid='" + plotOwner + "';")) +
                            "," + Time.currentTime() + ");")) {

                        //Send message to plot owner.
                        globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + plotOwner +
                                "','&cPlot " + user.review.plot + " has been denied, feedback has been provided in the plot menu.');");


                        //Set status of plot back to claimed.
                        plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + user.review.plot + ";");

                        //Remove submitted plot entry.
                        PlotSystem.getInstance().plotSQL.update("DELETE FROM plot_submissions WHERE id=" + user.review.plot + ";");

                        //Update last visit time, to prevent inactivity removal of plot.
                        plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + user.review.plot + ";");

                        //Set status of plot back to claimed.
                        plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + user.review.plot + ";");

                        //Remove the reviewer from the plot.
                        WorldGuardFunctions.removeMember(String.valueOf(user.review.plot), u.player.getUniqueId().toString(), world);

                        //Send feedback.
                        u.player.sendMessage(Utils.success("Plot &3" + user.review.plot + " &ahas been denied."));

                        //Get number of submitted plots.
                        int plot_count = PlotSystem.getInstance().plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

                        //Send message to reviewers that a plot has been reviewed.
                        if (plot_count == 1) {
                            Network.getInstance().chat.broadcastMessage(Utils.success("A plot has been reviewed, there is ")
                                    .append(Component.text(1, NamedTextColor.DARK_AQUA))
                                    .append(Utils.success(" submitted plot.")), "uknet:reviewer");
                        } else {
                            Network.getInstance().chat.broadcastMessage(Utils.success("A plot has been reviewed, there are ")
                                    .append(Component.text(plot_count, NamedTextColor.DARK_AQUA))
                                    .append(Utils.success(" submitted plots.")), "uknet:reviewer");
                        }

                        //Close review.
                        u.player.closeInventory();
                        user.review.closeReview();
                        user.review = null;

                    } else {

                        u.player.sendMessage(Utils.error("An error occurred, please notify an admin."));

                    }
                });

        //View previous feedback, if it exists.
        if (plotSQL.hasRow("SELECT id FROM deny_data WHERE uuid='" + plotOwner + "' AND id=" + plotID + ";")) {

            setItem(18, Utils.createItem(Material.LECTERN, 1,
                            Utils.title("Previous Feedback"),
                            Utils.line("Click to review previous"),
                            Utils.line("feedback this player received"),
                            Utils.line("while building this plot.")),
                    u -> {

                        //Open the previous feedback menu.
                        if (user.review.previousFeedbackGui == null) {
                            user.review.previousFeedbackGui = new PreviousFeedbackGui(plotID, user);
                        }

                        u.player.closeInventory();
                        user.review.previousFeedbackGui.open(u);

                    });
        }

        //Cancel review.
        setItem(26, Utils.createItem(Material.BARRIER, 1,
                        Utils.title("Cancel Review"),
                        Utils.line("Stop reviewing this plot.")),
                u -> {

                    //Remove the reviewer from the plot.
                    WorldGuardFunctions.removeMember(String.valueOf(user.review.plot), u.player.getUniqueId().toString(), world);

                    //Set the plot back to submitted.
                    plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + user.review.plot + ";");

                    //Send feedback.
                    u.player.sendMessage(Utils.success("Cancelled reviewing of plot ")
                            .append(Component.text(user.review.plot, NamedTextColor.DARK_AQUA)));

                    //Close review.
                    u.player.closeInventory();
                    user.review.closeReview();
                    user.review = null;

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
