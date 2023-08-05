package me.bteuk.plotsystem.utils.plugins;

import java.util.List;
import java.util.Set;

import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
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

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.Constants.MAX_Y;
import static me.bteuk.network.utils.Constants.MIN_Y;

public class WorldEditor {

    public static boolean updateWorld(List<BlockVector2> copyVector, List<BlockVector2> pasteVector, World copy, World paste) {

        //Get the worlds in worldEdit format
        com.sk89q.worldedit.world.World copyWorld = new BukkitWorld(copy);
        com.sk89q.worldedit.world.World pasteWorld = new BukkitWorld(paste);

        Polygonal2DRegion copyRegion = new Polygonal2DRegion(copyWorld, copyVector, MIN_Y, MAX_Y - 1);
        Polygonal2DRegion pasteRegion = new Polygonal2DRegion(pasteWorld, pasteVector, MIN_Y, MAX_Y - 1);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(copyRegion);

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(copyWorld).fastMode(true).checkMemory(false).limitUnlimited().changeSetNull().build()) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, copyRegion, clipboard, copyRegion.getMinimumPoint()
            );
            forwardExtentCopy.setCopyingBiomes(true);
            forwardExtentCopy.setCopyingEntities(true);
            // configure here
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            e.printStackTrace();
            return false;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(pasteWorld).fastMode(true).checkMemory(false).limitUnlimited().changeSetNull().build()) {

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pasteRegion.getMinimumPoint())
                    .copyBiomes(true)
                    .copyEntities(true)
                    // configure here
                    .build();
            Operations.complete(operation);
            editSession.flushQueue();
        } catch (WorldEditException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean largeCopy(BlockVector3 copyMin, BlockVector3 copyMax, BlockVector3 pasteMin, World copy, World paste) {

        //Get the worlds in worldEdit format
        com.sk89q.worldedit.world.World copyWorld = new BukkitWorld(copy);
        com.sk89q.worldedit.world.World pasteWorld = new BukkitWorld(paste);

        CuboidRegion copyRegion = new CuboidRegion(copyWorld, copyMin, copyMax);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(copyRegion);

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(copyWorld).fastMode(false).checkMemory(true).limitUnlimited().changeSetNull().build()) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, copyRegion, clipboard, copyRegion.getMinimumPoint()
            );
            forwardExtentCopy.setCopyingBiomes(true);
            Operations.complete(forwardExtentCopy);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(pasteWorld).fastMode(false).checkMemory(true).limitUnlimited().changeSetNull().build()) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pasteMin)
                    .ignoreAirBlocks(true)
                    .copyBiomes(true)
                    // configure here
                    .build();
            Operations.complete(operation);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        clipboard.close();

        return true;

    }

    /**
     * Deletes all entities in the given region.
     * @param vector the vector that represent the x,z bounds of the region
     * @param bukkitWorld the world of the region
     * @return true if successful
     */
    public static boolean deleteEntities(List<BlockVector2> vector, World bukkitWorld) {

        //Get the world in worldEdit format
        com.sk89q.worldedit.world.World world = new BukkitWorld(bukkitWorld);

        Polygonal2DRegion region = new Polygonal2DRegion(world, vector, MIN_Y, MAX_Y - 1);

        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(world).fastMode(true).checkMemory(false).limitUnlimited().changeSetNull().build()) {

            //Get a list of chunks to make sure they're loaded.
            Set<BlockVector2> chunks = region.getChunks();
            chunks.forEach(c -> editSession.getWorld().checkLoadedChunk(c.toBlockVector3()));

            List<? extends Entity> entities = editSession.getEntities(region);

            final int[] count = {0};
            entities.forEach(e -> {
                if (!e.getType().getId().equalsIgnoreCase("minecraft:player")) {
                    count[0]++;
                    e.remove();
                }
            });
            String ent = count[0] == 1 ? "entity" : "entities";
            LOGGER.info(String.format("Deleted %d %s from the region!", count[0], ent));
            editSession.flushQueue();

        } catch (WorldEditException e) {
            LOGGER.info("Unable to remove the entities in the region.");
            e.printStackTrace();
            return false;
        }

        return true;
    }
}