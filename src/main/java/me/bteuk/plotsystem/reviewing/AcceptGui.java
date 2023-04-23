package me.bteuk.plotsystem.reviewing;

import java.util.ArrayList;
import java.util.List;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Roles;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WorldEditor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import com.sk89q.worldedit.math.BlockVector2;

import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

public class AcceptGui extends Gui {

    //Reviewing values.
    private int accuracy;
    private int quality;

    private final User user;

    public AcceptGui(User user) {

        super(54, Component.text("Review Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        //Set default values;
        accuracy = 1;
        quality = 1;

        createGui();

    }

    private void createGui() {

        for (int j = 1; j <= 3; j++) {
            for (int i = 1; i <= 5; i++) {

                if (j == 1) {

                    int finalI = i;

                    //Create accuracy buttons.
                    if (accuracy < i) {

                        setItem((j * 9) + i + 10, Utils.createItem(Material.RED_CONCRETE, 1,
                                        Utils.title("Accuracy: " + i)),

                                u -> {

                                    //Set accuracy and update the gui.
                                    accuracy = finalI;

                                    //Update the gui.
                                    refresh();
                                    u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                                }
                        );
                    } else {

                        setItem((j * 9) + i + 10, Utils.createItem(Material.LIME_CONCRETE, 1,
                                        Utils.title("Accuracy: " + i)),

                                u -> {

                                    //Set accuracy and update the gui.
                                    accuracy = finalI;

                                    //Update the gui.
                                    refresh();
                                    u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                                }
                        );
                    }
                }

                if (j == 3) {

                    int finalI = i;

                    //Create quality buttons.
                    if (quality < i) {

                        setItem((j * 9) + i + 10, Utils.createItem(Material.RED_CONCRETE, 1,
                                        Utils.title("Quality: " + i)),

                                u -> {

                                    //Set quality and update the gui.
                                    quality = finalI;

                                    //Update the gui.
                                    refresh();
                                    u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                                }
                        );

                    } else {

                        setItem((j * 9) + i + 10, Utils.createItem(Material.LIME_CONCRETE, 1,
                                        Utils.title("Quality: " + i)),

                                u -> {

                                    //Set quality and update the gui.
                                    quality = finalI;

                                    //Update the gui.
                                    refresh();
                                    u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                                }
                        );

                    }
                }
            }
        }

        setItem(4, Utils.createItem(Material.EMERALD, 1,
                        Utils.title("Accept Plot"),
                        Utils.line("Click to accept the plot with the current settings.")),

                u -> {

                    //Get globalSQL and plotSQL.
                    GlobalSQL globalSQL = PlotSystem.getInstance().globalSQL;
                    PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

                    //Get plot owner.
                    String plotOwner = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + user.review.plot + " AND is_owner=1;");

                    //Get world of plot.
                    World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + user.review.plot + ";"));

                    if (world == null) {
                        PlotSystem.getInstance().getLogger().warning("World of plot is null!!!");
                        return;
                    }

                    //Get save world.
                    String save_world = PlotSystem.getInstance().getConfig().getString("save_world");
                    if (save_world == null) {
                        PlotSystem.getInstance().getLogger().warning("Save world is not set in config!");
                        return;
                    }

                    World saveWorld = Bukkit.getWorld(save_world);

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
                            if (!(plotSQL.update("INSERT INTO book_data(id,page,contents) VALUES(" + bookID + "," + i + ",'" + text.replace("'", "\\'") + "');"))) {
                                u.player.sendMessage(Utils.error("An error occurred, please notify an admin."));
                                return;
                            }
                            i++;
                        }
                    }

                    //Calculate points.
                    //TODO Enable this when points are added.
                    //int points = (int) Math.round((PlotValues.sizeValue(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + user.review.plot + ";")) +
                    //        PlotValues.difficultyValue(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.review.plot + ";"))) *
                    //        ((accuracyMultiplier()+qualityMultiplier())/2));

                    //Add to accept data.
                    if (!plotSQL.update("INSERT INTO accept_data(id,uuid,reviewer,book_id,accuracy,quality,accept_time) VALUES(" +
                            user.review.plot + ",'" + plotOwner + "','" + u.player.getUniqueId() + "'," + bookID + "," +
                            accuracy + "," + quality + "," + Time.currentTime() + ");")) {

                        PlotSystem.getInstance().getLogger().severe("An error occurred while inserting to accept_data.");

                    }

