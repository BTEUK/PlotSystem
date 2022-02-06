package me.bteuk.plotsystem.gui.plots;

import me.bteuk.plotsystem.serverconfig.Multiverse;
import me.bteuk.plotsystem.serverconfig.SetupGui;
import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CreatePlotGui {

    public static Inventory inv;
    public static Component inventory_name;
    public static int inv_rows = 3 * 9;

    public static void initialize() {

        inventory_name = Component.text("Create Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD);

        inv = Bukkit.createInventory(null, inv_rows);

    }

    public static Inventory Gui(User u) {

        Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

        inv.clear();

        if (u.plotCreateInfo.stage == 1) {

            //Select server type, plots only, tutorial only, both.
            Utils.createItem(inv, Material.LIME_CONCRETE, 1, 12, ChatColor.AQUA + "" + ChatColor.BOLD + "Plots Only",
                    Utils.chat("&fThis will setup the server to only allow plot worlds."));

            Utils.createItem(inv, Material.LIME_CONCRETE, 1, 14, ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Only",
                    Utils.chat("&fThis will setup the server for the tutorial, but nothing else."));

            Utils.createItem(inv, Material.LIME_CONCRETE, 1, 16, ChatColor.AQUA + "" + ChatColor.BOLD + "Plots and Tutorial",
                    Utils.chat("&fThis will setup the server to have both plots and the tutorial on the same server."));

        } else if (stage == 2) {

            //If the server is has plots then prompts the player with a plot world creation menu, to select the number of plot worlds.
            Utils.createItem(inv, Material.LIME_CONCRETE, 1, 14, ChatColor.AQUA + "" + ChatColor.BOLD + "Create New Plot World",
                    Utils.chat("&fClick here to create a new plot world."));

            Utils.createItem(inv, Material.LIME_CONCRETE, 1, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "Complete Server Setup",
                    Utils.chat("&fClick here to complete the server setup."));

        }

        toReturn.setContents(inv.getContents());
        return toReturn;
    }

    public static void clicked(User u, int slot, ItemStack clicked, Inventory inv, NavigationSQL
            navigationSQL, PlotSQL plotSQL) {

        Player p = u.player;

        if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plots Only")) {

            plots_only = true;
            stage = 2;
            p.getInventory().setContents(SetupGui.inv.getContents());
            p.updateInventory();
            return;

        } else if (clicked.getItemMeta().getLocalizedName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Only")) {

            plots_only = false;
            p.closeInventory();

            if (Multiverse.createVoidWorld("tutorial_world")) {

                plotSQL.addWorld("tutorial_world", "tutorial");
                plotSQL.addServer(true, false);
                p.sendMessage(Utils.chat("&aServer setup complete!"));
                Bukkit.getLogger().info(Utils.chat("&aCreated the tutorial world."));
                Bukkit.getLogger().info(Utils.chat("&aServer setup complete!"));

            } else {

                p.sendMessage(Utils.chat("&cAn error occurred when creating the world, please check the console for further info."));

            }
            return;

        } else if (clicked.getItemMeta().getLocalizedName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plots and Tutorial")) {

            plots_only = false;
            stage = 2;
            p.getInventory().setContents(SetupGui.inv.getContents());
            p.updateInventory();
            return;

        } else if (clicked.getItemMeta().getLocalizedName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Create New Plot World")) {

            plot_worlds++;

            if (Multiverse.createVoidWorld("plot_world_" + plot_worlds)) {

                plotSQL.addWorld("plot_world_" + plot_worlds, "build");
                p.sendMessage(Utils.chat("&aCreated a new plot world, there are now " + plot_worlds + " in total."));
                Bukkit.getLogger().info(Utils.chat("&aCreated a new plot world, there are now " + plot_worlds + " in total."));

            } else {

                p.closeInventory();
                p.sendMessage(Utils.chat("&cAn error occurred when creating the world, please check the console for further info."));

            }
            return;

        } else if (clicked.getItemMeta().getLocalizedName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Complete Server Setup")) {

            p.closeInventory();
            if (Multiverse.createVoidWorld("save_world")) {

                plotSQL.addWorld("save_world", "save");
                Bukkit.getLogger().info(Utils.chat("&aCreated the save world."));

            } else {

                p.sendMessage(Utils.chat("&cAn error occurred when creating the world, please check the console for further info."));
                return;

            }

            if (!plots_only) {

                if (Multiverse.createVoidWorld("tutorial_world")) {

                    plotSQL.addWorld("tutorial_world", "tutorial");
                    plotSQL.addServer(false, false);
                    p.sendMessage(Utils.chat("&aServer setup complete!"));
                    Bukkit.getLogger().info(Utils.chat("&aCreated the tutorial world."));
                    Bukkit.getLogger().info(Utils.chat("&aServer setup complete!"));

                } else {

                    p.sendMessage(Utils.chat("&cAn error occurred when creating the world, please check the console for further info."));
                    return;

                }

            } else {

                plotSQL.addServer(false, true);
                p.sendMessage(Utils.chat("&aServer setup complete!"));
                Bukkit.getLogger().info(Utils.chat("&aServer setup complete!"));

            }

            navigationSQL.addServer();
            p.sendMessage(Utils.chat("&cRestart the server to enable the plot system."));
            Bukkit.getLogger().info(Utils.chat("&cRestart the server to enable the plot system."));
            return;
        }
    }
}
