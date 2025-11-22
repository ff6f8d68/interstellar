package team.nextlevelmodding.ar2.ships;

import org.valkyrienskies.mod.util.RelocationUtilKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ServerShip;

import org.joml.Vector3i;
import org.joml.Vector3d;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class ShipMaker {
    public static Ship assembleShip(ServerLevel level, BlockPos pos, double scale) {
        if (!level.getBlockState(pos).isAir()) {
            ServerShip parentShip = VSGameUtilsKt.getShipManagingPos(level, pos);
            ServerShip serverShip = VSGameUtilsKt.getShipObjectWorld(level).createNewShipAtBlock(VectorConversionsMCKt.toJOML(pos), false, scale, VSGameUtilsKt.getDimensionId(level));
            Vector3i centerVec = new Vector3i();
            serverShip.getChunkClaim().getCenterBlockCoordinates(VSGameUtilsKt.getYRange(level), centerVec);
            BlockPos centerPos = VectorConversionsMCKt.toBlockPos(centerVec);
            // Move the block from the world to a ship
            RelocationUtilKt.relocateBlock(level, pos, centerPos, true, serverShip, Rotation.NONE);
            if (parentShip != null) {
                // Compute the ship transform
                var newShipPosInWorld = parentShip.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOMLD(pos).add(0.5, 0.5, 0.5));
                var newShipPosInShipyard = VectorConversionsMCKt.toJOMLD(pos).add(0.5, 0.5, 0.5);
                var newShipRotation = parentShip.getTransform().getShipToWorldRotation();
                var shipTransform = new ShipTransformImpl(newShipPosInWorld, newShipPosInShipyard, newShipRotation, new Vector3d(scale));
                ((org.valkyrienskies.core.impl.game.ships.ShipDataCommon) serverShip).setTransform(shipTransform);
            }
            return serverShip;
        } else {
            return null;
        }
    }
}