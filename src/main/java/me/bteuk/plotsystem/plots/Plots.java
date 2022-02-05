package me.bteuk.plotsystem.plots;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.listeners.plots.ClaimEnter;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Plots {

    private Main instance;
    public static ItemStack selectionTool;

    private PlotSQL plotSQL;
    private GlobalSQL globalSQL;

    public Plots(Main instance, PlotSQL plotSQL, GlobalSQL globalSQL) {

        this.instance = instance;
        this.plotSQL = plotSQL;
        this.globalSQL = globalSQL;

    }

    public void setup() {

        //Create selection tool item
        selectionTool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = selectionTool.getItemMeta();
        meta.setLocalizedName(Utils.chat("&aSelection Tool"));
        selectionTool.setItemMeta(meta);

        //Listeners
        new ClaimEnter(instance, plotSQL, globalSQL);

    }

}
