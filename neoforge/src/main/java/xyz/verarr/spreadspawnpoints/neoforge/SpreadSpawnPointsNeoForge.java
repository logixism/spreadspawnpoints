package xyz.verarr.spreadspawnpoints.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPoints;
import xyz.verarr.spreadspawnpoints.commands.RespawnCommand;
import xyz.verarr.spreadspawnpoints.commands.SpawnpointsCommand;
import xyz.verarr.spreadspawnpoints.spawnpoints.BlueMapIntegration;

@Mod(SpreadSpawnPoints.MOD_ID)
public final class SpreadSpawnPointsNeoForge extends SpreadSpawnPoints {
    public SpreadSpawnPointsNeoForge() {
        // Run our common setup.
        init();

        if (ModList.get().isLoaded("bluemap")) {
            BlueMapIntegration.register();
            NeoForge.EVENT_BUS.addListener((ServerStartedEvent event)
                                               -> BlueMapIntegration.onServerStarted(event.getServer()));
            NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event)
                                               -> BlueMapIntegration.onServerStopped(event.getServer()));
        }
    }

    @Override
    protected void initCommands() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            event.getDispatcher().register(SpawnpointsCommand.command);
            event.getDispatcher().register(RespawnCommand.command);
        });
    }
}
