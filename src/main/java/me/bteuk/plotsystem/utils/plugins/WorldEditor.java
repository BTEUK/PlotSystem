package me.bteuk.plotsystem.utils.plugins;

import java.util.List;

import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WorldEditor {

    public static boolean updateWorld(List<BlockVector2> copyVector, List<BlockVector2> pasteVector, World copy, World paste) {

        //Get the worlds in worldEdit format
        com.sk89q.worldedit.world.World copyWorld = new BukkitWorld(copy);
        com.sk89q.worldedit.world.World pasteWorld = new BukkitWorld(paste);

        Polygonal2DRegion copyRegion = new Polygonal2DRegion(copyWorld, copyVector, 1, 256);
        Polygonal2DRegion pasteRegion = new Polygonal2DRegion(pasteWorld, pasteVector, 1, 256);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(copyRegion);

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(copyWorld).fastMode(true).checkMemory(false).limitUnlimited().changeSetNull().build()) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, copyRegion, clipboard, copyRegion.getMinimumPoint()
            );
            // configure here
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(pasteWorld).fastMode(true).checkMemory(false).limitUnlimited().changeSetNull().build()) {
            @SuppressWarnings("resource")
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pasteRegion.getMinimumPoint())
                    // configure here
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        return true;
    }
}