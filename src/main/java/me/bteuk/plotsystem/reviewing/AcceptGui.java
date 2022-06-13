package me.bteuk.plotsystem.reviewing;

import java.util.ArrayList;
import java.util.List;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.utils.Points;
import me.bteuk.network.utils.enums.PointsType;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.PlotValues;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

public class AcceptGui {

    public static UniqueGui createAcceptGui(User user) {

        UniqueGui gui = new UniqueGui(54, Component.text("Review Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Set accept for easy access.
        Accept ac = user.review.accept;

        for (int j = 1; j <= 3; j++) {
            for (int i = 1; i <= 5; i++) {

                if (j == 1) {

                    int finalI = i;

                    //Create accuracy buttons.
                    if (ac.accuracy < i) {

                        gui.setItem((j * 9) + i + 1, Utils.createItem(Material.RED_CONCRETE, 1,
                                        Utils.chat("&b&lAccuracy: " + i)),

                                u -> {

                                    //Set accuracy and update the gui.
                                    ac.accuracy = finalI;
                                    user.review.acceptGui.delete();
                                    user.review.acceptGui = AcceptGui.createAcceptGui(user);
                                    user.review.acceptGui.update(u);
                                    u.player.getInventory().setContents(user.review.acceptGui.getInventory().getContents());

                                }
                        );
                    } else {

                        gui.setItem((j * 9) + i + 1, Utils.createItem(Material.GREEN_CONCRETE, 1,
                                        Utils.chat("&b&lAccuracy: " + i)),

                                u -> {

                                    //Set accuracy and update the gui.
                                    ac.accuracy = finalI;
                                    user.review.acceptGui.delete();
                                    user.review.acceptGui = AcceptGui.createAcceptGui(user);
                                    user.review.acceptGui.update(u);
                                    u.player.getInventory().setContents(user.review.acceptGui.getInventory().getContents());

                                }
                        );
                    }
                }

                if (j == 3) {

                    int finalI = i;

                    //Create quality buttons.
                    if (ac.quality < i) {

                        gui.setItem((j * 9) + i + 1, Utils.createItem(Material.RED_CONCRETE, 1,
                                        Utils.chat("&b&lQuality: " + i)),

                                u -> {

                                    //Set quality and update the gui.
                                    ac.quality = finalI;
                                    user.review.acceptGui.delete();
                                    user.review.acceptGui = AcceptGui.createAcceptGui(user);
                                    user.review.acceptGui.update(u);
                                    u.player.getInventory().setContents(user.review.acceptGui.getInventory().getContents());

                                }
                        );

                    } else {

                        gui.setItem((j * 9) + i + 1, Utils.createItem(Material.GREEN_CONCRETE, 1,
                                        Utils.chat("&b&lQuality: " + i)),

                                u -> {

                                    //Set quality and update the gui.
                                    ac.quality = finalI;
                                    user.review.acceptGui.delete();
                                    user.review.acceptGui = AcceptGui.createAcceptGui(user);
                                    user.review.acceptGui.update(u);
                                    u.player.getInventory().setContents(user.review.acceptGui.getInventory().getContents());

                                }
                        );

                    }
                }
            }
        }

        gui.setItem(4, Utils.createItem(Material.EMERALD, 1,
                        Utils.chat("&b&lAccept Plot"),
                        Utils.chat("&fClick to accept the plot with the current settings.")),

                u -> {

                    //Get globalSQL and plotSQL.
                    GlobalSQL globalSQL = PlotSystem.getInstance().globalSQL;
                    PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

                    //Get plot owner.
                    String plotOwner = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + user.review.plot + " AND is_owner=1;");

                    //Get world of plot.
                    World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + user.review.plot + ";"));

                    //Get save world.
                    World saveWorld = Bukkit.getWorld(PlotSystem.getInstance().getConfig().getString("save_world"));

                    //Set bookID to 0 if it has not been edited.
                    int bookID = 0;

                    //Close inventory.
                    u.player.closeInventory();

                    //Check if there is feedback.
                    if (user.review.editBook.isEdited) {

                        //Get the feedback written in the book.
                        //noinspection deprecation
                        List<String> book = user.review.bookMeta.getPages();
                        //Create new book id.
                        bookID = 1 + plotSQL.getInt("SELECT id FROM book_data ORDER BY id DESC;");

                        //Iterate through all pages and store them in database.
                        int i = 1;

                        for (String text : book) {
                            if (!(plotSQL.update("INSERT INTO book_data(id,page,text) VALUES(" + bookID + "," + i + "," + text + ");"))) {
                                u.player.sendMessage(Utils.chat("&cAn error occured, please notify an admin."));
                                return;
                            }
                            i++;
                        }
                    }

                    //Calculate points.
                    int points = (int) Math.round((PlotValues.sizeValue(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + user.review.plot + ";")) +
                            PlotValues.difficultyValue(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.review.plot + ";"))) *
                            ((ac.accuracyMultiplier()+ac.qualityMultiplier())/2));

