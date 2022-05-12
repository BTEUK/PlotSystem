package me.bteuk.plotsystem.gui;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.PlotValues;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class ClaimGui {

    public static UniqueGui claimPlotGui(User user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Claim Plot", NamedTextColor.AQUA, TextDecoration.BOLD));

        gui.setItem(12, Utils.createItem(PlotValues.sizeMaterial(user.plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + user.inPlot + ";")), 1,
                Utils.chat("&b&lPlot Size"),
                Utils.chat("&f" + PlotValues.sizeName(user.plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + user.inPlot + ";")))));

        gui.setItem(12, Utils.createItem(PlotValues.difficultyMaterial(user.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.inPlot + ";")), 1,
                Utils.chat("&b&lPlot Difficulty"),
                Utils.chat("&f" + PlotValues.difficultyName(user.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + user.inPlot + ";")))));

        gui.setItem(12, Utils.createItem(Material.ENDER_EYE, 1,
                        Utils.chat("&b&lView Plot in Google Maps"),
                        Utils.chat("&fClick to open a link to this plot in google maps.")),
                u ->

                {

                    u.player.closeInventory();

                    TextComponent message = new TextComponent("Click here to open the plot in Google Maps");
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "insert link here"));

                    u.player.sendMessage(message);

                });

        gui.setItem(12, Utils.createItem(Material.EMERALD, 1,
                        Utils.chat("&b&lClaim Plot"),
                        Utils.chat("&fClick to claim the plot and start building.")),
                u ->

                {

                    User eUser = PlotSystem.getInstance().getUser(u.player);

                    u.player.closeInventory();

                    //If the plot status can be updated, add the player as plot owner.
                    if (eUser.plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + eUser.inPlot + ";")) {

                        //If the player can't be given owner, set the plot status back to unclaimed.
                        if (eUser.plotSQL.update("INSERT INTO plot_members(id,uuid,is_owner,last_enter) VALUES(" + eUser.inPlot + ", " + eUser.uuid + ", " + 1 + ", " + Time.currentTime() + ";")) {

                            //Add player to worldguard region.
                            if (WorldGuardFunctions.addMember(eUser.inPlot, eUser.uuid, eUser.player.getWorld())) {

                                eUser.player.sendMessage(Utils.chat("&aSuccessfully claimed plot &b" + eUser.inPlot + "&c, good luck building."));
                                Bukkit.getLogger().info("Plot " + eUser.inPlot + " successfully claimed by " + eUser.name);

                            } else {

                                eUser.player.sendMessage(Utils.chat("&cAn error occurred while claiming the plot."));
                                Bukkit.getLogger().warning("Plot " + eUser.inPlot + " was claimed but they were not added to the worldguard region.");

                            }

                        } else {

                            eUser.player.sendMessage(Utils.chat("&cAn error occurred while claiming the plot."));
                            Bukkit.getLogger().warning("Plot owner insert failed for plot " + eUser.inPlot);

                            //Attempt to set plot back to unclaimed
                            if (eUser.plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + eUser.inPlot + ";")) {

                                Bukkit.getLogger().warning("Plot " + eUser.inPlot + " has been set back to unclaimed.");

                            } else {

                                Bukkit.getLogger().severe("Plot " + eUser.inPlot + " is set to claimed but has no owner!");

                            }
                        }

                    } else {

                        eUser.player.sendMessage(Utils.chat("&cAn error occurred while claiming the plot."));
                        Bukkit.getLogger().warning("Status could not be changed to claimed for plot " + eUser.inPlot);

                    }

                });

        return gui;

    }
}
