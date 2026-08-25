package xyz.verarr.spreadspawnpoints.spawnpoints.generators;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.*;

public interface StrictCodecUtils {
    static <T> MapCodec<T> strict(MapCodec<T> original) {
        return new MapCodec<T>() {
            @Override
            public <K> DataResult<T> decode(DynamicOps<K> ops, MapLike<K> input) {
                Set<String> expectedKeys = original.keys(ops)
                                               .map(ops::getStringValue)
                                               .flatMap(dataResult -> dataResult.result().stream())
                                               .collect(Collectors.toSet());

                Set<String> inputKeys = new HashSet<>();
                input.entries().forEach(pair -> {
                    ops.getStringValue(pair.getFirst()).result().ifPresent(inputKeys::add);
                });

                inputKeys.removeAll(expectedKeys);

                if (!inputKeys.isEmpty()) {
                    return DataResult.error(() -> "Invalid tag keys: " + String.join(", ", inputKeys));
                }

                return original.decode(ops, input);
            }

            @Override
            public <K> RecordBuilder<K> encode(T input, DynamicOps<K> ops,
                                               RecordBuilder<K> prefix) {
                return original.encode(input, ops, prefix);
            }

            @Override
            public <K> java.util.stream.Stream<K> keys(DynamicOps<K> ops) {
                return original.keys(ops);
            }
        };
    }
}
