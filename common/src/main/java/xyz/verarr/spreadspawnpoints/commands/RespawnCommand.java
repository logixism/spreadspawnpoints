package xyz.verarr.spreadspawnpoints.commands;

import java.util.Collection;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import xyz.eclipseisoffline.commonpermissionsapi.api.CommonPermissions;
import xyz.verarr.spreadspawnpoints.ServerPlayerExtension;
import xyz.verarr.spreadspawnpoints.SpreadSpawnPointsPermissions;

/**
 * Command for moving players to their spawns
 */
public class RespawnCommand {
    /**
     * Moves specified players to their spawnpoint and teleports them in
     * place (so the players' position is actually sent to the clients).
     */
    private static int execute(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException {
        final Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");

        ServerPlayer sourcePlayer = context.getSource().getPlayer();
        if (sourcePlayer != null) {
            boolean permissionSelf =
                CommonPermissions.check(sourcePlayer, SpreadSpawnPointsPermissions.RESPAWN_SELF,
                                        PermissionLevel.GAMEMASTERS);
            boolean permissionOthers =
                CommonPermissions.check(sourcePlayer, SpreadSpawnPointsPermissions.RESPAWN_OTHERS,
                                        PermissionLevel.GAMEMASTERS);
            if (!permissionSelf && players.contains(sourcePlayer))
                throw new SimpleCommandExceptionType(
                    Component.literal("You do not have permission to respawn yourself."))
                    .create();
            if (!permissionOthers
                && players.stream().anyMatch(player -> !player.equals(sourcePlayer)))
                throw new SimpleCommandExceptionType(
                    Component.literal("You do not have permission to respawn other players."))
                    .create();
        } else if (!CommonPermissions.check(context.getSource(),
                                            SpreadSpawnPointsPermissions.RESPAWN_OTHERS,
                                            PermissionLevel.GAMEMASTERS))
            throw new SimpleCommandExceptionType(
                Component.literal("Source does not have permission to use this command."))
                .create();

        players.forEach(ServerPlayerExtension::moveToSpawn);
        context.getSource().sendSuccess(
            () -> Component.literal(String.format("Respawned %d players", players.size())), true);
        return 1;
    }

    /**
     * Target selector argument.
     */
    private static final RequiredArgumentBuilder<CommandSourceStack, EntitySelector>
                         argumentBuilder =
            Commands.argument("target", EntityArgument.players()).executes(RespawnCommand::execute);

    /**
     * Full command tree for <code>respawn</code> command.
     * Executes {@link RespawnCommand#execute(CommandContext)}.
     */
    public static final LiteralArgumentBuilder<CommandSourceStack> command =
        Commands.literal("respawn")
            .requires(CommonPermissions.require(SpreadSpawnPointsPermissions.RESPAWN,
                                                PermissionLevel.GAMEMASTERS))
            .then(argumentBuilder);
}
