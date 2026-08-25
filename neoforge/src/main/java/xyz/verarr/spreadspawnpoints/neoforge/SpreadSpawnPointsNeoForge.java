package xyz.verarr.spreadspawnpoints.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPoints;
import xyz.verarr.spreadspawnpoints.commands.RespawnCommand;
import xyz.verarr.spreadspawnpoints.commands.SpawnpointsCommand;

@Mod(SpreadSpawnPoints.MOD_ID)
public final class SpreadSpawnPointsNeoForge extends SpreadSpawnPoints {
    public SpreadSpawnPointsNeoForge() {
        // Run our common setup.
        init();
    }

    @Override
    protected void initCommands() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            event.getDispatcher().register(SpawnpointsCommand.command);
            event.getDispatcher().register(RespawnCommand.command);
        });
    }
}
