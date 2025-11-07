package mods.hexagonal.ar2.ships;

import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.impl.shadow.id;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.apigame.physics.PhysicsEntityServer;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.apigame.ShipTeleportData;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import org.joml.Vector3dc;
import org.joml.Vector3d;
import org.joml.Quaterniond;

import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

// This class is straight up yoinked from starlance, I should probably simplify it at some point
public class TeleportHandler {

	private static final Map<Long, Set<Integer>> shipIdToConstraints = ((mods.hexagonal.ar2.mixins.ServerShipObjectWorldAccessor) VSGameUtilsKt.getShipObjectWorld(ValkyrienSkiesMod.getCurrentServer())).getShipIdToConstraints();
	private static final Map<Integer, VSConstraint> constraintIdToConstraint = ((mods.hexagonal.ar2.mixins.ServerShipObjectWorldAccessor) VSGameUtilsKt.getShipObjectWorld(ValkyrienSkiesMod.getCurrentServer())).getConstraints();

	private final Map<Long, Vector3d> shipToPos = new HashMap<>();
	private final ServerShipWorldCore shipWorld;
	private double greatestOffset;
	private final ServerLevel newDim;
	private final ServerLevel originalDim;
	private final boolean highestShipPriority;

	public TeleportHandler(ServerLevel newDim, ServerLevel originalDim, boolean highestShipPriority) {
		shipWorld = VSGameUtilsKt.getShipObjectWorld(newDim);
		this.newDim = newDim;
		this.originalDim = originalDim;
		this.highestShipPriority = highestShipPriority;
	}

	public void handleTeleport(Ship ship, Vector3d newPos) {
		// The user has done a woopsie, transform to world
		if (VSGameUtilsKt.isBlockInShipyard(this.newDim, newPos.x, newPos.y, newPos.z)) {
			// If the position we teleport to is in the shipyard, we stack overflow
			Ship posShip = VSGameUtilsKt.getShipManagingPos(this.newDim, newPos);
			if (posShip != null) {
				newPos = posShip.getTransform().getShipToWorld().transformPosition(newPos);
			}
		}
		collectShips(ship, newPos);
		handleTeleport();
	}

	private void collectConnected(Long currentPhysObject, Vector3dc origin, Vector3d newPos) {
		if (currentPhysObject == null)
			return;
		if (shipToPos.containsKey(currentPhysObject))
			return;
		Set<Integer> constraints = shipIdToConstraints.get(currentPhysObject);
		Vector3dc pos = transformFromId(currentPhysObject, shipWorld).getPositionInWorld();
		double offset = pos.get(1) - origin.get(1);
		offset *= highestShipPriority ? 1 : -1;
		if (offset > greatestOffset)
			greatestOffset = offset;
		shipToPos.put(currentPhysObject, pos.sub(origin, new Vector3d()).add(newPos, new Vector3d()));
		if (constraints != null) {
			constraints.iterator().forEachRemaining(id -> {
				VSConstraint constraint = constraintIdToConstraint.get(id);
				collectConnected(constraint.getShipId0(), origin, newPos);
				collectConnected(constraint.getShipId1(), origin, newPos);
			});
		}
	}

	private void collectShips(Ship ship, Vector3d newPos) {
		Vector3dc origin = ship.getTransform().getPositionInWorld();
		collectConnected(ship.getId(), origin, newPos);
		collectNearby(origin, newPos);
	}

	private void collectNearby(Vector3dc origin, Vector3d newPos) {
		Map<Long, Vector3d> newShipToPos = new HashMap<>();
		shipToPos.keySet().forEach(id -> {
			if (shipToPos.containsKey(id))
				return;
			QueryableShipData<LoadedServerShip> loadedShips = shipWorld.getLoadedShips();
			Ship ship = loadedShips.getById(id);
			if (ship == null)
				return;
			loadedShips.getIntersecting(VectorConversionsMCKt.toJOML(VectorConversionsMCKt.toMinecraft(ship.getWorldAABB()).inflate(10)))
					.forEach(intersecting -> newShipToPos.put(intersecting.getId(), intersecting.getTransform().getPositionInWorld().sub(origin, new Vector3d()).add(newPos, new Vector3d())));
		});
		shipToPos.putAll(newShipToPos);
	}

	private void handleTeleport() {
		greatestOffset *= highestShipPriority ? -1 : 1;
		shipToPos.forEach((id, newPos) -> {
			dismountEntities(id);
			handleShipTeleport(id, newPos);
		});
	}

	private void dismountEntities(Long id) {
		ServerShip ship = shipWorld.getLoadedShips().getById(id);
		if (ship == null)
			return;
		AABB inflatedAABB = VectorConversionsMCKt.toMinecraft(ship.getWorldAABB()).inflate(20);
		originalDim.getEntities(null, inflatedAABB).forEach((entity) -> {
			entity.dismountTo(entity.getX(), entity.getY(), entity.getZ());
		});
	}

	private void handleShipTeleport(Long id, Vector3d newPos) {
		String vsDimName = ((DimensionIdProvider) newDim).getDimensionId();
		ServerShip ship = shipWorld.getLoadedShips().getById(id);
		if (ship == null) {
			PhysicsEntityServer physEntity = ((ShipObjectServerWorld) shipWorld).getLoadedPhysicsEntities().get(id);
			if (physEntity == null) {
				return;
			}
			ShipTeleportData teleportData = new ShipTeleportDataImpl(newPos.add(0, greatestOffset, 0), physEntity.getShipTransform().getShipToWorldRotation(), new Vector3d(), new Vector3d(), vsDimName, null);
			shipWorld.teleportPhysicsEntity(physEntity, teleportData);
		}
		ShipTeleportData teleportData = new ShipTeleportDataImpl(newPos.add(0, greatestOffset, 0), ship.getTransform().getShipToWorldRotation(), new Vector3d(), new Vector3d(), vsDimName, null);
		shipWorld.teleportShip(ship, teleportData);
	}

	private static ShipTransform transformFromId(Long id, ServerShipWorldCore shipWorld) {
		Ship ship = shipWorld.getAllShips().getById(id);
		if (ship == null) {
			PhysicsEntityServer physicsEntity = ((ShipObjectServerWorld) shipWorld).getLoadedPhysicsEntities().get(id);
			if (physicsEntity == null)
				return new ShipTransformImpl(new Vector3d(), new Vector3d(), new Quaterniond(), new Vector3d());
			return physicsEntity.getShipTransform();
		}
		return ship.getTransform();
	}

	public static ServerLevel dimToLevel(String dimensionString) {
		MinecraftServer server = ValkyrienSkiesMod.getCurrentServer();
		if (server == null)
			return null;
		return server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionString)));
	}

	public static Vector3d toDegrees(Vector3d radianVec) {
		double pitchDeg = Math.toDegrees(radianVec.x);
		double yawDeg = Math.toDegrees(radianVec.y);
		double rollDeg = Math.toDegrees(radianVec.z);
		return new Vector3d(pitchDeg, yawDeg, rollDeg);
	}
}
