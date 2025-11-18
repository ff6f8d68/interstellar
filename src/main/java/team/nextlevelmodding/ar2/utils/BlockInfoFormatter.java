package team.nextlevelmodding.ar2.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * Utility for formatting block information in a readable way
 * Format: BlockType (x, y, z) or BlockType (x, y, z, ship_slug)
 */
public class BlockInfoFormatter {

    /**
     * Get a formatted string for a block at a position
     * Shows block name, coordinates, and ship slug if on a ship
     */
    public static String formatBlockInfo(Level level, BlockPos pos) {
        if (level == null) {
            return "Unknown (?,?,?)";
        }

        // Get block type
        String blockType = getBlockTypeName(level, pos);

        // Get coordinates
        String coords = String.format("(%d, %d, %d", pos.getX(), pos.getY(), pos.getZ());

        // Check if on a ship
        String shipInfo = getShipInfo(level, pos);
        if (shipInfo != null && !shipInfo.isEmpty()) {
            coords += ", " + shipInfo;
        }

        coords += ")";

        return blockType + " " + coords;
    }

    /**
     * Get the friendly block type name
     */
    private static String getBlockTypeName(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        String blockName = block.getName().getString();

        // Clean up the name if needed
        if (blockName == null || blockName.isEmpty()) {
            blockName = block.getClass().getSimpleName();
        }

        return blockName;
    }

    /**
     * Get ship slug if the block is on a VS ship
     * Returns null if not on a ship
     */
    private static String getShipInfo(Level level, BlockPos pos) {
        try {
            if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
                return null;
            }

            Ship ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pos);
            if (ship != null) {
                return ship.getSlug();
            }
        } catch (Exception e) {
            // If we can't access VS data, just return null
        }

        return null;
    }

    /**
     * Get just the ship slug if block is on a ship
     */
    public static String getShipSlug(Level level, BlockPos pos) {
        return getShipInfo(level, pos);
    }

    /**
     * Check if a block is on a VS ship
     */
    public static boolean isOnShip(Level level, BlockPos pos) {
        return getShipInfo(level, pos) != null;
    }
}