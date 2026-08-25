package xyz.verarr.spreadspawnpoints.spawnpoints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.joml.Vector2i;

public final class Vector2iExtension {
    public static final Codec<Vector2i> CODEC =
        RecordCodecBuilder.create(instance
                                  -> instance
                                         .group(Codec.INT.fieldOf("x").forGetter(v -> v.x),
                                                Codec.INT.fieldOf("z").forGetter(v -> v.y))
                                         .apply(instance, Vector2i::new));
}
