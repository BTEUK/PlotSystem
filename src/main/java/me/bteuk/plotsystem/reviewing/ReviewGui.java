package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

import java.util.ArrayList;
import java.util.List;

public class ReviewGui {

    public static UniqueGui createReviewGui(User user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Review Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        GlobalSQL globalSQL = PlotSystem.getInstance().globalSQL;
        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

        //Get plot owner.
        String plotOwner = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + user.review.plot + " AND is_owner=1;");

        //Get world of plot.
        World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + user.review.plot + ";"));

        gui.setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.chat("&b&lPlot Info"),
                Utils.chat("&fPlot ID: " + user.review.plot),
                Utils.chat("&fPlot Owner: " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotOwner + " ;"))));

        gui.setItem(12, Utils.createItem(Material.GRASS_BLOCK, 1,
                        Utils.chat("&b&lBefore View"),
                        Utils.chat("&fTeleport to the plot before it was claimed.")),
                u -> {

                    //Teleport to plot in original state.
                    u.player.teleport(WorldGuardFunctions.getBeforeLocation(user.review.plot, world));

                });

        gui.setItem(14, Utils.createItem(Material.STONE_BRICKS, 1,
                        Utils.chat("&b&lCurrent View"),
                        Utils.chat("&fTeleport to the current view of the plot.")),
                u -> {

                    //Teleport to plot in current state.
                    u.player.teleport(WorldGuardFunctions.getCurrentLocation(user.review.plot, world));

                });

        gui.setItem(10, Utils.createItem(Material.LIME_CONCRETE, 1,
                        Utils.chat("&b&lAccept Plot"),
                        Utils.chat("&fOpens the accept gui.")),
                u -> {

                    //Open accept gui, create a new one if it is null.
                    if (user.review.acceptGui == null) {

                        user.review.acceptGui = AcceptGui.createAcceptGui(user);

                    }

                    //Open accept gui.
                    u.player.closeInventory();
                    user.review.acceptGui.open(u);

                });

        gui.setItem(16, Utils.createItem(Material.RED_CONCRETE, 1,
                        Utils.chat("&b&lDeny Plot"),
                        Utils.chat("&fDeny the plot and return it to the plot owner.")),
                u -> {

                    //Close inventory.
                    u.player.closeInventory();

                    //Check if the feedback book has been edited.
                    if (!user.review.editBook.isEdited) {

                        u.player.sendMessage(Utils.chat("&cYou must provide feedback to deny the plot."));
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
                        if (!(plotSQL.update("INSERT INTO book_data(id,page,contents) VALUES(" + bookID + "," + i + ",'" + text + "');"))) {
                            u.player.sendMessage(Utils.chat("&cAn error occured, please notify an admin."));
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

                        //Update last visit time, to prevent inactivity removal of plot.
                        plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + user.review.plot + ";");

                        //Remove the reviewer from the plot.
                        WorldGuardFunctions.removeMember(user.review.plot, u.player.getUniqueId().toString(), world);

                        //Send feedback.
                        u.player.sendMessage(Utils.chat("&aPlot " + user.review.plot + " has been denied."));

                        //If another plot is submitted tell the reviewer.
                        int submittedPlots = 0;
                        if (plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                            //Get arraylist of submitted plots.
                            ArrayList<Integer> nPlots = plotSQL.getIntList("SELECT id FROM plot_data WHERE status='submitted';");

                            //Iterate through all plots.
                            for (int nPlot : nPlots) {

                                //If you are not owner or member of the plot select it for the next review.
                                if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE uuid='" + u.player.getUniqueId() + "' AND id=" + nPlot + ";")) {

                                    submittedPlots++;

                                }
                            }

                            if (submittedPlots == 1) {
                                u.player.sendMessage(Utils.chat("&aThere is 1 plot available for review."));
                            } else {
                                u.player.sendMessage(Utils.chat("&aThere are " + submittedPlots + " plots available for review."));
                            }

                        } else {

                            u.player.sendMessage(Utils.chat("&aAll plots have been reviewed."));

                        }

                        //Close review.
                        u.player.closeInventory();
                        user.review.closeReview();
                        user.review = null;

                    } else {

                        u.player.sendMessage(Utils.chat("&cAn error occured, please notify an admin."));

                    }
                });

        //Cancel review.
        gui.setItem(22, Utils.createItem(Material.BARRIER, 1,
                        Utils.chat("&b&lCancel Review"),
                        Utils.chat("&fStop reviewing this plot.")),
                u -> {

                    //Remove the reviewer from the plot.
                    WorldGuardFunctions.removeMember(user.review.plot, u.player.getUniqueId().toString(), world);

                    //Set the plot back to submitted.
                    plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + user.review.plot + ";");

                    //Close review.
                    u.player.closeInventory();
                    user.review.closeReview();
                    user.review = null;

                    //Send feedback.
                    u.player.sendMessage(Utils.chat("&cCancelled reviewing of plot " + user.review.plot));

                });

        return gui;

    }
}
