package me.bteuk.plotsystem.gui;

import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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

        Utils.createItem(inv, u.selectionTool.sizeMaterial(), 1, 12, Utils.chat("&b&l" + u.selectionTool.sizeName()),
                Utils.chat("&fClick to cycle through different sizes."));

        Utils.createItem(inv, u.selectionTool.difficultyMaterial(), 1, 12, Utils.chat("&b&l" + u.selectionTool.difficultyName()),
                Utils.chat("&fClick to cycle through different difficulties."));

        toReturn.setContents(inv.getContents());
        return toReturn;

    }

    public static void clicked(User u, int slot, ItemStack clicked, Inventory inv, NavigationSQL navigationSQL, PlotSQL plotSQL) {

        if (clicked.getItemMeta().getLore().equals(Utils.chat("&fClick to cycle through different sizes."))) {

            //Change the size by 1.
            if (u.selectionTool.size == 3) {

                u.selectionTool.size = 1;

            } else {

                u.selectionTool.size ++;

            }

            //Update the inventory.
            u.player.getInventory().setContents(CreatePlotGui.Gui(u).getContents());
            u.player.updateInventory();

        } else if (clicked.getItemMeta().getLore().equals(Utils.chat("&fClick to cycle through different sizes."))) {

            //Change the difficulty by 1.
            if (u.selectionTool.difficulty == 3) {

                u.selectionTool.difficulty = 1;

            } else {

                u.selectionTool.difficulty ++;

            }

            //Update the inventory.
            u.player.getInventory().setContents(CreatePlotGui.Gui(u).getContents());
            u.player.updateInventory();
        } else if (clicked.getItemMeta().getLocalizedName().equals(Utils.chat("&b&lCreate Plot"))) {

            //Create the plot.
            u.selectionTool.createPlot();
            u.player.closeInventory();

        }

        return;


    }
}
