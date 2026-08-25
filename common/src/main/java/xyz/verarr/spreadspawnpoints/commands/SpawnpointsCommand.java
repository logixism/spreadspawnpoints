package xyz.verarr.spreadspawnpoints.commands;

import java.util.Collection;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import xyz.eclipseisoffline.commonpermissionsapi.api.CommonPermissions;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPointsPermissions;
import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointGenerator.SpawnPointGeneratorType;
import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointManager;

/**
 * Main collection of commands for Spread Spawnpoints
 */
public class SpawnpointsCommand {
    /**
     * Commands related to spawnpoint generators
     */
    private static class GeneratorCommand {
        /**
         * Command for querying the currently active spawnpoint generator
         *
         * @see SpawnPointManager#getSpawnPointGenerator()
         */
        private static class QueryCommand {
            /**
             * Prints current generator's identifier to command feedback.
             */
            private static int execute(CommandContext<CommandSourceStack> context) {
                final ServerLevel       level             = context.getSource().getLevel();
                final SpawnPointManager spawnPointManager = SpawnPointManager.getInstance(level);
                context.getSource().sendSuccess(
                    ()
                        -> Component.literal(
                            String.format("The spawn point generator is %s",
                                          spawnPointManager.getSpawnPointGenerator().identifier())),
                    false);
                return Command.SINGLE_SUCCESS;
            }

            /**
             * Command tree for <code>spawnpoint generator query</code> command
             */
            public static final LiteralArgumentBuilder<CommandSourceStack> command =
                Commands.literal("query")
                    .requires(
                        CommonPermissions.require(SpreadSpawnPointsPermissions.GENERATOR_QUERY,
                                                  PermissionLevel.GAMEMASTERS))
                    .executes(QueryCommand::execute);
        }

        /**
         * Command for setting (replacing) the spawnpoint generator
         *
         * @see SpawnPointManager#setSpawnPointGenerator(ResourceKey)
         */
        private static class SetCommand {
            /**
             * Sets the generator to the specified one and resets all
             * spawnpoints.
             * <p>
             * Executed when only identifier argument
             * ({@link SetCommand#identifierArgument}) is specified.
             */
            private static int execute(CommandContext<CommandSourceStack> context)
                throws CommandSyntaxException {
                try {
                    final ResourceKey<SpawnPointGeneratorType<?>> generator =
                        ResourceKeyArgument.getRegistryKey(
                            context, "generator", SpawnPointGeneratorType.REGISTRY_KEY,
                            new DynamicCommandExceptionType(
                                id
                                -> Component.literal(
                                    "Specified generator does not exist or has not been registered: "
                                    + id)));

                    if (!CommonPermissions.check(context.getSource(),
                                                 SpreadSpawnPointsPermissions.SPAWNPOINTS_RESET_ALL,
                                                 PermissionLevel.GAMEMASTERS)) {
                        throw new SimpleCommandExceptionType(
                            Component.literal(
                                "You do not have permission to reset all spawnpoints."))
                            .create();
                    }

                    final ServerLevel       serverLevel = context.getSource().getLevel();
                    final SpawnPointManager spawnPointManager =
                        SpawnPointManager.getInstance(serverLevel);
                    spawnPointManager.setSpawnPointGenerator(generator);
                    context.getSource().sendSuccess(
                        ()
                            -> Component.literal(String.format("Spawn point generator set to %s",
                                                               generator.identifier())),
                        true);
                    spawnPointManager.resetSpawnPoints();
                    return Command.SINGLE_SUCCESS;
                } catch (Exception e) {
                    throw new SimpleCommandExceptionType(Component.literal(e.toString())).create();
                }
            }

