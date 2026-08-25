package xyz.verarr.spreadspawnpoints.spawnpoints;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import xyz.verarr.spreadspawnpoints.mixin.PlayerSpawnFinderInvoker;

public class SpawnPointHelper {
    public static boolean isValidSpawnPoint(ServerLevel level, BlockPos pos) {
        var future = PlayerSpawnFinder.findSpawn(level, pos);

        level.getServer().managedBlock(future::isDone);

        Vec3 result = future.join();

        BlockPos floored = BlockPos.containing(result);

        BlockPos found = PlayerSpawnFinderInvoker.invokeGetLevelRespawnPos(level, floored.getX(),
                                                                           floored.getZ());
        return found != null;
    }
}
