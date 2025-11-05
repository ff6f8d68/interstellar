package mods.hexagonal.ar2.ships;

import org.valkyrienskies.core.impl.hooks.VSEvents;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = "manouver")
public class OnShipLoad {
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		VSEvents.ShipLoadEvent.Companion.on((shipLoadEvent) -> {
			ForceInducedShips.getOrCreate(shipLoadEvent.getShip());
			GravityInducedShips.getOrCreate(shipLoadEvent.getShip());
		});
	}
}
