package me.bteuk.plotsystem.plots;

import com.sk89q.worldedit.math.BlockVector2;

public class Location {

    private int xmin;
    private int xmax;

    private int zmin;
    private int zmax;

    private String name;

    public Location(String name, int x1, int x2, int z1, int z2) {

        this.name = name;

        if (x1 < x2) {

            xmin = x1;
            xmax = x2;

        } else {

            xmin = x2;
            xmax = x1;

        }

        if (z1 < z2) {

            zmin = z1;
            zmax = z2;

        } else {

            zmin = z2;
            zmax = z1;

        }

    }

    public String getName() {
        return name;
    }

    public boolean inLocation(BlockVector2 bv) {

        if (bv.getX() >= xmin && bv.getX() <= xmax && bv.getZ() >= zmin && bv.getZ() <= zmax) {

            return true;

        } else {

            return false;

        }
    }
}
