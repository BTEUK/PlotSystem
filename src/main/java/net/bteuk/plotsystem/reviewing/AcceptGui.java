package net.bteuk.plotsystem.reviewing;

import com.sk89q.worldedit.math.BlockVector2;
import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.lib.dto.ChatMessage;
import net.bteuk.network.lib.dto.DiscordDirectMessage;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.Role;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.PlotStatus;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import net.bteuk.plotsystem.exceptions.RegionNotFoundException;
import net.bteuk.plotsystem.utils.PlotHelper;
import net.bteuk.plotsystem.utils.User;
import net.bteuk.plotsystem.utils.plugins.WorldEditor;
import net.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.bteuk.plotsystem.PlotSystem.LOGGER;

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
                    GlobalSQL globalSQL = Network.getInstance().getGlobalSQL();
                    PlotSQL plotSQL = Network.getInstance().getPlotSQL();

                    //Get plot owner.
                    String plotOwner = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + user.review.plot + " AND is_owner=1;");

                    //Get world of plot.
                    World world = Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + user.review.plot + ";"));

                    if (world == null) {
                        LOGGER.warning("World of plot is null!!!");
                        return;
                    }

                    //Get save world.
                    String save_world = PlotSystem.getInstance().getConfig().getString("save_world");
                    if (save_world == null) {
                        LOGGER.warning("Save world is not set in config!");
                        return;
                    }

                    World saveWorld = Bukkit.getWorld(save_world);

                    //Set bookID to 0 if it has not been edited.
                    int bookID = 0;
                    List<String> pages = new ArrayList<>();

                    //Close inventory.
                    u.player.closeInventory();

                    //Check if there is feedback.
                    if (user.review.editBook.isEdited) {

                        //Get the feedback written in the book.
                        List<Component> book = user.review.bookMeta.pages();
                        //Create new book id.
                        bookID = 1 + plotSQL.getInt("SELECT id FROM book_data ORDER BY id DESC;");

                        //Iterate through all pages and store them in database.
                        int i = 1;

                        for (Component text : book) {
                            String page = PlainTextComponentSerializer.plainText().serialize(text);
                            pages.add(page);
                            if (!(plotSQL.update("INSERT INTO book_data(id,page,contents) VALUES(" + bookID + "," + i + ",'" + page.replace("'", "\\'") + "');"))) {
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

                        LOGGER.severe("An error occurred while inserting to accept_data.");

                    }

                    //Send message to plot owner.
                    globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + plotOwner +
                            "','&aPlot " + user.review.plot + " has been accepted.');");

                    //Remove plot members.
                    plotSQL.update("DELETE FROM plot_members WHERE id=" + user.review.plot + ";");

                    //Set plot to completed.
                    PlotHelper.updatePlotStatus(user.review.plot, PlotStatus.COMPLETED);

                    //Remove submitted plot entry.
                    plotSQL.update("DELETE FROM plot_submissions WHERE id=" + user.review.plot + ";");

                    //Add points to player.
                    //By referencing network plugin.
                    //TODO Enable this when points are added.
                    //Points.addPoints(plotOwner, points, PointsType.BUILDING_POINTS);

                    //Get negative coordinate transform.
                    int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + world.getName() + "';");
                    int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + world.getName() + "';");

                    List<BlockVector2> copyVector;

                    try {
                        copyVector = WorldGuardFunctions.getPoints(String.valueOf(user.review.plot), world);
                    } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                        u.player.sendMessage(Utils.error("An error occurred in the plot accepting process, please contact an admin."));
                        e.printStackTrace();
                        return;
                    }
                    List<BlockVector2> pasteVector = new ArrayList<>();

                    //Create paste vector by taking the copy vector coordinate and adding the coordinate transform.
                    for (BlockVector2 bv : copyVector) {

                        pasteVector.add(BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform));

                    }

                    //Update the world by copying the build world to the save world.
                    Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getInstance(), () -> {
                        WorldEditor.updateWorld(copyVector, pasteVector, world, saveWorld);

                        LOGGER.info("Plot " + user.review.plot + " successfully saved.");

                        //Remove plot from worldguard.
                        try {
                            WorldGuardFunctions.delete(String.valueOf(user.review.plot), world);
                        } catch (RegionManagerNotFoundException e) {
                            u.player.sendMessage(Utils.error("An error occurred while removing the plot, please contact an admin."));
                            e.printStackTrace();
                            return;
                        }

                        //Send feedback in chat and console.
                        u.player.sendMessage(Utils.success("Plot ")
                                .append(Component.text(user.review.plot, NamedTextColor.DARK_AQUA))
                                .append(Utils.success(" accepted.")));

                        //Get number of submitted plots.
                        int plot_count = plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

                        //Send message to reviewers that a plot has been reviewed.
                        ChatMessage chatMessage = new ChatMessage("reviewer", "server",
                                ChatUtils.success("A plot has been reviewed, there " + (plot_count == 1 ? "is" : "are") + " %s submitted plots.", String.valueOf(plot_count))
                        );
                        Network.getInstance().getChat().sendSocketMesage(chatMessage);

                        //Get the plot difficulty and player role.
                        int difficulty = plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.review.plot + ";");
                        String builderRole = Roles.builderRole(plotOwner).join();

                        //Calculate the role the player will be promoted to, if any.
                        String newRole = getNewRole(difficulty, builderRole);

                        //Send a message to the plot owner letting them know their plot has been accepted.
                        String discordMessage = "Plot " + user.review.plot + " has been accepted.";
                        if (newRole != null) {
                            Role role = Roles.getRoles().stream().filter(r -> r.getId().equals(newRole)).findFirst().orElse(null);
                            if (role != null) {
                                discordMessage += "\nYou have been promoted to **" + role.getName() + "**";
                            }
                        }
                        if (!pages.isEmpty()) {
                            discordMessage += "\nFeedback: " + String.join(" ", pages);
                        }
                        DiscordDirectMessage discordDirectMessage = new DiscordDirectMessage(plotOwner, discordMessage);
                        Network.getInstance().getChat().sendSocketMesage(discordDirectMessage);

                        // Add the new role and remove the old one.
                        String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + plotOwner + "';");
                        Roles.alterRole(plotOwner, name, newRole, false, true);
                        Roles.alterRole(plotOwner, name, builderRole, true, false);

                        //Close gui and clear review.
                        //Run it sync.
                        Bukkit.getScheduler().runTask(PlotSystem.getInstance(), () -> user.review.closeReview());
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

    @Nullable
    private static String getNewRole(int difficulty, String role) {
        String newRole = null;
        switch (difficulty) {
            case 1 -> {
                if (role.equals("applicant")) {
                    newRole = "apprentice";
                }
            }
            case 2 -> {
                if (role.equals("applicant") || role.equals("apprentice")) {
                    newRole = "jrbuilder";
                }
            }
            case 3 -> {
                if (role.equals("applicant") || role.equals("apprentice") || role.equals("jrbuilder")) {
                    newRole = "builder";
                }
            }
        }
        return newRole;
    }

    public double accuracyMultiplier() {

        return (1 + (accuracy - 3) * PlotSystem.getInstance().getConfig().getDouble("accuracy_multiplier"));

    }

    public double qualityMultiplier() {

        return (1 + (quality - 3) * PlotSystem.getInstance().getConfig().getDouble("quality_multiplier"));

    }
}
