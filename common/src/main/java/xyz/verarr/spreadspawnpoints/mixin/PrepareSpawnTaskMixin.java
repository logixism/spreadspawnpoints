package xyz.verarr.spreadspawnpoints.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.config.PrepareSpawnTask;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.verarr.spreadspawnpoints.ServerPlayerExtension;

@Mixin(PrepareSpawnTask.class)
abstract class PrepareSpawnTaskMixin {
    @Final @Shadow private NameAndId       nameAndId;
    @Final @Shadow private MinecraftServer server;

    @WrapOperation(
        method = "start(Ljava/util/function/Consumer;)V",
        at     = @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/world/level/storage/ServerLevelData;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    LevelData.RespawnData
    initialSpawnData(ServerLevelData instance, Operation<LevelData.RespawnData> original) {
        ServerLevel level = server.overworld();
        BlockPos    pos =
            ServerPlayerExtension.getSpecificSpawnPos(nameAndId, level);  // TODO: default dimension
        return new LevelData.RespawnData(new GlobalPos(level.dimension(), pos), 0, 0);
    }
}
