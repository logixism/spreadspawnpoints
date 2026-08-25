package xyz.verarr.spreadspawnpoints.spawnpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.joml.Vector2i;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPoints;
import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointGenerator.SpawnPointGeneratorType;
import xyz.verarr.spreadspawnpoints.spawnpoints.generators.VanillaSpawnPointGenerator;

public class SpawnPointManager extends SavedData {
    private static final SpawnPointGeneratorType<?> DEFAULT_SPAWNPOINT_GENERATOR =
        VanillaSpawnPointGenerator.TYPE;

    /**
     * Constructs a spawnpoint generator of the specified type for the
     * specified world.
     *
     * @param generatorType type of the generator to construct
     * @param level         server level to use in the constructor
     * @return the newly constructed spawnpoint generator
     * @throws IllegalArgumentException if given SpawnPointGenerator doesn't
     *                                  have a constructor with a ServerWorld parameter
     */
    private static SpawnPointGenerator
    constructSpawnPointGeneratorForWorld(SpawnPointGeneratorType<?> generatorType,
                                         ServerLevel                level) {
        return generatorType.constructor().apply(level);
    }

    private final Map<UUID, Vector2i> playerSpawnPoints = new HashMap<>();

    private SpawnPointGenerator generator;
    private final ServerLevel   level;

    private SpawnPointManager(ServerLevel level) {
        this.level     = level;
        this.generator = constructSpawnPointGeneratorForWorld(DEFAULT_SPAWNPOINT_GENERATOR, level);
    }

    private SpawnPointManager(ServerLevel level, SpawnPointGenerator generator) {
        this.level     = level;
        this.generator = generator;
    }

    /**
     * Gets the identifier of the spawnpoint generator currently in use.
     *
     * @return the resource key of the spawnpoint generator
     */
    public ResourceKey<SpawnPointGeneratorType<?>> getSpawnPointGenerator() {
        return SpawnPointGenerator.SpawnPointGeneratorType.REGISTRY
            .getResourceKey(generator.getType())
            .orElseThrow(()
                             -> new IllegalStateException("Current generator is unregistered: "
                                                          + generator.getType()));
    }

    /**
     * Replaces the currently used spawnpoint generator with a newly
     * constructed one.
     *
     * @param key the resource key of the new spawnpoint generator type
     */
    public void
    setSpawnPointGenerator(ResourceKey<SpawnPointGenerator.SpawnPointGeneratorType<?>> key) {
        var type =
            SpawnPointGenerator.SpawnPointGeneratorType.REGISTRY.getOptional(key).orElseThrow(
                ()
                    -> new IllegalArgumentException("Unknown spawnpoint generator: "
                                                    + key.identifier()));
        generator = type.constructor().apply(level);
    }

    /**
     * Erases all spawnpoints.
     */
    public void resetSpawnPoints() { playerSpawnPoints.clear(); }

    /**
     * Generate a new spawnpoint, <b>iteratively trying</b> until a valid
     * spawnpoint is found.
     *
     * @return new valid spawnpoint
     */
    protected Vector2i nextSafe() {
        int vanillaInvalid = 0;
        int customInvalid  = 0;
        while (true) {
            int totalInvalid = vanillaInvalid + customInvalid;
            if (totalInvalid > 10000) {
                SpreadSpawnPoints.LOGGER.error(
                    "Spawnpoint search exceeded 10,000 iterations, aborting");
                BlockPos fallback = level.getRespawnData().pos();
                return new Vector2i(fallback.getX(), fallback.getZ());
            }
            if (totalInvalid % 100 == 0 && totalInvalid != 0)
                SpreadSpawnPoints.LOGGER.warn("Iterating through {}th spawnpoint", totalInvalid);

            Vector2i spawnPoint = generator.next();

            boolean customValid = generator.isValid(spawnPoint);
            if (!customValid) {
                customInvalid++;
                continue;
            }

            boolean vanillaValid = SpawnPointHelper.isValidSpawnPoint(
                level, new BlockPos(spawnPoint.x, 0, spawnPoint.y));
            if (!vanillaValid) {
                vanillaInvalid++;
                continue;
            }

            if (totalInvalid > 1)
                SpreadSpawnPoints.LOGGER.info(
                    "Iterated through {} spawnpoints ({} gamerule-invalid, {} generator-invalid) before valid spawnpoint found",
                    totalInvalid, vanillaInvalid, customInvalid);
            generator.add(spawnPoint);
            return spawnPoint;
        }
    }

