package me.bteuk.plotsystem.gui;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CreatePlotGui {

    public static UniqueGui createPlotGui(User user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Create Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Choose plot size.
        gui.setItem(12, me.bteuk.network.utils.Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&l" + user.selectionTool.sizeName()),
                        Utils.chat("&fClick to cycle through different sizes.")),
                u ->

                {

                    User eUser = Main.getInstance().getUser(u.player);

                    //Change the size by 1.
                    if (eUser.selectionTool.size == 3) {

                        eUser.selectionTool.size = 1;

                    } else {

                        eUser.selectionTool.size++;

                    }

                    //Update the inventory.
                    u.uniqueGui.delete();
                    u.uniqueGui = CreatePlotGui.createPlotGui(eUser);
                    u.player.getInventory().setContents(u.uniqueGui.getInventory().getContents());
                    Gui.openInventories.put(u.player.getUniqueId(), u.uniqueGui.getUuid());

                });

        //Choose plot difficulty.
        gui.setItem(14, me.bteuk.network.utils.Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&l" + user.selectionTool.difficultyName()),
                        Utils.chat("&fClick to cycle through different difficulties.")),
                u ->

                {

                    User eUser = Main.getInstance().getUser(u.player);

                    //Change the difficulty by 1.
                    if (eUser.selectionTool.difficulty == 3) {

                        eUser.selectionTool.difficulty = 1;

                    } else {

                        eUser.selectionTool.difficulty++;

                    }

                    //Update the inventory.
                    u.uniqueGui.delete();
                    u.uniqueGui = CreatePlotGui.createPlotGui(eUser);
                    u.player.getInventory().setContents(u.uniqueGui.getInventory().getContents());
                    Gui.openInventories.put(u.player.getUniqueId(), u.uniqueGui.getUuid());

                });

        //Create plot.
        gui.setItem(12, me.bteuk.network.utils.Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&l Create Plot"),
                        Utils.chat("&fClick create a new plot with the settings selected.")),
                u ->

                {

                    User eUser = Main.getInstance().getUser(u.player);

                    //Update the inventory.
                    u.player.closeInventory();
                    u.uniqueGui.delete();
                    u.uniqueGui = null;

                    //Create plot.
                    eUser.selectionTool.createPlot();

                });

        return gui;

    }
}
