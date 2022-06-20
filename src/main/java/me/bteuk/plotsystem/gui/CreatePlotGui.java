package me.bteuk.plotsystem.gui;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.PlotValues;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class CreatePlotGui {

    //This gui handles the plot creation process, and will allow the user to set the parameters of the plot.
    public static UniqueGui createPlotGui(User user) {

        //Create an empty gui with size and name.
        UniqueGui gui = new UniqueGui(27, Component.text("Create Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Choose plot size.
        gui.setItem(11, Utils.createItem(PlotValues.sizeMaterial(user.selectionTool.size), 1,
                        Utils.chat("&b&l" + PlotValues.sizeName(user.selectionTool.size)),
                        Utils.chat("&fClick to cycle through sizes.")),
                u ->

                {

                    //Get an instance of the plotsystem user.
                    User eUser = PlotSystem.getInstance().getUser(u.player);

                    //Change the size by 1.
                    //If less than 3 (large) increase by 1, else return to 1.
                    if (eUser.selectionTool.size == 3) {

                        eUser.selectionTool.size = 1;

                    } else {

                        eUser.selectionTool.size++;

                    }

                    //Update the gui.
                    u.uniqueGui.update(u, CreatePlotGui.createPlotGui(eUser));

                });

        //Choose plot difficulty.
        gui.setItem(15, Utils.createItem(PlotValues.difficultyMaterial(user.selectionTool.difficulty), 1,
                        Utils.chat("&b&l" + PlotValues.difficultyName(user.selectionTool.difficulty)),
                        Utils.chat("&fClick to cycle through different difficulties.")),
                u ->

                {

                    User eUser = PlotSystem.getInstance().getUser(u.player);

                    //Change the difficulty by 1.
                    //If less than 3 (hard) increase by 1, else return to 1.
                    if (eUser.selectionTool.difficulty == 3) {

                        eUser.selectionTool.difficulty = 1;

                    } else {

                        eUser.selectionTool.difficulty++;

                    }

                    //Update the gui.
                    u.uniqueGui.update(u, CreatePlotGui.createPlotGui(eUser));

                });

        //Create plot.
        gui.setItem(13, Utils.createItem(Material.DIAMOND, 1,
                        Utils.chat("&b&lCreate Plot"),
                        Utils.chat("&fClick create a new plot with the settings selected.")),
                u ->

                {

                    User eUser = PlotSystem.getInstance().getUser(u.player);

                    //Close the inventory and delete the gui.
                    u.player.closeInventory();

                    //Create plot with the selection created by the user.
                    eUser.selectionTool.createPlot();

                });

        //Fill the border of the gui with grey stained glass pane.
        for (int i = 0; i <= 26; i++) {

            //Skip the centre.
            if (i == 10) {
                i = 17;
            }

            gui.setItem(i, Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, 1, ""));
        }

        return gui;

    }
}
