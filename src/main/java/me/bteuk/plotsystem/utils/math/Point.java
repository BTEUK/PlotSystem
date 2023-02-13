package me.bteuk.plotsystem.utils.math;

import java.util.List;

import com.sk89q.worldedit.math.BlockVector2;

public class Point {

    public static BlockVector2 getAveragePoint(List<BlockVector2> points) {

        double size = points.size();
        double x = 0;
        double z = 0;

        for (BlockVector2 bv : points) {

            x += bv.getX() / size;
            z += bv.getZ() / size;

        }

        return (BlockVector2.at(x, z));

    }
}
