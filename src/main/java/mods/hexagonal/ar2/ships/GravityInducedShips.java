package mods.hexagonal.ar2.ships;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;

import org.joml.Vector3d;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class GravityInducedShips implements ShipForcesInducer {
	private volatile double gravity = 1;

	public GravityInducedShips() {
	}

	@Override
	public void applyForces(@NotNull PhysShip physicShip) {
		PhysShipImpl physShip = (PhysShipImpl) physicShip;
		double shipGravity = (1 - this.gravity) * 10 * physShip.get_inertia().getShipMass();
		physShip.applyInvariantForce(new Vector3d(0, shipGravity, 0));
	}

	public void setGravity(Number newGrav) {
		gravity = newGrav.doubleValue();
	}

	public Number getGravity() {
		return gravity;
	}

	public static GravityInducedShips getOrCreate(ServerShip ship) {
		GravityInducedShips attachment = ship.getAttachment(GravityInducedShips.class);
		if (attachment == null) {
			attachment = new GravityInducedShips();
			ship.saveAttachment(GravityInducedShips.class, attachment);
		}
		return attachment;
	}

	public static GravityInducedShips get(Level level, BlockPos pos) {
		ServerLevel serverLevel = (ServerLevel) level;
		ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pos);
		return ship != null ? getOrCreate(ship) : null;
	}
}
