package xyz.verarr.spreadspawnpoints.spawnpoints.generators;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.joml.Vector2i;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPoints;
import xyz.verarr.spreadspawnpoints.mixin.LegacyRandomSourceAccessor;
import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointGenerator;
import xyz.verarr.spreadspawnpoints.spawnpoints.Vector2iExtension;

public class PredefinedSpawnPointGenerator implements SpawnPointGenerator {
    private List<Vector2i>    spawnPoints;
    private final RandomSource random;

    @Override
    public Vector2i next() {
        return new Vector2i(spawnPoints.get(random.nextInt(spawnPoints.size())));
    }

    @Override
    public boolean isValid(Vector2i spawnPoint) {
        return spawnPoints.contains(spawnPoint);
    }

    @Override
    public void add(Vector2i spawnPoint) { }

    @Override
    public void remove(Vector2i spawnPoint) { }

    // Serialization
    @Override
    public SpawnPointGeneratorType<?> getType() {
        return TYPE;
    }

    private static final Codec<List<Vector2i>> SPAWN_POINTS_CODEC =
        Vector2iExtension.CODEC.listOf().validate(spawnPoints -> {
            if (spawnPoints.isEmpty())
                return DataResult.error(() -> "spawnPoints must contain at least one spawnpoint");
            return DataResult.success(spawnPoints);
        });

    private static final Codec<PredefinedSpawnPointGenerator> CODEC = RecordCodecBuilder.create(
        instance
        -> instance
               .group(SPAWN_POINTS_CODEC.fieldOf("spawnPoints").forGetter(gen -> gen.spawnPoints),
                      Codec.LONG.fieldOf("seed").forGetter(
                          gen -> ((LegacyRandomSourceAccessor) (gen.random)).getSeed().get()))
               .apply(instance, PredefinedSpawnPointGenerator::new));

    /// From-parts constructor
    PredefinedSpawnPointGenerator(List<Vector2i> spawnPoints, long seed) {
        if (spawnPoints.isEmpty())
            throw new IllegalArgumentException("spawnPoints must contain at least one spawnpoint");
        this.spawnPoints = List.copyOf(spawnPoints);
        this.random      = new LegacyRandomSource(seed);
    }

    private static final Decoder<Consumer<PredefinedSpawnPointGenerator>> PARTIAL_DECODER =
        StrictCodecUtils
            .strict(RecordCodecBuilder.<Consumer<PredefinedSpawnPointGenerator>>mapCodec(
                instance
                -> instance
                       .group(SPAWN_POINTS_CODEC.optionalFieldOf("spawnPoints")
                                  .forGetter(_ -> Optional.empty()),
                              Codec.LONG.optionalFieldOf("seed").forGetter(_ -> Optional.empty()))
                       .apply(instance, (spawnPoints, seed) -> gen -> {
                           spawnPoints.ifPresent(points -> gen.spawnPoints = List.copyOf(points));
                           seed.ifPresent(gen.random::setSeed);
                       })))
            .codec();

    public static final SpawnPointGeneratorType<PredefinedSpawnPointGenerator> TYPE =
        SpawnPointGeneratorType.register(
            Identifier.fromNamespaceAndPath(SpreadSpawnPoints.MOD_ID, "predefined"),
            new SpawnPointGeneratorType<>(_ -> CODEC, PARTIAL_DECODER,
                                          PredefinedSpawnPointGenerator::new));

    /// Default constructor
    public PredefinedSpawnPointGenerator(ServerLevel serverLevel) {
        BlockPos worldSpawn = serverLevel.getRespawnData().pos();
        this.spawnPoints = List.of(new Vector2i(worldSpawn.getX(), worldSpawn.getZ()));
        this.random      = new LegacyRandomSource(serverLevel.getSeed());
    }
}