            /**
             * Sets the generator to the specified one and resets all
             * spawnpoints according to reset argument
             * ({@link SetCommand#resetArgument}).
             * <p>
             * Executed when reset argument ({@link SetCommand#resetArgument})
             * is present.
             */
            private static int executeWithResetFlag(CommandContext<CommandSourceStack> context)
                throws CommandSyntaxException {
                try {
                    final ResourceKey<SpawnPointGeneratorType<?>> generator =
                        ResourceKeyArgument.getRegistryKey(
                            context, "generator", SpawnPointGeneratorType.REGISTRY_KEY,
                            new DynamicCommandExceptionType(
                                id
                                -> Component.literal(
                                    "Specified generator does not exist or has not been registered: "
                                    + id)));

                    if (BoolArgumentType.getBool(context, "resetSpawnPoints")
                        && !CommonPermissions.check(
                            context.getSource(), SpreadSpawnPointsPermissions.SPAWNPOINTS_RESET_ALL,
                            PermissionLevel.GAMEMASTERS)) {
                        throw new SimpleCommandExceptionType(
                            Component.literal(
                                "You do not have permission to reset all spawnpoints."))
                            .create();
                    }

                    final SpawnPointManager spawnPointManager =
                        SpawnPointManager.getInstance(context.getSource().getLevel());
                    spawnPointManager.setSpawnPointGenerator(generator);
                    context.getSource().sendSuccess(
                        ()
                            -> Component.literal(String.format("Spawn point generator set to %s",
                                                               generator.identifier())),
                        true);
                    if (BoolArgumentType.getBool(context, "resetSpawnPoints"))
                        spawnPointManager.resetSpawnPoints();
                    return Command.SINGLE_SUCCESS;
                } catch (Exception e) {
                    throw new SimpleCommandExceptionType(Component.literal(e.toString())).create();
                }
            }

            /**
             * Reset boolean argument. Optional, default true. Executes
             * {@link SetCommand#executeWithResetFlag(CommandContext)} when
             * specified.
             *
             * @see SpawnPointManager#resetSpawnPoints()
             */
            private static final RequiredArgumentBuilder<CommandSourceStack, Boolean>
                resetArgument = Commands.argument("resetSpawnPoints", BoolArgumentType.bool())
                                    .executes(SetCommand::executeWithResetFlag);

            /**
             * Identifier argument. Required. Executes
             * {@link SetCommand#execute(CommandContext)} when no further
             * arguments specified.
             */
            private static final
                RequiredArgumentBuilder<CommandSourceStack, ResourceKey<SpawnPointGeneratorType<?>>>
                    identifierArgument =
                        Commands
                            .argument("generator",
                                      ResourceKeyArgument.<SpawnPointGeneratorType<?>>key(
                                          SpawnPointGeneratorType.REGISTRY.key()))
                            .suggests(new GeneratorSuggestionProvider())
                            .executes(SetCommand::execute)
                            .then(resetArgument);

            /**
             * Command tree for <code>spawnpoints generator set</code> command
             */
            public static final LiteralArgumentBuilder<CommandSourceStack> command =
                Commands.literal("set")
                    .requires(CommonPermissions.require(SpreadSpawnPointsPermissions.GENERATOR_SET,
                                                        PermissionLevel.GAMEMASTERS))
                    .then(identifierArgument);
        }

        /**
         * Command for modifying the data of a spawnpoint generator
         *
         * @see SpawnPointGeneratorType#partialDecoder()
         */
        private static class DataCommand {
            /**
             * @see SpawnPointGeneratorType#partialDecoder()
             */
            private static int execute(CommandContext<CommandSourceStack> context)
                throws CommandSyntaxException {
                final CompoundTag nbt =
                    NbtTagArgument.getNbtTag(context, "nbt")
                        .asCompound()
                        .orElseThrow(()
                                         -> new SimpleCommandExceptionType(
                                                Component.literal("Data must be a Compound tag."))
                                                .create());
                final SpawnPointManager spawnPointManager =
                    SpawnPointManager.getInstance(context.getSource().getLevel());
                try {
                    spawnPointManager.modifyGenerator(nbt);
                } catch (IllegalArgumentException e) {
                    throw new SimpleCommandExceptionType(
                        Component.literal("Illegal data passed to generator: " + e.getMessage()))
                        .create();
                }
                context.getSource().sendSuccess(()
                                                    -> Component.literal("Data modified. ("
                                                                         + nbt.keySet().size()
                                                                         + " keys updated)"),
                                                true);
                return Command.SINGLE_SUCCESS;
            }

