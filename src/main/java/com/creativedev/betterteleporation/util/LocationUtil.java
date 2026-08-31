package com.creativedev.betterteleporation.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static boolean isSafeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (y < world.getMinHeight() || y >= world.getMaxHeight() - 1) {
            return false;
        }

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        if (isHazardous(feet.getType()) || isHazardous(head.getType()) || isHazardous(ground.getType())) {
            return false;
        }

        if (isSuffocating(feet) || isSuffocating(head)) {
            return false;
        }

        return true;
    }

    private static boolean isSuffocating(Block block) {
        Material type = block.getType();
        return block.getType().isSolid() && block.getType().isOccluding() && !block.isPassable() && type != Material.COBWEB;
    }

    private static boolean isHazardous(Material material) {
        return material == Material.LAVA
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.MAGMA_BLOCK
                || material == Material.CACTUS
                || material == Material.SWEET_BERRY_BUSH
                || material == Material.WITHER_ROSE;
    }
}
