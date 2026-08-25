package xyz.verarr.spreadspawnpoints;

import net.minecraft.resources.Identifier;

import xyz.eclipseisoffline.commonpermissionsapi.api.CommonPermissionNode;
import xyz.eclipseisoffline.commonpermissionsapi.api.CommonPermissions;

public interface SpreadSpawnPointsPermissions {
    private static CommonPermissionNode node(String path) {
        return CommonPermissions.node(
            Identifier.fromNamespaceAndPath(SpreadSpawnPoints.MOD_ID, path));
    }

    private static CommonPermissionNode extend(CommonPermissionNode parent, String path) {
        return CommonPermissions.node(parent.identifier().withSuffix("." + path));
    }

    CommonPermissionNode COMMAND_ROOT = node("command");

    CommonPermissionNode SPAWNPOINTS = extend(COMMAND_ROOT, "spawnpoints");

    CommonPermissionNode GENERATOR        = extend(SPAWNPOINTS, "generator");
    CommonPermissionNode GENERATOR_SET    = extend(GENERATOR, "set");
    CommonPermissionNode GENERATOR_MODIFY = extend(GENERATOR, "modify");
    CommonPermissionNode GENERATOR_QUERY  = extend(GENERATOR, "query");

    CommonPermissionNode SPAWNPOINTS_RESET     = extend(SPAWNPOINTS, "reset");
    CommonPermissionNode SPAWNPOINTS_RESET_ALL = extend(SPAWNPOINTS_RESET, "all");

    CommonPermissionNode RESPAWN        = extend(COMMAND_ROOT, "respawn");
    CommonPermissionNode RESPAWN_SELF   = extend(RESPAWN, "self");
    CommonPermissionNode RESPAWN_OTHERS = extend(RESPAWN, "others");
}
