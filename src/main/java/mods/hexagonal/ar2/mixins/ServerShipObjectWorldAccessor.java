package mods.hexagonal.ar2.mixins;

import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;

import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;
import java.util.Map;

/**@deprecated this is sus we should change it*/
@Deprecated
@Mixin(ShipObjectServerWorld.class)
public interface ServerShipObjectWorldAccessor {
	@Accessor(value = "shipIdToConstraints", remap = false)
	Map<Long, Set<Integer>> getShipIdToConstraints();

	@Accessor(value = "constraints", remap = false)
	Map<Integer, VSConstraint> getConstraints();
}