                    //Add to accept data.
                    if (!plotSQL.update("INSERT INTO accept_data(id,uuid,reviewer,book_id,accuracy,quality,time) VALUES(" +
                            user.review.plot + "," + plotOwner + "," + u.player.getUniqueId() + "," + bookID + "," +
                            user.review.accept.accuracy + "," + user.review.accept.quality + "," + Time.currentTime() + ");")) {

                        PlotSystem.getInstance().getLogger().severe(Utils.chat("&cAn error occured while inserting to accept_data."));

                    }

                    //Send message to plot owner.
                    globalSQL.update("INSERT INTO messages(recipient,message) VALUES(" + plotOwner +
                            ",'&aPlot " + user.review.plot + " has been accepted.');");

                    //Remove plot members.
                    plotSQL.update("DELETE FROM plot_members WHERE id=" + user.review.plot + ";");

                    //Set plot to completed.
                    plotSQL.update("UPDATE plot_data SET status='completed' WHERE id=" + user.review.plot + ";");

                    //Add points to player.
                    //By referencing network plugin.
                    Points.addPoints(plotOwner, points, PointsType.BUILDING_POINTS);

                    //Get negative coordinate transform.
                    int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name=" + world.getName() + ";");
                    int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name=" + world.getName() + ";");

                    List<BlockVector2> copyVector = WorldGuardFunctions.getPoints(user.review.plot, world);
                    List<BlockVector2> pasteVector = new ArrayList<>();

                    //Create paste vector by taking the copy vector coordinate and adding the coordinate transform.
                    for (BlockVector2 bv : copyVector) {

                        pasteVector.add(BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform));

                    }

                    //Update the world by copying the build world to the save world.
                    WorldEditor.updateWorld(copyVector, pasteVector, world, saveWorld);

                    PlotSystem.getInstance().getLogger().info(Utils.chat("&aPlot " + user.review.plot + " successfully saved."));

                    //Remove plot from worldguard.
                    WorldGuardFunctions.deletePlot(user.review.plot, world);

                    //Send feedback in chat and console.
                    u.player.sendMessage(Utils.chat("&aPlot " + user.review.plot + " accepted."));

                    //If another plot is submitted tell the reviewer.
                    int submittedPlots = 0;
                    if (plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                        //Get arraylist of submitted plots.
                        ArrayList<Integer> nPlots = plotSQL.getIntList("SELECT id FROM plot_data WHERE status='submitted';");

                        //Iterate through all plots.
                        for (int nPlot : nPlots) {

                            //If you are not owner or member of the plot select it for the next review.
                            if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE uuid=" + u.player.getUniqueId() + " AND id=" + nPlot + ";")) {

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

                    //Close gui and clear review.
                    user.review.closeReview();
                    user.review = null;

                }
        );

        gui.setItem(53, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fGo back to the review menu.")),

                u -> {

                    //Go back to the review gui and delete the accept gui.
                    u.player.closeInventory();
                    user.review.reviewGui.open(u);

                }
        );

        return gui;

    }
}
