package xyz.verarr.spreadspawnpoints;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.portal.TeleportTransition;

import org.joml.Vector2i;
import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointManager;

public class ServerPlayerExtension {
    public static void moveToSpawn(ServerPlayer player) {
        TeleportTransition defaultSpawn =
            TeleportTransition.createDefault(player, TeleportTransition.DO_NOTHING);

        player.teleport(defaultSpawn);
    }

    public static BlockPos getSpecificSpawnPos(ServerPlayer player, ServerLevel level) {
        String name = player.getDisplayName().getString();
        return getSpecificSpawnPos(player.getUUID(), level, name);
    }

    public static BlockPos getSpecificSpawnPos(NameAndId player, ServerLevel level) {
        return getSpecificSpawnPos(player.id(), level, player.name());
    }

    public static BlockPos getSpecificSpawnPos(UUID player, ServerLevel level) {
        return getSpecificSpawnPos(player, level, player);
    }

    private static BlockPos getSpecificSpawnPos(UUID player, ServerLevel level, Object displayAs) {
        SpreadSpawnPoints.LOGGER.info("Player {} is being spawned in the world: {}", displayAs,
                                      level.dimension());
        SpawnPointManager spawnPointManager = SpawnPointManager.getInstance(level);
        Vector2i          spawnPoint        = spawnPointManager.getSpawnPoint(player);
        SpreadSpawnPoints.LOGGER.info("Player {} will spawn at: {}, {}", displayAs, spawnPoint.x,
                                      spawnPoint.y);
        return new BlockPos(spawnPoint.x, 0, spawnPoint.y);
    }
}
