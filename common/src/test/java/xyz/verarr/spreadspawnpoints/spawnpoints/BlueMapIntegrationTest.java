package xyz.verarr.spreadspawnpoints.spawnpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

class BlueMapIntegrationTest {
    @Test
    void createsVisibleMarkerForEachPredefinedSpawnpoint() {
        MarkerSet markerSet = BlueMapIntegration.createMarkerSet(
            List.of(new Vector2i(-10, 20), new Vector2i(300, -400)), 63);

        assertEquals("Predefined spawnpoints", markerSet.getLabel());
        assertTrue(markerSet.isToggleable());
        assertFalse(markerSet.isDefaultHidden());
        assertEquals(2, markerSet.getMarkers().size());

        POIMarker first = assertInstanceOf(POIMarker.class,
                                           markerSet.getMarkers().get("spawnpoint-0"));
        assertEquals("Predefined spawnpoint 1", first.getLabel());
        assertEquals("X: -10, Z: 20", first.getDetail());
        assertEquals(new Vector3d(-9.5, 63, 20.5), first.getPosition());

        POIMarker second = assertInstanceOf(POIMarker.class,
                                            markerSet.getMarkers().get("spawnpoint-1"));
        assertEquals(new Vector3d(300.5, 63, -399.5), second.getPosition());
    }
}
