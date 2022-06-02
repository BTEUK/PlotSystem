package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.enums.PlotDifficulty;
import me.bteuk.plotsystem.utils.enums.PlotSize;
import me.bteuk.plotsystem.utils.plugins.WGCreatePlot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;

public class SelectionTool extends WGCreatePlot {

    //Stores a reference to the user for simplicity.
    private final User u;

    //This vector of BlockVector2 (2d points (x,z)) represent the selected points.
    private final ArrayList<BlockVector2> vector;

    //The world where the selection is being made.
    private World world;

    //The location (plot system location) where the plot is.
    private String location;

    //Area of the plot (m^2).
    private int area;

    //Size and difficulty of the plot.
    //Represented by integer values of 1-3.
    //Size: 1=small, 2=medium, 3=large
    //Difficulty: 1=easy, 2=normal, 3=hard
    public int size;
    public int difficulty;

    //PlotSQL
    private final PlotSQL plotSQL;

    //Constructor, sets up the basics of the selection tool, including default values fo size and difficulty.
    public SelectionTool(User u, PlotSQL plotSQL) {

        this.u = u;
        vector = new ArrayList<>();
        this.plotSQL = plotSQL;

        //Set default size and difficulty
        size = 1;
        difficulty = 1;

    }

    //Clear the selection.
    //This is executed when the player starts a new selection (by left-clicking with the selection tool), or when the player creates a plot.
    public void clear() {

        world = null;
        location = null;

        size = 1;
        difficulty = 1;

        vector.clear();

    }

    //Starts a new selection with the selection tool, represents left-clicking.
    public void startSelection(Block block, String location) {

        //Since this is the start of a selection make sure the vector is empty.
        clear();

        //Set the world.
        world = block.getWorld();

        //Get the x,z of the block clicked and store it in the vector.
        BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());
        vector.add(bv2);

        //Set the location.
        this.location = location;

    }

    //Add a point to the selection, represents right-clicking.
    public boolean addPoint(Block block) {

        //Create the blockvector2.
        BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());

        //If the distance in a plot exceeds 500 blocks it's too large.
        //Send an error message to the player.
        if (bv2.distance(vector.get(0)) > 500) {

            u.player.sendMessage(Utils.chat("&cThis point is over 500 blocks from the first point, please make the selection smaller."));
            return false;

        } else {

            vector.add(bv2);
            return true;

        }
    }

    public void giveSelectionTool() {

        //Get the player inventory and check whether they already have the selection tool.
        PlayerInventory i = u.player.getInventory();

        //Check if the player already has the selection tool in their inventory.
        if (i.contains(PlotSystem.selectionTool)) {

            //Get the selection tool from their inventory and swap it with the item in their hand.
            i.setItem(i.first(PlotSystem.selectionTool), i.getItemInMainHand());
            i.setItemInMainHand(PlotSystem.selectionTool);

        } else {

            //If they don't have the selection tool already set it in their main hand.
            i.setItemInMainHand(PlotSystem.selectionTool);

        }
    }

    //Return number of elements in vector.
    public int size() {

        return vector.size();

    }

    public World world() {

        return world;

    }

    //Sets the area of the selection.
    public void area() {

        //If the vector has less than 3 points you can't get an area.
        if (size() < 3) {
            area = 0;
        }

        int sumX = 0;
        int sumZ = 0;

        for (int i = 0; i < size() - 1; i++) {

            sumX += vector.get(i).getX() * vector.get(i + 1).getZ();
            sumZ += vector.get(i + 1).getX() * vector.get(i).getZ();

        }

        area = Math.abs((sumX - sumZ) / 2);

    }

    //Sets the default plot size.
    public void setDefaultSize() {

        if (area <= PlotSystem.getInstance().getConfig().getInt("default_size.small")) {

            size = 1;

        } else if (area <= PlotSystem.getInstance().getConfig().getInt("default_size.medium")) {

            size = 2;

        } else {

            size = 3;

        }
    }

    //Returns the plot difficulty name.
    public PlotDifficulty difficultyName() {

        switch (difficulty) {

            case 1:
                return PlotDifficulty.EASY;
            case 2:
                return PlotDifficulty.NORMAL;
            case 3:
                return PlotDifficulty.HARD;
            default:
                return null;

        }
    }

    //Returns the plot size name.
    public PlotSize sizeName() {

        switch (size) {

            case 1:
                return PlotSize.SMALL;
            case 2:
                return PlotSize.MEDIUM;
            case 3:
                return PlotSize.LARGE;
            default:
                return null;

        }
    }

    //Returns the plot size material.
    public Material sizeMaterial() {

        switch (size) {

            case 1:
                return Material.LIME_CONCRETE;
            case 2:
                return Material.YELLOW_CONCRETE;
            case 3:
                return Material.RED_CONCRETE;
            default:
                return null;

        }
    }

    //Returns the plot difficulty material.
    public Material difficultyMaterial() {

        switch (difficulty) {

            case 1:
                return Material.LIME_CONCRETE;
            case 2:
                return Material.YELLOW_CONCRETE;
            case 3:
                return Material.RED_CONCRETE;
            default:
                return null;

        }
    }

    //Before this method can be run the player must have gone through the plot creation gui.
    //This will make sure the difficulty and size are set.
    public boolean createPlot() {

        //Create the plot.
        if (createPlot(u.player, world, location, vector, plotSQL, size, difficulty)) {

            //Store plot bounds.
            int i = 1;
            for (BlockVector2 point : vector) {

                plotSQL.update("INSERT INTO points_data(id,points_number,x,z) VALUES(" +
                        plotID + "," + i + "," + point.getX() + "," + point.getZ() + ");");
                i++;

            }

            //Send feedback.
            u.player.sendMessage(Utils.chat("&aPlot created with ID &3" + plotID +
                    "&a, difficulty &3" + difficultyName()) +
                    "&a and size &3" + sizeName());

            return true;

        } else {

            return false;

        }
    }
}
