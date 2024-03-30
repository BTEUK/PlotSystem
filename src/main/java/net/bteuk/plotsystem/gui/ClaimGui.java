package net.bteuk.plotsystem.gui;

import net.bteuk.network.gui.Gui;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Utils;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import net.bteuk.plotsystem.exceptions.RegionNotFoundException;
import net.bteuk.plotsystem.utils.PlotValues;
import net.bteuk.plotsystem.utils.User;
import net.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class ClaimGui extends Gui {

    private final User user;

    public ClaimGui(User user) {

        super(27, Component.text("Claim Plot", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    private void createGui() {

        setItem(20, Utils.createItem(PlotValues.sizeMaterial(user.plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + user.inPlot + ";")), 1,
                Utils.title("Plot Size"),
                Utils.line(PlotValues.sizeName(user.plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + user.inPlot + ";")))));

        setItem(24, Utils.createItem(PlotValues.difficultyMaterial(user.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.inPlot + ";")), 1,
                Utils.title("Plot Difficulty"),
                Utils.line(PlotValues.difficultyName(user.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.inPlot + ";")))));

        setItem(22, Utils.createItem(Material.ENDER_EYE, 1,
                        Utils.title("View Plot in Google Maps"),
                        Utils.line("Click to open a link to this plot in google maps.")),
                u ->

                {

                    u.player.closeInventory();

                    //Get corners of the plot.
                    int[][] corners = user.plotSQL.getPlotCorners(user.inPlot);

                    int sumX = 0;
                    int sumZ = 0;

                    //Find the centre.
                    for (int[] corner : corners) {

                        sumX += corner[0];
                        sumZ += corner[1];

                    }

                    double x = sumX / (double) corners.length;
                    double z = sumZ / (double) corners.length;

                    //Subtract the coordinate transform to make the coordinates in the real location.
                    x -= user.plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" +
                            user.plotSQL.getString("SELECT location FROM plot_data WHERE id=" + user.inPlot + ";") + "';");
                    z -= user.plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" +
                            user.plotSQL.getString("SELECT location FROM plot_data WHERE id=" + user.inPlot + ";") + "';");

                    //Convert to irl coordinates.

                    try {

                        final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
                        double[] coords = bteGeneratorSettings.projection().toGeo(x, z);

                        //Generate link to google maps.
                        Component message = Utils.success("Click here to open the plot in Google Maps.");
                        message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/@?api=1&map_action=map&basemap=satellite&zoom=21&center=" + coords[1] + "," + coords[0]));
                        u.player.sendMessage(message);

                    } catch (OutOfProjectionBoundsException e) {
                        e.printStackTrace();
                    }

                });

        setItem(4, Utils.createItem(Material.EMERALD, 1,
                        Utils.title("Claim Plot"),
                        Utils.line("Click to claim the plot and start building.")),
                u ->

                {

                    User eUser = PlotSystem.getInstance().getUser(u.player);
                    u.player.closeInventory();

                    //Check if the plot is not already claimed, since it may happen that the gui is spammed.
                    if (eUser.plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + eUser.inPlot + " AND status='unclaimed';")) {

                        //If the plot status can be updated, add the player as plot owner.
                        if (eUser.plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + eUser.inPlot + ";")) {

                            //If the player can't be given owner, set the plot status back to unclaimed.
                            if (eUser.plotSQL.update("INSERT INTO plot_members(id,uuid,is_owner,last_enter) VALUES(" + eUser.inPlot + ",'" + eUser.uuid + "',1," + Time.currentTime() + ");")) {

                                //Add player to worldguard region.
                                try {
                                    if (WorldGuardFunctions.addMember(String.valueOf(eUser.inPlot), eUser.uuid, eUser.player.getWorld())) {

                                        eUser.player.sendMessage(Utils.success("Successfully claimed plot ")
                                                .append(Component.text(eUser.inPlot, NamedTextColor.DARK_AQUA))
                                                .append(Utils.success(", good luck building.")));
                                        // Send link to plot in Google Maps.
                                        eUser.player.performCommand("ll");
                                        Bukkit.getLogger().info("Plot " + eUser.inPlot + " successfully claimed by " + eUser.name);

                                    } else {

                                        eUser.player.sendMessage(Utils.error("An error occurred while claiming the plot."));
                                        Bukkit.getLogger().warning("Plot " + eUser.inPlot + " was claimed but they were not added to the worldguard region.");

                                    }
                                } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                                    eUser.player.sendMessage(Utils.error("An error occurred while claiming the plot, please notify an admin."));
                                    e.printStackTrace();
                                }

                            } else {

                                eUser.player.sendMessage(Utils.error("An error occurred while claiming the plot."));
                                Bukkit.getLogger().warning("Plot owner insert failed for plot " + eUser.inPlot);

                                //Attempt to set plot back to unclaimed
                                if (eUser.plotSQL.update("UPDATE plot_data SET status='unclaimed' WHERE id=" + eUser.inPlot + ";")) {

                                    Bukkit.getLogger().warning("Plot " + eUser.inPlot + " has been set back to unclaimed.");

                                } else {

                                    Bukkit.getLogger().severe("Plot " + eUser.inPlot + " is set to claimed but has no owner!");

                                }
                            }

                        } else {

                            eUser.player.sendMessage(Utils.error("An error occurred while claiming the plot."));
                            Bukkit.getLogger().warning("Status could not be changed to claimed for plot " + eUser.inPlot);

                        }
                    } else {

                        eUser.player.sendMessage(Utils.error("This plot is already claimed, it could be due to clicking the claim button multiple times."));

                    }

                });
    }

    public void refresh() {

        clearGui();
        createGui();

    }
}
