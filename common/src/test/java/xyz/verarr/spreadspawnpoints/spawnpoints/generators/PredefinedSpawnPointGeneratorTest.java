package xyz.verarr.spreadspawnpoints.spawnpoints.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

class PredefinedSpawnPointGeneratorTest {
    private static final List<Vector2i> SPAWN_POINTS =
        List.of(new Vector2i(-120, 45), new Vector2i(300, -900), new Vector2i(42, 42));

    @Test
    void randomlySelectsOnlyConfiguredSpawnPoints() {
        PredefinedSpawnPointGenerator generator =
            new PredefinedSpawnPointGenerator(SPAWN_POINTS, 1234L);
        Set<Vector2i> selected = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            Vector2i spawnPoint = generator.next();
            assertTrue(SPAWN_POINTS.contains(spawnPoint));
            assertTrue(generator.isValid(spawnPoint));
            selected.add(spawnPoint);
        }

        assertEquals(Set.copyOf(SPAWN_POINTS), selected);
        assertFalse(generator.isValid(new Vector2i(0, 0)));
    }

    @Test
    void returnedSpawnPointCannotMutateConfiguration() {
        PredefinedSpawnPointGenerator generator =
            new PredefinedSpawnPointGenerator(List.of(new Vector2i(10, 20)), 1234L);

        Vector2i spawnPoint = generator.next();
        spawnPoint.set(30, 40);

        assertEquals(new Vector2i(10, 20), generator.next());
        assertFalse(generator.isValid(spawnPoint));
    }

    @Test
    void generatorDataReplacesSpawnPoints() {
        PredefinedSpawnPointGenerator generator =
            new PredefinedSpawnPointGenerator(List.of(new Vector2i(10, 20)), 1234L);
        List<Vector2i> replacement = List.of(new Vector2i(-50, 75), new Vector2i(500, 1000));

        PredefinedSpawnPointGenerator.TYPE.partialDecoder()
            .parse(NbtOps.INSTANCE, generatorData(replacement))
            .getOrThrow(IllegalArgumentException::new)
            .accept(generator);

        assertFalse(generator.isValid(new Vector2i(10, 20)));
        for (int i = 0; i < 100; i++)
            assertTrue(replacement.contains(generator.next()));
    }

    @Test
    void generatorDataRejectsEmptySpawnPointList() {
        PredefinedSpawnPointGenerator generator =
            new PredefinedSpawnPointGenerator(SPAWN_POINTS, 1234L);

        assertThrows(IllegalArgumentException.class,
                     ()
                         -> PredefinedSpawnPointGenerator.TYPE.partialDecoder()
                                .parse(NbtOps.INSTANCE, generatorData(List.of()))
                                .getOrThrow(IllegalArgumentException::new)
                                .accept(generator));
    }

    private static CompoundTag generatorData(List<Vector2i> spawnPoints) {
        ListTag spawnPointTags = new ListTag();
        for (Vector2i spawnPoint : spawnPoints) {
            CompoundTag spawnPointTag = new CompoundTag();
            spawnPointTag.putInt("x", spawnPoint.x);
            spawnPointTag.putInt("z", spawnPoint.y);
            spawnPointTags.add(spawnPointTag);
        }

        CompoundTag data = new CompoundTag();
        data.put("spawnPoints", spawnPointTags);
        return data;
    }
}
