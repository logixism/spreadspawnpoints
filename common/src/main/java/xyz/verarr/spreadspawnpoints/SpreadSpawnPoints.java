package xyz.verarr.spreadspawnpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.verarr.spreadspawnpoints.spawnpoints.generators.SpawnPointGenerators;

public abstract class SpreadSpawnPoints {
    public static final String MOD_ID = "spreadspawnpoints";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    protected void init() {
        LOGGER.info("Hello Minecraft modding world!");

        SpawnPointGenerators.init();
        LOGGER.info("Registered Spawn Point Generators!");

        initCommands();
    }

    protected abstract void initCommands();
}
