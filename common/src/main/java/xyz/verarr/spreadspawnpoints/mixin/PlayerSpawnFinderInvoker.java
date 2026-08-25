package xyz.verarr.spreadspawnpoints.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerSpawnFinder.class)
public interface PlayerSpawnFinderInvoker {
    @Invoker("getLevelRespawnPos")
    static BlockPos invokeGetLevelRespawnPos(ServerLevel world, int x, int z) {
        throw new AssertionError();
    }
}
