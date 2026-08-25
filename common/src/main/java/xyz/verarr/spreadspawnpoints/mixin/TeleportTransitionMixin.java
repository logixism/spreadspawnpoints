package xyz.verarr.spreadspawnpoints.mixin;

import java.util.Set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.verarr.spreadspawnpoints.ServerPlayerExtension;

@Mixin(TeleportTransition.class)
abstract class TeleportTransitionMixin {
    @WrapMethod(
        method =
            "createDefault(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;")
    private static TeleportTransition
    defaultForPlayer(ServerPlayer                              player,
                     TeleportTransition.PostTeleportTransition postTeleportTransition,
                     Operation<TeleportTransition>             original) {
        return spreadspawnpoints$spawnPointForPlayer(player, postTeleportTransition, false);
    }

    @WrapMethod(
        method =
            "missingRespawnBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;")
    private static TeleportTransition
    missingRespawnBlockForPlayer(ServerPlayer                              player,
                                 TeleportTransition.PostTeleportTransition postTeleportTransition,
                                 Operation<TeleportTransition>             original) {
        return spreadspawnpoints$spawnPointForPlayer(player, postTeleportTransition, true);
    }

    @Unique
    private static TeleportTransition spreadspawnpoints$spawnPointForPlayer(
        ServerPlayer                              player,
        TeleportTransition.PostTeleportTransition postTeleportTransition,
        boolean                                   missingRespawnBlock) {
        ServerLevel           newLevel    = player.level().getServer().findRespawnDimension();
        LevelData.RespawnData respawnData = newLevel.getRespawnData();
        return new TeleportTransition(
            newLevel,
            Vec3.atBottomCenterOf(player.adjustSpawnLocation(
                newLevel, ServerPlayerExtension.getSpecificSpawnPos(player, newLevel))),
            Vec3.ZERO, respawnData.yaw(), respawnData.pitch(), missingRespawnBlock, false, Set.of(),
            postTeleportTransition);
    }
}
