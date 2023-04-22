package me.bteuk.plotsystem.reviewing;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.utils.User;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class PreviousFeedbackGui extends Gui {

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    private final int plotID;
    private final User user;

    public PreviousFeedbackGui(int plotID, User user) {

        super(45, Component.text("Previous Feedback", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;
        this.user = user;

        //Get plot sql.
        plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        globalSQL = Network.getInstance().globalSQL;

        createGui();

    }

    private void createGui() {

        //Get plot owner uuid.
        String uuid = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;");

        //Get the number of times the plot was denied for the current plot owner.
        int deniedCount = plotSQL.getInt("SELECT COUNT(attempt) FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "';");

        //Slot count.
        int slot = 10;

        //Iterate through the deniedCount inversely.
        //We cap the number at 21, since we'd never expect a player to have more plots denied than that,
        //it also saves us having to create multiple pages.
        for (int i = deniedCount; i > 0; i--) {

            //If the slot is greater than the number that fit in a page, stop.
            if (slot > 34) {

                break;

            }

            //Add player to gui.
            int finalI = i;
            setItem(slot, Utils.createItem(Material.WRITTEN_BOOK, 1,
                            Utils.title("Feedback for submission " + i),
                            Utils.line("Click to view feedback for this submission."),
                            Utils.line("Reviewed by ")
                                    .append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                                            + plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "' AND attempt=" + i + ";") + "';"), NamedTextColor.GRAY))),

                    u ->

                    {

                        //Close inventory.
                        u.player.closeInventory();

                        //Create book.
                        Component title = Utils.title("Plot " + plotID + " Attempt " + finalI);
                        Component author = Utils.line(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "' AND attempt=" + finalI + ";") + "';"));

                        //Get pages of the book.
                        ArrayList<String> sPages = plotSQL.getStringList("SELECT contents FROM book_data WHERE id="
                                + plotSQL.getInt("SELECT book_id FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "' AND attempt=" + finalI + ";") + ";");

                        //Create a list of components from the list of strings.
                        ArrayList<Component> pages = new ArrayList<>();
                        for (String page : sPages) {
                            pages.add(Component.text(page));
                        }

                        Book book = Book.book(title, author, pages);

                        //Open the book.
                        u.player.openBook(book);

                    });


            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }
        }

        //Return to plot info menu.
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
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

        this.clearGui();
        createGui();

    }
}
