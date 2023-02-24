package me.bteuk.plotsystem.gui;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class CreateZoneGui extends Gui {

    private final User user;

    //This gui handles the plot creation process, and will allow the user to set the parameters of the plot.
    public CreateZoneGui(User user) {

        super(27, Component.text("Create Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    private void createGui() {

        //Create zone.
        setItem(13, Utils.createItem(Material.DIAMOND, 1,
                        Utils.title("Create Zone"),
                        Utils.line("Click create a new zone with the settings selected.")),
                u ->

                {

                    //Check if settings are valid.

                    User eUser = PlotSystem.getInstance().getUser(u.player);

                    //Close the inventory.
                    u.player.closeInventory();

                    //Create plot with the selection created by the user.
                    eUser.selectionTool.createPlot();

                });

        //Fill the border of the gui with grey stained glass pane.
        /*for (int i = 0; i <= 26; i++) {

            //Skip the centre.
            if (i == 10) {
                i = 17;
            }

            setItem(i, Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, 1, ""));
        }*/
    }

    public void refresh() {

        clearGui();
        createGui();

    }
}
