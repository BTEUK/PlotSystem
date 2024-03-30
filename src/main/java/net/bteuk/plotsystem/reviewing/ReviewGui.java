package net.bteuk.plotsystem.reviewing;

import com.sk89q.worldedit.math.BlockVector2;
import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Utils;
import net.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import net.bteuk.plotsystem.exceptions.RegionNotFoundException;
import net.bteuk.plotsystem.exceptions.WorldNotFoundException;
import net.bteuk.plotsystem.PlotSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import net.bteuk.plotsystem.utils.User;
import net.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

import java.util.List;

import static net.bteuk.plotsystem.utils.PlotValues.difficultyMaterial;

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

        globalSQL = Network.getInstance().getGlobalSQL();
        plotSQL = Network.getInstance().getPlotSQL();

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

                    try {
                        Location l = WorldGuardFunctions.getBeforeLocation(String.valueOf(user.review.plot), world);
                        u.player.teleport(l);
                    } catch (RegionManagerNotFoundException | RegionNotFoundException | WorldNotFoundException e) {
                        u.player.sendMessage(Utils.error("Unable to teleport you to the before view of this plot, please contact an admin."));
                        e.printStackTrace();
                    }

                    //Try to create the outline of the before view.
                    try {

                        //Get outlines of the plot.
                        List<BlockVector2> vector = WorldGuardFunctions.getPointsTransformedToSaveWorld(String.valueOf(user.review.plot), world);

                        //Get the plot difficulty.
                        int difficulty = plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";");

                        //Draw the outline.
                        PlotSystem.getInstance().getOutlines().addOutline(u.player, vector, difficultyMaterial(difficulty).createBlockData());

                    } catch (RegionNotFoundException | RegionManagerNotFoundException e) {

                        u.player.sendMessage(Component.text("Outline could not be drawn in save world, please contact an admin!", NamedTextColor.DARK_RED));
                        e.printStackTrace();

                    }

                });

        setItem(14, Utils.createItem(Material.STONE_BRICKS, 1,
                        Utils.title("Current View"),
                        Utils.line("Teleport to the current view of the plot.")),
                u -> {

                    //Teleport to plot in current state.
                    u.player.closeInventory();

                    try {
                        Location l = WorldGuardFunctions.getCurrentLocation(String.valueOf(user.review.plot), world);
                        u.player.teleport(l);
                    } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                        u.player.sendMessage(Utils.error("Unable to teleport you to the this plot, please contact an admin."));
                        e.printStackTrace();
                    }

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
                    List<Component> book = user.review.bookMeta.pages();
                    //Create new book id.
                    int bookID = 1 + plotSQL.getInt("SELECT id FROM book_data ORDER BY id DESC;");

                    //Iterate through all pages and store them in database.
                    int i = 1;

                    for (Component text : book) {
                        //Add escape characters to '
                        if (!(plotSQL.update("INSERT INTO book_data(id,page,contents) VALUES(" + bookID + "," + i + ",'" + PlainTextComponentSerializer.plainText().serialize(text).replace("'", "\\'") + "');"))) {
                            u.player.sendMessage(Utils.error("An error occurred, please notify an admin."));
                            return;
                        }
                        i++;
                    }

                    //Update deny data.
                    if (plotSQL.update("INSERT INTO deny_data(id,uuid,reviewer,book_id,attempt,deny_time) VALUES(" + user.review.plot + ",'" +
                            plotOwner + "','" + u.player.getUniqueId() + "'," + bookID + "," +
                            (1 + plotSQL.getInt("SELECT COUNT(attempt) FROM deny_data WHERE id=" + user.review.plot + " AND uuid='" + plotOwner + "';")) +
                            "," + Time.currentTime() + ");")) {

                        //Send message to plot owner.
                        globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + plotOwner +
                                "','&cPlot " + user.review.plot + " has been denied, feedback has been provided in the plot menu.');");


                        //Set status of plot back to claimed.
                        plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + user.review.plot + ";");

                        //Remove submitted plot entry.
                        plotSQL.update("DELETE FROM plot_submissions WHERE id=" + user.review.plot + ";");

                        //Update last visit time, to prevent inactivity removal of plot.
                        plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + user.review.plot + ";");

                        //Set status of plot back to claimed.
                        plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + user.review.plot + ";");

                        //Remove the reviewer from the plot.
                        try {
                            WorldGuardFunctions.removeMember(String.valueOf(user.review.plot), u.player.getUniqueId().toString(), world);
                        } catch (RegionNotFoundException | RegionManagerNotFoundException e) {

                            u.player.sendMessage(Utils.error("Unable to remove you from the plot, please notify an admin."));
                            e.printStackTrace();

                        }

                        //Send feedback.
                        u.player.sendMessage(Utils.success("Plot ")
                                .append(Component.text(user.review.plot, NamedTextColor.DARK_AQUA))
                                .append(Utils.success(" has been denied.")));

                        //Get number of submitted plots.
                        int plot_count = plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

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

                        //Send a message to the plot owner letting them know their plot has been denied.
                        //Compose the message to send, it is comma-separated.
                        Network.getInstance().chat.broadcastMessage(Component.text(plotOwner + "," + "denied" + "," + user.review.plot), "uknet:discord_dm");

                        //Close review.
                        u.player.closeInventory();
                        user.review.closeReview();

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
                    try {
                        WorldGuardFunctions.removeMember(String.valueOf(user.review.plot), u.player.getUniqueId().toString(), world);
                    } catch (RegionNotFoundException | RegionManagerNotFoundException e) {

                        u.player.sendMessage(Utils.error("Unable to remove you from the plot, please notify an admin."));
                        e.printStackTrace();

                    }


                    //Set the plot back to submitted.
                    plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + user.review.plot + ";");

                    //Send feedback.
                    u.player.sendMessage(Utils.success("Cancelled reviewing of plot ")
                            .append(Component.text(user.review.plot, NamedTextColor.DARK_AQUA)));

                    //Close review.
                    u.player.closeInventory();
                    user.review.closeReview();

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
