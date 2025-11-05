package mods.hexagonal.ar2;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Fluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, ar2.MOD_ID);

    // Rocket Fuel
    public static final RegistryObject<ForgeFlowingFluid.Source> ROCKET_FUEL =
            FLUIDS.register("rocket_fuel",
                    () -> new ForgeFlowingFluid.Source(getRocketFuelProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> ROCKET_FUEL_FLOWING =
            FLUIDS.register("rocket_fuel_flowing",
                    () -> new ForgeFlowingFluid.Flowing(getRocketFuelFlowingProperties()));

    // Advanced Rocket Fuel
    public static final RegistryObject<ForgeFlowingFluid.Source> ADVANCED_ROCKET_FUEL =
            FLUIDS.register("advanced_rocket_fuel",
                    () -> new ForgeFlowingFluid.Source(getAdvancedRocketFuelProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> ADVANCED_ROCKET_FUEL_FLOWING =
            FLUIDS.register("advanced_rocket_fuel_flowing",
                    () -> new ForgeFlowingFluid.Flowing(getAdvancedRocketFuelFlowingProperties()));

    // Bipropellant Rocket Fuel
    public static final RegistryObject<ForgeFlowingFluid.Source> BIPROPELLANT_ROCKET_FUEL =
            FLUIDS.register("bipropellant_rocket_fuel",
                    () -> new ForgeFlowingFluid.Source(getBipropellantRocketFuelProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> BIPROPELLANT_ROCKET_FUEL_FLOWING =
            FLUIDS.register("bipropellant_rocket_fuel_flowing",
                    () -> new ForgeFlowingFluid.Flowing(getBipropellantRocketFuelFlowingProperties()));

    // Nuclear Rocket Fuel
    public static final RegistryObject<ForgeFlowingFluid.Source> NUCLEAR_ROCKET_FUEL =
            FLUIDS.register("nuclear_rocket_fuel",
                    () -> new ForgeFlowingFluid.Source(getNuclearRocketFuelProperties()));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> NUCLEAR_ROCKET_FUEL_FLOWING =
            FLUIDS.register("nuclear_rocket_fuel_flowing",
                    () -> new ForgeFlowingFluid.Flowing(getNuclearRocketFuelFlowingProperties()));

    // ===== Properties Methods =====

    public static ForgeFlowingFluid.Properties getRocketFuelProperties() {
        return new ForgeFlowingFluid.Properties(
                Registry.ROCKET_FUEL_TYPE,
                ROCKET_FUEL,
                ROCKET_FUEL_FLOWING
        ).bucket(ModBlocks.ROCKET_FUEL_BUCKET).block(() -> (LiquidBlock) ModBlocks.ROCKET_FUEL_BLOCK.get());
    }

    public static ForgeFlowingFluid.Properties getRocketFuelFlowingProperties() {
        return getRocketFuelProperties();
    }

    public static ForgeFlowingFluid.Properties getAdvancedRocketFuelProperties() {
        return new ForgeFlowingFluid.Properties(
                Registry.ADVANCED_ROCKET_FUEL_TYPE,
                ADVANCED_ROCKET_FUEL,
                ADVANCED_ROCKET_FUEL_FLOWING
        ).bucket(ModBlocks.ADVANCED_ROCKET_FUEL_BUCKET).block(() -> (LiquidBlock) ModBlocks.ADVANCED_ROCKET_FUEL_BLOCK.get());
    }

    public static ForgeFlowingFluid.Properties getAdvancedRocketFuelFlowingProperties() {
        return getAdvancedRocketFuelProperties();
    }

    public static ForgeFlowingFluid.Properties getBipropellantRocketFuelProperties() {
        return new ForgeFlowingFluid.Properties(
                Registry.BIPROPELLANT_ROCKET_FUEL_TYPE,
                BIPROPELLANT_ROCKET_FUEL,
                BIPROPELLANT_ROCKET_FUEL_FLOWING
        ).bucket(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET).block(() -> (LiquidBlock) ModBlocks.BIPROPELLANT_ROCKET_FUEL_BLOCK.get());
    }

    public static ForgeFlowingFluid.Properties getBipropellantRocketFuelFlowingProperties() {
        return getBipropellantRocketFuelProperties();
    }

    public static ForgeFlowingFluid.Properties getNuclearRocketFuelProperties() {
        return new ForgeFlowingFluid.Properties(
                Registry.NUCLEAR_ROCKET_FUEL_TYPE,
                NUCLEAR_ROCKET_FUEL,
                NUCLEAR_ROCKET_FUEL_FLOWING
        ).bucket(ModBlocks.NUCLEAR_ROCKET_FUEL_BUCKET).block(() -> (LiquidBlock) ModBlocks.NUCLEAR_ROCKET_FUEL_BLOCK.get());
    }

    public static ForgeFlowingFluid.Properties getNuclearRocketFuelFlowingProperties() {
        return getNuclearRocketFuelProperties();
    }
}