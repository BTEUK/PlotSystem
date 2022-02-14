package me.bteuk.plotsystem.gui;

import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CreatePlotGui {

    public static Inventory inv;
    public static Component inventory_name;
    public static int inv_rows = 3 * 9;

    public static void initialize() {

        inventory_name = Component.text("Create Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD);

        inv = Bukkit.createInventory(null, inv_rows);

    }

    public static Inventory Gui(User u) {

        Inventory toReturn = Bukkit.createInventory(null, inv_rows, inventory_name);

        inv.clear();

        Utils.createItem(inv, u.selectionTool.sizeMaterial(), 1, 12, Utils.chat("&b&l" + u.selectionTool.sizeName()),
                Utils.chat("&fClick to cycle through different sizes."));

        Utils.createItem(inv, u.selectionTool.difficultyMaterial(), 1, 12, Utils.chat("&b&l" + u.selectionTool.difficultyName()),
                Utils.chat("&fClick to cycle through different difficulties."));

        toReturn.setContents(inv.getContents());
        return toReturn;

    }

    public static void clicked(User u, int slot, ItemStack clicked, Inventory inv, NavigationSQL navigationSQL, PlotSQL plotSQL) {

        Player p = u.player;

        if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "" + ChatColor.BOLD + "Plots Only")) {

        }
        return;


    }
}
