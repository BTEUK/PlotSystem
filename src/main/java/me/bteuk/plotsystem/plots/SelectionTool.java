package me.bteuk.plotsystem.plots;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.enums.PlotDifficulty;
import me.bteuk.plotsystem.utils.enums.PlotSize;
import me.bteuk.plotsystem.utils.plugins.WGCreatePlot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;

public class SelectionTool extends WGCreatePlot {

    //Stores a reference to the user for simplicity.
    private final User u;

    //This vector of BlockVector2 (2d points) represent the selected points.
    private final ArrayList<BlockVector2> vector;

    //The world where the selection is being made.
    private World world;

    //The location where the plot is.
    private String location;

    public SelectionTool(User u) {

        this.u = u;
        vector = new ArrayList<>();

    }

    //Clear the selection.
    public void clear() {

        world = null;
        location = null;
        vector.clear();

    }

    public void startSelection(Block block, String location) {

        //Since this is the start of a selection make sure the vector is empty.
        clear();

        //Set the world.
        world = block.getWorld();

        //Get the x,z of the block clicked and store it in the vector.
        BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());
        vector.add(bv2);
        this.location = location;

    }

    public boolean addPoint(Block block) {

        //Add a point to the vector.
        BlockVector2 bv2 = BlockVector2.at(block.getX(), block.getZ());

        //If the distance in a plot exceeds 500 blocks it's too large.
        if (bv2.distance(vector.get(0)) > 500) {

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
        if (i.contains(Main.selectionTool)) {

            //Get the selection tool from their inventory and swap it with the item in their hand.
            i.setItem(i.first(Main.selectionTool), i.getItemInMainHand());
            i.setItemInMainHand(Main.selectionTool);

        } else {

            //If they don't have the selection tool already set it in their main hand.
            i.setItemInMainHand(Main.selectionTool);

        }
    }

    //Return number of elements in vector.
    public int size() {

        return vector.size();

    }

    public World world() {

        return world;

    }

    //Sets size as the area of the selection
    public void area() {

        //If the vector has less than 3 points you can't get an area.
        if (size() < 3) {
            size = 0;
        }

        int sumX = 0;
        int sumZ = 0;

        for (int i = 0; i < size() - 1; i++) {

            sumX += vector.get(i).getX() * vector.get(i + 1).getZ();
            sumZ += vector.get(i + 1).getX() * vector.get(i).getZ();

        }

        size = Math.abs((sumX - sumZ) / 2);

    }

    public PlotDifficulty difficultyName() {

        switch(difficulty) {

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

    public PlotSize sizeName() {

        if (size < 600) {

            return PlotSize.SMALL;

        } else if (size < 1500) {

            return PlotSize.MEDIUM;

        } else {

            return PlotSize.LARGE;

        }
    }
}
