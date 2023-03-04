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

        //TODO Zone info

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
                    eUser.selectionTool.createZone();

                });

        //Set public/private.
        if (user.selectionTool.is_public) {

            setItem(13, Utils.createItem(Material.OAK_DOOR, 1,
                            Utils.title("Set the zone to private."),
                            Utils.line("Click to make the zone private."),
                            Utils.line("A private zone means the owner has"),
                            Utils.line("to invite members for them to build.")),
                    u ->

                    {

                        //Set private.
                        user.selectionTool.is_public = false;

                        //Refresh the gui.
                        refresh();
                        user.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                    });

        } else {

            setItem(13, Utils.createItem(Material.IRON_DOOR, 1,
                            Utils.title("Set the zone to public."),
                            Utils.line("Click to make the zone public."),
                            Utils.line("A public zone allows JrBuilder+"),
                            Utils.line("to join without having to request.")),
                    u ->

                    {

                        //Set private.
                        user.selectionTool.is_public = true;

                        //Refresh the gui.
                        refresh();
                        user.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                    });

        }

        //Set expiration time.
        setItem(13, Utils.createItem(Material.CLOCK, user.selectionTool.hours,
                        Utils.title("Set the zone expiration time."),
                        Utils.line("Click to cycle through expiration times."),
                        Utils.line("The current time is " + user.selectionTool.hours + " hours."),
                        Utils.line("The expiration time can be extended later.")),
                u ->

                {

                    //Increase expiration time.
                    switch (user.selectionTool.hours) {
                        case 2 -> user.selectionTool.hours = 6;
                        case 6 -> user.selectionTool.hours = 24;
                        case 24 -> user.selectionTool.hours = 48;
                        case 48 -> user.selectionTool.hours = 2;
                    }

                    //Refresh the gui.
                    refresh();
                    user.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

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
