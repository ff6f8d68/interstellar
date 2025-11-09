package mods.hexagonal.ar2.fluids;

import org.joml.Vector3f;
import mods.hexagonal.ar2.ar2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluidTypes {
    public static final ResourceLocation WATER_STILL_RL = new ResourceLocation("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = new ResourceLocation("block/water_flow");
    public static final ResourceLocation ROCKET_FUEL_OVERLAY_RL = new ResourceLocation(ar2.MOD_ID, "misc/in_rocket_fuel");
    public static final ResourceLocation ADVANCED_ROCKET_FUEL_OVERLAY_RL = new ResourceLocation(ar2.MOD_ID, "misc/in_advanced_rocket_fuel");
    public static final ResourceLocation BIPROPELLANT_ROCKET_FUEL_OVERLAY_RL = new ResourceLocation(ar2.MOD_ID, "misc/in_bipropellant_rocket_fuel");
    public static final ResourceLocation NUCLEAR_ROCKET_FUEL_OVERLAY_RL = new ResourceLocation(ar2.MOD_ID, "misc/in_nuclear_rocket_fuel");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ar2.MOD_ID);

    public static final RegistryObject<FluidType> ROCKET_FUEL_FLUID_TYPE = register("rocket_fuel_fluid",
            FluidType.Properties.create().lightLevel(4).density(20).viscosity(10).sound(SoundAction.get("drink"),
                    SoundEvents.HONEY_DRINK), ROCKET_FUEL_OVERLAY_RL,
            0xFFFF8C00, new Vector3f(255f / 255f, 140f / 255f, 0f / 255f));

    public static final RegistryObject<FluidType> ADVANCED_ROCKET_FUEL_FLUID_TYPE = register("advanced_rocket_fuel_fluid",
            FluidType.Properties.create().lightLevel(5).density(22).viscosity(12).sound(SoundAction.get("drink"),
                    SoundEvents.HONEY_DRINK), ADVANCED_ROCKET_FUEL_OVERLAY_RL,
            0xFFCC8833, new Vector3f(204f / 255f, 136f / 255f, 51f / 255f));

    public static final RegistryObject<FluidType> BIPROPELLANT_ROCKET_FUEL_FLUID_TYPE = register("bipropellant_rocket_fuel_fluid",
            FluidType.Properties.create().lightLevel(3).density(18).viscosity(8).sound(SoundAction.get("drink"),
                    SoundEvents.HONEY_DRINK), BIPROPELLANT_ROCKET_FUEL_OVERLAY_RL,
            0xFFE6AA55, new Vector3f(230f / 255f, 170f / 255f, 85f / 255f));

    public static final RegistryObject<FluidType> NUCLEAR_ROCKET_FUEL_FLUID_TYPE = register("nuclear_rocket_fuel_fluid",
            FluidType.Properties.create().lightLevel(8).density(25).viscosity(15).sound(SoundAction.get("drink"),
                    SoundEvents.HONEY_DRINK), NUCLEAR_ROCKET_FUEL_OVERLAY_RL,
            0xFF00FF00, new Vector3f(0f / 255f, 255f / 255f, 0f / 255f));


    private static RegistryObject<FluidType> register(String name, FluidType.Properties properties,
            ResourceLocation overlayRL, int color, Vector3f colorVector) {
        return FLUID_TYPES.register(name, () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, overlayRL,
                color, colorVector, properties));
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}