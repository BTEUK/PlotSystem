package me.bteuk.plotsystem.tutorial;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.listeners.tutorial.CommandListener;
import me.bteuk.plotsystem.listeners.tutorial.MoveEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Tutorial {

    private Main instance;
    public static ItemStack tutorialGui;

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