            /**
             * Passed data argument
             */
            private static final RequiredArgumentBuilder<CommandSourceStack, Tag> argumentBuilder =
                Commands.argument("nbt", NbtTagArgument.nbtTag()).executes(DataCommand::execute);

            /**
             * Command tree for <code>spawnpoints generator data</code> command
             */
            public static final LiteralArgumentBuilder<CommandSourceStack> command =
                Commands.literal("data")
                    .requires(
                        CommonPermissions.require(SpreadSpawnPointsPermissions.GENERATOR_MODIFY,
                                                  PermissionLevel.GAMEMASTERS))
                    .then(argumentBuilder);
        }

        /**
         * Command tree for <code>spawnpoints generator</code> command
         */
        public static LiteralArgumentBuilder<CommandSourceStack> command =
            Commands.literal("generator")
                .requires(CommonPermissions.require(SpreadSpawnPointsPermissions.GENERATOR,
                                                    PermissionLevel.GAMEMASTERS))
                .then(QueryCommand.command)
                .then(SetCommand.command)
                .then(DataCommand.command);
    }

    /**
     * Command for resetting all spawnpoints
     * ({@link ResetCommand#execute(CommandContext)}) or spawnpoints of
     * a specified player
     * ({@link ResetCommand#executeWithArgument(CommandContext)}).
     */
    private static class ResetCommand {
        /**
         * Executed when no argument is specified.
         */
        private static int execute(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
            if (!CommonPermissions.check(context.getSource(),
                                         SpreadSpawnPointsPermissions.SPAWNPOINTS_RESET_ALL,
                                         PermissionLevel.GAMEMASTERS)) {
                throw new SimpleCommandExceptionType(
                    Component.literal("You do not have permission to reset all spawnpoints."))
                    .create();
            }

            final ServerLevel       level             = context.getSource().getLevel();
            final SpawnPointManager spawnPointManager = SpawnPointManager.getInstance(level);
            spawnPointManager.resetSpawnPoints();
            context.getSource().sendSuccess(
                () -> Component.literal("Reset all spawn points."), true);
            return Command.SINGLE_SUCCESS;
        }

        /**
         * Executed when {@link #argumentBuilder} is specified.
         */
        private static int executeWithArgument(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
            final ServerLevel       level             = context.getSource().getLevel();
            final SpawnPointManager spawnPointManager = SpawnPointManager.getInstance(level);
            final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");
            int                            affected =
                (int) players.stream().filter(spawnPointManager::resetSpawnPoint).count();
            if (affected > 0) {
                context.getSource().sendSuccess(
                    () -> Component.literal("Reset %d spawn points.".formatted(affected)), true);
                return affected;
            } else {
                throw new SimpleCommandExceptionType(
                    Component.literal("No specified player already has a "
                                      + "spawnpoint, nothing was affected."))
                    .create();
            }
        }

        /**
         * Reset target argument. When specified, executes
         * {@link ResetCommand#executeWithArgument(CommandContext)}
         *
         * @see SpawnPointManager#resetSpawnPoint(ServerPlayer)
         */
        private static final RequiredArgumentBuilder<CommandSourceStack, EntitySelector>
                             argumentBuilder = Commands.argument("target", EntityArgument.players())
                                  .executes(ResetCommand::executeWithArgument);

        /**
         * Command tree for <code>spawnpoints reset</code> command
         * @see SpawnPointManager#resetSpawnPoints()
         */
        public static LiteralArgumentBuilder<CommandSourceStack> command =
            Commands.literal("reset")
                .requires(CommonPermissions.require(SpreadSpawnPointsPermissions.SPAWNPOINTS_RESET,
                                                    PermissionLevel.GAMEMASTERS))
                .executes(ResetCommand::execute)
                .then(argumentBuilder);
    }

    /**
     * Full command tree for <code>spawnpoints</code> command
     */
    public static final LiteralArgumentBuilder<CommandSourceStack> command =
        Commands.literal("spawnpoints")
            .requires(CommonPermissions.require(SpreadSpawnPointsPermissions.SPAWNPOINTS,
                                                PermissionLevel.GAMEMASTERS))
            .then(GeneratorCommand.command)
            .then(ResetCommand.command);
}
