package xyz.verarr.spreadspawnpoints.spawnpoints;

import java.util.stream.Stream;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;

import com.mojang.serialization.*;

import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointGenerator.SpawnPointGeneratorType;

class FlatGeneratorMapCodec extends MapCodec<SpawnPointGenerator> {
    private final ServerLevel level;

    public FlatGeneratorMapCodec(ServerLevel level) { this.level = level; }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString("spawnPointGenerator"),
                         ops.createString("spawnPointGeneratorData"));
    }

    @Override
    public <T> DataResult<SpawnPointGenerator> decode(DynamicOps<T> ops, MapLike<T> input) {
        T typeElement = input.get("spawnPointGenerator");
        if (typeElement == null)
            return DataResult.error(() -> "Missing field: spawnPointGenerator");

        var keyCodec = ResourceKey.codec(SpawnPointGeneratorType.REGISTRY_KEY);
        return keyCodec.parse(ops, typeElement).flatMap(key -> {
            var type = SpawnPointGeneratorType.REGISTRY.get(key);
            if (type.isEmpty()) return DataResult.error(() -> "Unknown generator type: " + key);

            Codec<? extends SpawnPointGenerator> specificCodec =
                type.get().value().codecFactory().apply(level);

            T dataElement = input.get("spawnPointGeneratorData");
            if (dataElement == null)
                return DataResult.error(() -> "Missing field: spawnPointGeneratorData");

            return specificCodec.parse(ops, dataElement).map(gen -> (SpawnPointGenerator) gen);
        });
    }

    @Override
    public <T> RecordBuilder<T>
               encode(SpawnPointGenerator input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        var keyCodec = ResourceKey.codec(SpawnPointGeneratorType.REGISTRY_KEY);
        ResourceKey<SpawnPointGeneratorType<?>> key =
            SpawnPointGeneratorType.REGISTRY.getResourceKey(input.getType())
                .orElseThrow(() -> new IllegalStateException("Unregistered type"));

        prefix.add("spawnPointGenerator", keyCodec.encodeStart(ops, key));

        @SuppressWarnings("unchecked")  // T extends SpawnPointGenerator
        Codec<SpawnPointGenerator> specificCodec =
            (Codec<SpawnPointGenerator>) input.getType().codecFactory().apply(level);
        prefix.add("spawnPointGeneratorData", specificCodec.encodeStart(ops, input));

        return prefix;
    }
}