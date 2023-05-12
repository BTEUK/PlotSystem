package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WGCreatePlot;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;

import static me.bteuk.plotsystem.PlotSystem.LOGGER;

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

    public BlockData outlineBlock = Material.LIGHT_BLUE_CONCRETE.createBlockData();
    public BlockData limeConc = Material.LIME_CONCRETE.createBlockData();
    public BlockData yellowConc = Material.YELLOW_CONCRETE.createBlockData();
    public BlockData redConc = Material.RED_CONCRETE.createBlockData();

    //PlotSQL
    private final PlotSQL plotSQL;

    //PlotOutline
    private final PlotOutline plotOutline;

    //Zones settings.
    public int hours;
    public boolean is_public;

    //Constructor, sets up the basics of the selection tool, including default values fo size and difficulty.
    public SelectionTool(User u, PlotSQL plotSQL) {

        this.u = u;
        vector = new ArrayList<>();
        this.plotSQL = plotSQL;

        //Set default size and difficulty
        size = 1;
        difficulty = 1;

        hours = 2;
        is_public = false;

        plotOutline = new PlotOutline();

    }

    //Clear the selection.
    //This is executed when the player starts a new selection (by left-clicking with the selection tool), or when the player creates a plot.
    public void clear() {

        world = null;
        location = null;

        size = 1;
        difficulty = 1;

        hours = 2;
        is_public = false;

        vector.clear();

        //Replace blocks back to original state.
        plotOutline.revertBlocks(u.player);

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

        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getInstance(),
                () -> plotOutline.sendBlockChange(u.player, bv2, outlineBlock),1L);

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

            u.player.sendMessage(Utils.error("This point is over 500 blocks from the first point, please make the selection smaller."));
            return false;

        } else {

            vector.add(bv2);

            //Clear previous selection outline.
            plotOutline.revertBlocks(u.player);

            //Create new outline.
            //Adding a point already means at least 2 points, so we can ignore the 1 point case.
            if (vector.size() == 2) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getInstance(),
                        () -> plotOutline.createLine(u.player, vector.get(0), vector.get(1), outlineBlock),1L);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getInstance(),
                        () -> plotOutline.createOutline(u.player, vector, outlineBlock, true),1L);
            }

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

            u.player.sendMessage(Utils.success("Switched to selection tool from inventory."));

        } else {

            //If they don't have the selection tool already set it in their main hand.
            i.setItemInMainHand(PlotSystem.selectionTool);

            u.player.sendMessage(Utils.success("Set selection tool to main hand."));

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

        int sum = 0;

        for (int i = 0; i < size(); i++) {

            if (i == (size() - 1)) {

                sum += (((vector.get(i).getZ() + vector.get(0).getZ())/2) * (vector.get(0).getX() - vector.get(i).getX()));

            } else {

                sum += (((vector.get(i).getZ() + vector.get(i+1).getZ())/2) * (vector.get(i+1).getX() - vector.get(i).getX()));

            }
        }

        area = Math.abs(sum);

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

    //Before this method can be run the player must have gone through the plot creation gui.
    //This will make sure the difficulty and size are set.
    public void createPlot() {

        //Create the plot.
        if (createPlot(u.player, world, location, vector, plotSQL, size, difficulty)) {

            //Store plot bounds.
            int i = 1;
            for (BlockVector2 point : vector) {

                plotSQL.update("INSERT INTO plot_corners(id,corner,x,z) VALUES(" +
                        plotID + "," + i + "," + point.getX() + "," + point.getZ() + ");");
                i++;

            }

            //Send feedback.
            u.player.sendMessage(Utils.success("Plot created with ID ")
                    .append(Component.text(plotID, NamedTextColor.DARK_AQUA))
                    .append(Utils.success(", difficulty "))
                    .append(Component.text(PlotValues.difficultyName(difficulty), NamedTextColor.DARK_AQUA))
                    .append(Utils.success(" and size "))
                    .append(Component.text(PlotValues.sizeName(size), NamedTextColor.DARK_AQUA)));
            LOGGER.info("Plot created with ID " + plotID +
                    ", difficulty " + PlotValues.difficultyName(difficulty) +
                    " and size " + PlotValues.sizeName(size));

            //Clear previous blocks.
            plotOutline.previousBlocks.clear();

            //Change plot outline to blockType of plot, rather than of selection.
            plotOutline.createOutline(u.player, WorldGuardFunctions.getPoints(String.valueOf(plotID), world), difficultyMaterial(difficulty), false);

        }
    }

    //Before this method can be run the player must have gone through the zone creation gui.
    //This will make sure public/private and expiration time has been set.
    public void createZone() {

        long expiration = Time.currentTime() + (hours * 1000L * 60L * 60L);

        //Create the zone.
        if (createZone(u.player, world, location, vector, plotSQL, expiration, is_public)) {

            //Add owner.
            plotSQL.update("INSERT INTO zone_members(id,uuid,is_owner) VALUES(" + plotID + ",'" + u.player.getUniqueId() + "',1);");

            //Store zone bounds.
            int i = 1;
            for (BlockVector2 point : vector) {

                plotSQL.update("INSERT INTO zone_corners(id,corner,x,z) VALUES(" +
                        plotID + "," + i + "," + point.getX() + "," + point.getZ() + ");");
                i++;

            }

            //Send feedback.
            u.player.sendMessage(Utils.success("Zone created with ID ")
                    .append(Component.text(plotID, NamedTextColor.DARK_AQUA))
                    .append(Utils.success(", it will expire at "))
                    .append(Component.text(Time.getDateTime(expiration), NamedTextColor.DARK_AQUA))
                    .append(Utils.success(", this can be extended in the Zone Menu.")));
            LOGGER.info("Zone created with ID " + plotID +
                    ", it will expire at " + Time.getDateTime(expiration));

            //Clear previous blocks.
            plotOutline.previousBlocks.clear();

            //Change plot outline to blockType of plot, rather than of selection.
            plotOutline.createOutline(u.player, WorldGuardFunctions.getPoints("z" + plotID, world), Material.PURPLE_CONCRETE.createBlockData(), false);

        }
    }

    //Returns the plot difficulty material.
    public BlockData difficultyMaterial(int difficulty) {

        return switch (difficulty) {
            case 1 -> limeConc;
            case 2 -> yellowConc;
            case 3 -> redConc;
            default -> null;
        };
    }
}