    /**
     * Update data of currently active spawnpoint generator. This may be
     * settings or state. It is up to the generator implementation to handle
     * the data passed.
     *
     * @param nbt NBT data to be passed to the generator
     * @see SpawnPointGeneratorType#partialDecoder()
     */
    public void modifyGenerator(CompoundTag nbt) throws IllegalArgumentException {
        @SuppressWarnings("unchecked")  // T extends SpawnPointGenerator
        Consumer<SpawnPointGenerator> modifier = (Consumer<SpawnPointGenerator>) generator.getType()
                                                     .partialDecoder()
                                                     .parse(NbtOps.INSTANCE, nbt)
                                                     .getOrThrow(IllegalArgumentException::new);
        (modifier).accept(this.generator);
        this.setDirty();
    }

    /**
     * Gets the spawnpoint of a player, or generates a new one if it doesn't
     * exist yet.
     *
     * @param player the player to get the spawnpoint for.
     * @return the spawnpoint of the player
     */
    public Vector2i getSpawnPoint(ServerPlayer player) { return getSpawnPoint(player.getUUID()); }

    /**
     * Gets the spawnpoint of a player by UUID, or generates a new one if it doesn't
     * exist yet.
     *
     * @param player the UUID of the player to get the spawnpoint for.
     * @return the spawnpoint of the player
     */
    public Vector2i getSpawnPoint(UUID player) {
        try {
            return (Vector2i) playerSpawnPoints.computeIfAbsent(player, uuid -> nextSafe()).clone();
        } catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Resets the spawnpoint of a player.
     *
     * @param player the player to reset the spawnpoint of.
     * @return <code>true</code> if there was a spawnpoint associated with
     * <code>player</code>, otherwise <code>false</code>.
     * @see #resetSpawnPoints()
     */
    public boolean resetSpawnPoint(ServerPlayer player) {
        return Objects.nonNull(playerSpawnPoints.remove(player.getUUID()));
    }

    // PersistentState stuff
    /**
     * Get the SpawnPointManager instance associated with a level.
     *
     * @param level the level to get the manager instance for
     * @return SpawnPointManager instance for the specified level
     */
    public static SpawnPointManager getInstance(ServerLevel level) {
        var type = new SavedDataType<SpawnPointManager>(
            Identifier.fromNamespaceAndPath(SpreadSpawnPoints.MOD_ID, "legacy"),
            () -> new SpawnPointManager(level), getCodec(level), null);

        SpawnPointManager spawnPointManager = level.getDataStorage().computeIfAbsent(type);
        spawnPointManager.setDirty();  // mark dirty always, as per the Fabric wiki
        return spawnPointManager;
    }

    protected static Codec<SpawnPointManager> getCodec(ServerLevel level) {
        return RecordCodecBuilder.create(
            instance
            -> instance
                   .group(Codec.unboundedMap(UUIDUtil.STRING_CODEC, Vector2iExtension.CODEC)
                              .fieldOf("playerSpawnPoints")
                              .forGetter(manager -> manager.playerSpawnPoints),
                          new FlatGeneratorMapCodec(level).forGetter(manager -> manager.generator))
                   .apply(instance, (spawnPoints, generator) -> {
                       SpawnPointManager manager = new SpawnPointManager(level, generator);
                       spawnPoints.forEach((player, spawnPoint) -> {
                           manager.playerSpawnPoints.put(player, spawnPoint);
                           manager.generator.add(spawnPoint);
                       });
                       return manager;
                   }));
    }
}
