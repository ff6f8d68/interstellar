package mods.hexagonal.ar2;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Fluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, ar2.MOD_ID);

    // ===============================================================
    // ROCKET FUEL
    // ===============================================================
    public static RegistryObject<ForgeFlowingFluid.Source> ROCKET_FUEL;
    public static RegistryObject<ForgeFlowingFluid.Flowing> ROCKET_FUEL_FLOWING;
    public static Supplier<ForgeFlowingFluid.Properties> ROCKET_FUEL_PROPS;

    // ===============================================================
    // ADVANCED ROCKET FUEL
    // ===============================================================
    public static RegistryObject<ForgeFlowingFluid.Source> ADVANCED_ROCKET_FUEL;
    public static RegistryObject<ForgeFlowingFluid.Flowing> ADVANCED_ROCKET_FUEL_FLOWING;
    public static Supplier<ForgeFlowingFluid.Properties> ADVANCED_ROCKET_FUEL_PROPS;

    // ===============================================================
    // BIPROPELLANT ROCKET FUEL
    // ===============================================================
    public static RegistryObject<ForgeFlowingFluid.Source> BIPROPELLANT_ROCKET_FUEL;
    public static RegistryObject<ForgeFlowingFluid.Flowing> BIPROPELLANT_ROCKET_FUEL_FLOWING;
    public static Supplier<ForgeFlowingFluid.Properties> BIPROPELLANT_ROCKET_FUEL_PROPS;

    // ===============================================================
    // NUCLEAR ROCKET FUEL
    // ===============================================================
    public static RegistryObject<ForgeFlowingFluid.Source> NUCLEAR_ROCKET_FUEL;
    public static RegistryObject<ForgeFlowingFluid.Flowing> NUCLEAR_ROCKET_FUEL_FLOWING;
    public static Supplier<ForgeFlowingFluid.Properties> NUCLEAR_ROCKET_FUEL_PROPS;

    static {
        // -------------------------
        // ROCKET FUEL
        // -------------------------
        ROCKET_FUEL = FLUIDS.register("rocket_fuel",
                () -> new ForgeFlowingFluid.Source(ROCKET_FUEL_PROPS));
        ROCKET_FUEL_FLOWING = FLUIDS.register("rocket_fuel_flowing",
                () -> new ForgeFlowingFluid.Flowing(ROCKET_FUEL_PROPS));
        ROCKET_FUEL_PROPS = () -> new ForgeFlowingFluid.Properties(
                Registry.ROCKET_FUEL_TYPE,
                ROCKET_FUEL,
                ROCKET_FUEL_FLOWING
        )
                .bucket(ModBlocks.ROCKET_FUEL_BUCKET)
                .block(() -> (LiquidBlock) ModBlocks.ROCKET_FUEL_BLOCK.get());

        // -------------------------
        // ADVANCED ROCKET FUEL
        // -------------------------
        ADVANCED_ROCKET_FUEL = FLUIDS.register("advanced_rocket_fuel",
                () -> new ForgeFlowingFluid.Source(ADVANCED_ROCKET_FUEL_PROPS));
        ADVANCED_ROCKET_FUEL_FLOWING = FLUIDS.register("advanced_rocket_fuel_flowing",
                () -> new ForgeFlowingFluid.Flowing(ADVANCED_ROCKET_FUEL_PROPS));
        ADVANCED_ROCKET_FUEL_PROPS = () -> new ForgeFlowingFluid.Properties(
                Registry.ADVANCED_ROCKET_FUEL_TYPE,
                ADVANCED_ROCKET_FUEL,
                ADVANCED_ROCKET_FUEL_FLOWING
        )
                .bucket(ModBlocks.ADVANCED_ROCKET_FUEL_BUCKET)
                .block(() -> (LiquidBlock) ModBlocks.ADVANCED_ROCKET_FUEL_BLOCK.get());

        // -------------------------
        // BIPROPELLANT ROCKET FUEL
        // -------------------------
        BIPROPELLANT_ROCKET_FUEL = FLUIDS.register("bipropellant_rocket_fuel",
                () -> new ForgeFlowingFluid.Source(BIPROPELLANT_ROCKET_FUEL_PROPS));
        BIPROPELLANT_ROCKET_FUEL_FLOWING = FLUIDS.register("bipropellant_rocket_fuel_flowing",
                () -> new ForgeFlowingFluid.Flowing(BIPROPELLANT_ROCKET_FUEL_PROPS));
        BIPROPELLANT_ROCKET_FUEL_PROPS = () -> new ForgeFlowingFluid.Properties(
                Registry.BIPROPELLANT_ROCKET_FUEL_TYPE,
                BIPROPELLANT_ROCKET_FUEL,
                BIPROPELLANT_ROCKET_FUEL_FLOWING
        )
                .bucket(ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET)
                .block(() -> (LiquidBlock) ModBlocks.BIPROPELLANT_ROCKET_FUEL_BLOCK.get());

        // -------------------------
        // NUCLEAR ROCKET FUEL
        // -------------------------
        NUCLEAR_ROCKET_FUEL = FLUIDS.register("nuclear_rocket_fuel",
                () -> new ForgeFlowingFluid.Source(NUCLEAR_ROCKET_FUEL_PROPS));
        NUCLEAR_ROCKET_FUEL_FLOWING = FLUIDS.register("nuclear_rocket_fuel_flowing",
                () -> new ForgeFlowingFluid.Flowing(NUCLEAR_ROCKET_FUEL_PROPS));
        NUCLEAR_ROCKET_FUEL_PROPS = () -> new ForgeFlowingFluid.Properties(
                Registry.NUCLEAR_ROCKET_FUEL_TYPE,
                NUCLEAR_ROCKET_FUEL,
                NUCLEAR_ROCKET_FUEL_FLOWING
        )
                .bucket(ModBlocks.NUCLEAR_ROCKET_FUEL_BUCKET)
                .block(() -> (LiquidBlock) ModBlocks.NUCLEAR_ROCKET_FUEL_BLOCK.get());
    }
}
