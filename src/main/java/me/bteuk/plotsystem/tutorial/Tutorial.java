package me.bteuk.plotsystem.tutorial;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.listeners.tutorial.CommandListener;
import me.bteuk.plotsystem.listeners.tutorial.MoveEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tutorial {

    private Main instance;
    public static ItemStack tutorialGui;

    //TutorialInfo tutorial;
    HashMap<Integer, String> pl;
    List<BlockVector2> pt;
    Location lo;
    ArrayList<Integer> pls;

    public Tutorial(Main instance) {

        this.instance = instance;

    }

    public void setup() {

        //Setup Tutorial Constants
        new TutorialConstants(instance.getConfig());

        //Create tutorial type skip item
        tutorialGui = new ItemStack(Material.LECTERN);
        ItemMeta meta3 = tutorialGui.getItemMeta();
        meta3.setLocalizedName(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Menu");
        tutorialGui.setItemMeta(meta3);

        //Tutorial listeners
        new CommandListener(instance);
        new MoveEvent(instance);

    }

}
