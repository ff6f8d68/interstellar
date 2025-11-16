package team.nextlevelmodding.ar2.fluids;


import team.nextlevelmodding.ar2.ar2;
import team.nextlevelmodding.ar2.ModBlocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, ar2.MOD_ID);

    public static final RegistryObject<FlowingFluid> SOURCE_ROCKET_FUEL = FLUIDS.register("rocket_fuel_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.ROCKET_FUEL_FLUID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_ROCKET_FUEL = FLUIDS.register("flowing_rocket_fuel",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.ROCKET_FUEL_FLUID_PROPERTIES));

    public static final RegistryObject<FlowingFluid> SOURCE_ADVANCED_ROCKET_FUEL = FLUIDS.register("advanced_rocket_fuel_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.ADVANCED_ROCKET_FUEL_FLUID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_ADVANCED_ROCKET_FUEL = FLUIDS.register("flowing_advanced_rocket_fuel",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.ADVANCED_ROCKET_FUEL_FLUID_PROPERTIES));

    public static final RegistryObject<FlowingFluid> SOURCE_BIPROPELLANT_ROCKET_FUEL = FLUIDS.register("bipropellant_rocket_fuel_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.BIPROPELLANT_ROCKET_FUEL_FLUID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLOWING_BIPROPELLANT_ROCKET_FUEL = FLUIDS.register("flowing_bipropellant_rocket_fuel",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.BIPROPELLANT_ROCKET_FUEL_FLUID_PROPERTIES));



    public static final ForgeFlowingFluid.Properties ROCKET_FUEL_FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.ROCKET_FUEL_FLUID_TYPE, SOURCE_ROCKET_FUEL, FLOWING_ROCKET_FUEL)
            .slopeFindDistance(2).levelDecreasePerBlock(2).block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.ROCKET_FUEL_BLOCK.get())
            .bucket(() -> (net.minecraft.world.item.BucketItem) ModBlocks.ROCKET_FUEL_BUCKET.get());

    public static final ForgeFlowingFluid.Properties ADVANCED_ROCKET_FUEL_FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.ADVANCED_ROCKET_FUEL_FLUID_TYPE, SOURCE_ADVANCED_ROCKET_FUEL, FLOWING_ADVANCED_ROCKET_FUEL)
            .slopeFindDistance(2).levelDecreasePerBlock(2).block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.ADVANCED_ROCKET_FUEL_BLOCK.get())
            .bucket(() -> (net.minecraft.world.item.BucketItem) ModBlocks.ADVANCED_ROCKET_FUEL_BUCKET.get());

    public static final ForgeFlowingFluid.Properties BIPROPELLANT_ROCKET_FUEL_FLUID_PROPERTIES = new ForgeFlowingFluid.Properties(
            ModFluidTypes.BIPROPELLANT_ROCKET_FUEL_FLUID_TYPE, SOURCE_BIPROPELLANT_ROCKET_FUEL, FLOWING_BIPROPELLANT_ROCKET_FUEL)
            .slopeFindDistance(2).levelDecreasePerBlock(2).block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.BIPROPELLANT_ROCKET_FUEL_BLOCK.get())
            .bucket(() -> (net.minecraft.world.item.BucketItem) ModBlocks.BIPROPELLANT_ROCKET_FUEL_BUCKET.get());




    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}