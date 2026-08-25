package xyz.verarr.spreadspawnpoints.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPoints;
import xyz.verarr.spreadspawnpoints.spawnpoints.BlueMapIntegration;
import xyz.verarr.spreadspawnpoints.commands.RespawnCommand;
import xyz.verarr.spreadspawnpoints.commands.SpawnpointsCommand;

public final class SpreadSpawnPointsFabric extends SpreadSpawnPoints implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        init();

        if (FabricLoader.getInstance().isModLoaded("bluemap")) {
            BlueMapIntegration.register();
            ServerLifecycleEvents.SERVER_STARTED.register(BlueMapIntegration::onServerStarted);
            ServerLifecycleEvents.SERVER_STOPPED.register(BlueMapIntegration::onServerStopped);
        }
    }

    @Override
    protected void initCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(SpawnpointsCommand.command);
            dispatcher.register(RespawnCommand.command);
        });
    }
}
