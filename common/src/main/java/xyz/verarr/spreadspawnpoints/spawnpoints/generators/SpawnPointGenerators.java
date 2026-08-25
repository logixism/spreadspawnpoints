package xyz.verarr.spreadspawnpoints.spawnpoints.generators;

public interface SpawnPointGenerators {
    static void init() {
        var _ = VanillaSpawnPointGenerator.TYPE;
        var _ = RandomSpawnPointGenerator.TYPE;
        var _ = GridSpawnPointGenerator.TYPE;
        var _ = SpringSpawnPointGenerator.TYPE;
    }
}
