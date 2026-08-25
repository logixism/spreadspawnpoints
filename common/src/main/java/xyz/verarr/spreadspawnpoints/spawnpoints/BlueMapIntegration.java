package xyz.verarr.spreadspawnpoints.spawnpoints;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.joml.Vector2ic;

/**
 * Optional BlueMap integration for predefined spawnpoints.
 */
public final class BlueMapIntegration {
    static final String MARKER_SET_ID = "spreadspawnpoints-predefined-spawnpoints";

    private static BlueMapAPI     api;
    private static MinecraftServer server;
    private static boolean         registered;

    private BlueMapIntegration() { }

    /**
     * Registers the BlueMap and spawnpoint-generator lifecycle listeners.
     */
    public static synchronized void register() {
        if (registered)
            return;

        registered = true;
        SpawnPointManager.setGeneratorChangeListener(BlueMapIntegration::updateMarkers);
        BlueMapAPI.onEnable(BlueMapIntegration::onBlueMapEnabled);
        BlueMapAPI.onDisable(BlueMapIntegration::onBlueMapDisabled);
    }

    /**
     * Supplies the active server once it has finished starting.
     *
     * @param startedServer active Minecraft server
     */
    public static synchronized void onServerStarted(MinecraftServer startedServer) {
        server = startedServer;
        scheduleSynchronization();
    }

    /**
     * Clears the active server after it stops.
     *
     * @param stoppedServer stopped Minecraft server
     */
    public static synchronized void onServerStopped(MinecraftServer stoppedServer) {
        if (server == stoppedServer)
            server = null;
    }

    private static synchronized void onBlueMapEnabled(BlueMapAPI enabledApi) {
        api = enabledApi;
        scheduleSynchronization();
    }

    private static synchronized void onBlueMapDisabled(BlueMapAPI disabledApi) {
        if (api == disabledApi)
            api = null;
    }

    private static void scheduleSynchronization() {
        if (api != null && server != null)
            server.execute(BlueMapIntegration::synchronizeAllLevels);
    }

    private static void synchronizeAllLevels() {
        MinecraftServer activeServer;
        synchronized (BlueMapIntegration.class) {
            if (api == null || server == null)
                return;
            activeServer = server;
        }

        for (ServerLevel level : activeServer.getAllLevels())
            updateMarkers(SpawnPointManager.getInstance(level));
    }

    private static synchronized void updateMarkers(SpawnPointManager manager) {
        if (api == null)
            return;

        List<? extends Vector2ic> spawnPoints = manager.predefinedSpawnPoints();
        api.getWorld(manager.level()).ifPresent(world -> {
            if (spawnPoints == null) {
                world.getMaps().forEach(map -> map.getMarkerSets().remove(MARKER_SET_ID));
                return;
            }

            MarkerSet markerSet = createMarkerSet(spawnPoints, manager.level().getSeaLevel());
            world.getMaps().forEach(map -> map.getMarkerSets().put(MARKER_SET_ID, markerSet));
        });
    }

    static MarkerSet createMarkerSet(List<? extends Vector2ic> spawnPoints, int markerY) {
        MarkerSet markerSet = MarkerSet.builder()
                                  .label("Spawns")
                                  .toggleable(true)
                                  .defaultHidden(false)
                                  .build();

        for (int index = 0; index < spawnPoints.size(); index++) {
            Vector2ic spawnPoint = spawnPoints.get(index);
            POIMarker marker = POIMarker.builder()
                                   .label("Spawn #" + (index + 1))
                                   .position(spawnPoint.x() + 0.5, markerY, spawnPoint.y() + 0.5)
                                   .sorting(index)
                                   .build();
            markerSet.getMarkers().put("spawnpoint-" + index, marker);
        }

        return markerSet;
    }
}