                    //Send message to plot owner.
                    globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + plotOwner +
                            "','&aPlot " + user.review.plot + " has been accepted.');");

                    //Remove plot members.
                    plotSQL.update("DELETE FROM plot_members WHERE id=" + user.review.plot + ";");

                    //Set plot to completed.
                    plotSQL.update("UPDATE plot_data SET status='completed' WHERE id=" + user.review.plot + ";");

                    //Remove submitted plot entry.
                    PlotSystem.getInstance().plotSQL.update("DELETE FROM plot_submissions WHERE id=" + user.review.plot + ";");

                    //Add points to player.
                    //By referencing network plugin.
                    //TODO Enable this when points are added.
                    //Points.addPoints(plotOwner, points, PointsType.BUILDING_POINTS);

                    //Get negative coordinate transform.
                    int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + world.getName() + "';");
                    int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + world.getName() + "';");

                    List<BlockVector2> copyVector = WorldGuardFunctions.getPoints(String.valueOf(user.review.plot), world);
                    List<BlockVector2> pasteVector = new ArrayList<>();

                    if (copyVector== null) {
                        PlotSystem.getInstance().getLogger().warning("CopyVector is null!!!");
                        return;
                    }

                    //Create paste vector by taking the copy vector coordinate and adding the coordinate transform.
                    for (BlockVector2 bv : copyVector) {

                        pasteVector.add(BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform));

                    }

                    //Update the world by copying the build world to the save world.
                    Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getInstance(), () -> {
                        WorldEditor.updateWorld(copyVector, pasteVector, world, saveWorld);

                        PlotSystem.getInstance().getLogger().info("Plot " + user.review.plot + " successfully saved.");

                        //Remove plot from worldguard.
                        WorldGuardFunctions.delete(String.valueOf(user.review.plot), world);

                        //Send feedback in chat and console.
                        u.player.sendMessage(Utils.success("Plot &3" + user.review.plot + " &aaccepted."));

                        //Get number of submitted plots.
                        int plot_count = PlotSystem.getInstance().plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

                        //Send message to reviewers that a plot has been reviewed.
                        if (plot_count == 1) {
                            Network.getInstance().chat.broadcastMessage(Utils.success("A plot has been reviewed, there is ")
                                    .append(Component.text(1, NamedTextColor.DARK_AQUA))
                                    .append(Utils.success("submitted plot.")), "uknet:reviewer");
                        } else {
                            Network.getInstance().chat.broadcastMessage(Utils.success("A plot has been reviewed, there are ")
                                    .append(Component.text(plot_count, NamedTextColor.DARK_AQUA))
                                    .append(Utils.success("submitted plots.")), "uknet:reviewer");
                        }

                        //Promote plot owner if they should be.
                        int difficulty = plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.review.plot + ";");

                        String role = globalSQL.getString("SELECT builder_role FROM player_data WHERE uuid='" + plotOwner + "';");

                        if (difficulty == 1 && role.equals("default")) {
                            //Promote player to apprentice.
                            Roles.promoteBuilder(plotOwner, "default", "apprentice");
                        } else if (difficulty == 2) {
                            if (role.equals("default")) {
                                //Promote player to jrbuilder.
                                Roles.promoteBuilder(plotOwner, "default", "jrbuilder");
                            } else if (role.equals("apprentice")) {
                                Roles.promoteBuilder(plotOwner, "apprentice", "jrbuilder");
                            }
                        } else if (difficulty == 3) {
                            switch (role) {
                                case "default" -> Roles.promoteBuilder(plotOwner, "default", "builder");
                                case "apprentice" -> Roles.promoteBuilder(plotOwner, "apprentice", "builder");
                                case "jrbuilder" -> Roles.promoteBuilder(plotOwner, "jrbuilder", "builder");
                            }
                        }

                        //Close gui and clear review.
                        user.review.closeReview();
                        user.review = null;

                    });
                }
        );

        setItem(53, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Go back to the review menu.")),

                u -> {

                    //Go back to the review gui.
                    u.player.closeInventory();
                    user.review.reviewGui.open(u);

                }
        );
    }

    public void refresh() {

        clearGui();
        createGui();

    }

    public double accuracyMultiplier() {

        return (1 + (accuracy-3) * PlotSystem.getInstance().getConfig().getDouble("accuracy_multiplier"));

    }

    public double qualityMultiplier() {

        return (1 + (quality-3) * PlotSystem.getInstance().getConfig().getDouble("quality_multiplier"));

    }
}
