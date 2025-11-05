package mods.hexagonal.ar2;

import mods.hexagonal.ar2.fluids.CustomFluidType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier; 

public class Registry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ar2.MOD_ID);

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(new ResourceLocation("forge", "fluid_type"), ar2.MOD_ID);

    public static final Supplier<SimpleParticleType> ROCKET_FLAME = PARTICLES.register("rocketflame",() -> new SimpleParticleType(true));

    // Rocket Fuel Fluid Types
    public static final RegistryObject<FluidType> ROCKET_FUEL_TYPE = FLUID_TYPES.register("rocket_fuel",
            () -> new CustomFluidType(FluidType.Properties.create()
                    .density(800)
                    .viscosity(1000)
                    .temperature(290)));

    public static final RegistryObject<FluidType> ADVANCED_ROCKET_FUEL_TYPE = FLUID_TYPES.register("advanced_rocket_fuel",
            () -> new CustomFluidType(FluidType.Properties.create()
                    .density(850)
                    .viscosity(1200)
                    .temperature(295)));

    public static final RegistryObject<FluidType> BIPROPELLANT_ROCKET_FUEL_TYPE = FLUID_TYPES.register("bipropellant_rocket_fuel",
            () -> new CustomFluidType(FluidType.Properties.create()
                    .density(900)
                    .viscosity(1400)
                    .temperature(300)));

    public static final RegistryObject<FluidType> NUCLEAR_ROCKET_FUEL_TYPE = FLUID_TYPES.register("nuclear_rocket_fuel",
            () -> new CustomFluidType(FluidType.Properties.create()
                    .density(950)
                    .viscosity(1600)
                    .temperature(400)));

}
