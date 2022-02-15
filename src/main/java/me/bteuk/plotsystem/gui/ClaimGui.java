package me.bteuk.plotsystem.gui;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.PlotValues;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ClaimGui {

    public static Inventory inv;
    public static Component inventory_name;
    public static int inv_rows = 3 * 9;

    public static void initialize() {

        inventory_name = Component.text("Claim Plot", NamedTextColor.AQUA, TextDecoration.BOLD);

        inv = Bukkit.createInventory(null, inv_rows);

    }

    public static Inventory Gui(User u, PlotSQL plotSQL) {

        Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

        inv.clear();

        Utils.createItem(inv, PlotValues.sizeMaterial(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + u.inPlot + ";")),
                1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Size",
                Utils.chat("&f" + PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + u.inPlot + ";"))));

        Utils.createItem(inv, PlotValues.difficultyMaterial(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + u.inPlot + ";")),
                1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Difficulty",
                Utils.chat("&f" + PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + u.inPlot + ";"))));

        Utils.createItem(inv, Material.ENDER_EYE, 1, 13, ChatColor.AQUA + "" + ChatColor.BOLD + "View Plot in Google Maps",
                Utils.chat("&fClick to open a link to this plot in google maps."));

        Utils.createItem(inv, Material.SPRUCE_BOAT, 1, 15, ChatColor.AQUA + "" + ChatColor.BOLD + "Claim Plot",
                Utils.chat("&fClick to claim the plot and start building."));

        toReturn.setContents(inv.getContents());
        return toReturn;
    }

    public static void clicked(User u, ItemStack clicked, PlotSQL plotSQL) {

        Player p = u.player;

        if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "View Plot in Google Maps")) {

            p.closeInventory();

            TextComponent message = new TextComponent("Click here to open the plot in Google Maps");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "insert link here"));

            p.sendMessage(message);
            return;

        } else if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Claim Plot")) {

            p.closeInventory();

            //If the plot status can be updated, add the player as plot owner.
            if (plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + u.inPlot + ";")) {

                //If the player can't be given owner, set the plot status back to unclaimed.
                if (plotSQL.insert("INSERT INTO plot_members(id,uuid,is_owner,last_enter) VALUES(" + u.inPlot + ", " + u.uuid + ", " + 1 + ", " + Time.currentTime() + ";")) {

                    //Add player to worldguard region.
                    if (WorldGuardFunctions.addMember(u.inPlot, u.uuid, u.world)) {

                        p.sendMessage(Utils.chat("&aSuccessfully claimed plot &b" + u.inPlot + "&c, good luck building."));
                        Bukkit.getLogger().info("Plot " + u.inPlot + " successfully claimed by " + u.name);

                    } else {

                        p.sendMessage(Utils.chat("&cAn error occurred while claiming the plot."));
                        Bukkit.getLogger().warning("Plot " + u.inPlot + " was claimed but they were not added to the worldguard region.");

                    }

                } else {

                    p.sendMessage(Utils.chat("&cAn error occurred while claiming the plot."));
                    Bukkit.getLogger().warning("Plot owner insert failed for plot " + u.inPlot);

                    //Attempt to set plot back to unclaimed
                    if (plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + u.inPlot + ";")) {

                        Bukkit.getLogger().warning("Plot " + u.inPlot + " has been set back to unclaimed.");

                    } else {

                        Bukkit.getLogger().severe("Plot " + u.inPlot + " is set to claimed but has no owner!");

                    }
                }

            } else {

                p.sendMessage(Utils.chat("&cAn error occurred while claiming the plot."));
                Bukkit.getLogger().warning("Status could not be changed to claimed for plot " + u.inPlot);

            }
        }
    }
}
