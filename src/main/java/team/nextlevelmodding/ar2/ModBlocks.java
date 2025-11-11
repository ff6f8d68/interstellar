package team.nextlevelmodding.ar2;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import mods.hexagonal.ar2.blocks.*;
import team.nextlevelmodding.ar2.blocks.*;
import team.nextlevelmodding.ar2.fluids.ModFluids;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ar2.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ar2.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ar2.MOD_ID);
    public static final RegistryObject<BlockEntityType<TankBlockEntity>> TANK =
            BLOCK_ENTITIES.register("tank",
                    () -> {
                        // Create a mutable holder for the type to avoid self-reference
                        java.util.concurrent.atomic.AtomicReference<BlockEntityType<TankBlockEntity>> typeHolder = 
                            new java.util.concurrent.atomic.AtomicReference<>();
                        
                        BlockEntityType<TankBlockEntity> type = BlockEntityType.Builder.of((pos, state) -> {
                            BlockEntityType<TankBlockEntity> t = typeHolder.get();
                            if (t == null) {
                                throw new IllegalStateException("TankBlockEntity type not initialized");
                            }
                            return new TankBlockEntity(t, pos, state);
                        },
                                ModBlocks.TOPTANK.get(),
                                ModBlocks.MIDDLETANK.get(),
                                ModBlocks.BOTTOMTANK.get(),
                                ModBlocks.BIPROPELLANT_TANK.get()
                        ).build(null);
                        
                        typeHolder.set(type);
                        return type;
                    }
            );
    // ---------------------------
    // Blocks & Items
    // ---------------------------
    /*
    public static final RegistryObject<Block> CHEMICALREACTOR =
            BLOCKS.register("chemicalreactor",
                    Chemicalreactor::new
            );

    public static final RegistryObject<Item> CHEMICALREACTOR_ITEM =
            ITEMS.register("chemicalreactor",
                    () -> new BlockItem(CHEMICALREACTOR.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> SAWBLADE =
            BLOCKS.register("sawblade",
                    Sawblade::new
            );

    public static final RegistryObject<Item> SAWBLADE_ITEM =
            ITEMS.register("sawblade",
                    () -> new BlockItem(SAWBLADE.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> WARPCORE =
            BLOCKS.register("warpcore",
                    Warpcore::new
            );

    public static final RegistryObject<Item> WARPCORE_ITEM =
            ITEMS.register("warpcore",
                    () -> new BlockItem(WARPCORE.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> ELECTROLYSER =
            BLOCKS.register("electrolyser",
                    Electrolyser::new
            );

    public static final RegistryObject<Item> ELECTROLYSER_ITEM =
            ITEMS.register("electrolyser",
                    () -> new BlockItem(ELECTROLYSER.get(),
                            new Item.Properties())
            );
      */
    public static final RegistryObject<Block> BOTTOMTANK =
            BLOCKS.register("bottomtank",
                    Bottomtank::new
            );

    public static final RegistryObject<Item> BOTTOMTANK_ITEM =
            ITEMS.register("bottomtank",
                    () -> new BlockItem(BOTTOMTANK.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> CRYSTALLISER =
            BLOCKS.register("crystalliser",
                    Crystalliser::new
            );

    public static final RegistryObject<Item> CRYSTALLISER_ITEM =
            ITEMS.register("crystalliser",
                    () -> new BlockItem(CRYSTALLISER.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> SPACEELEVATOR =
            BLOCKS.register("spaceelevator",
                    Spaceelevator::new
            );

    public static final RegistryObject<Item> SPACEELEVATOR_ITEM =
            ITEMS.register("spaceelevator",
                    () -> new BlockItem(SPACEELEVATOR.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> ROLLINGMACHINE =
            BLOCKS.register("rollingmachine",
                    Rollingmachine::new
            );

    public static final RegistryObject<Item> ROLLINGMACHINE_ITEM =
            ITEMS.register("rollingmachine",
                    () -> new BlockItem(ROLLINGMACHINE.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> JETPACK =
            BLOCKS.register("jetpack",
                    Jetpack::new
            );

    public static final RegistryObject<Item> JETPACK_ITEM =
            ITEMS.register("jetpack",
                    () -> new BlockItem(JETPACK.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> SOLAR_ARRAY =
            BLOCKS.register("solar_array",
                    SolarArray::new
            );

    public static final RegistryObject<Item> SOLAR_ARRAY_ITEM =
            ITEMS.register("solar_array",
                    () -> new BlockItem(SOLAR_ARRAY.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> MIDDLETANK =
            BLOCKS.register("middletank",
                    Middletank::new
            );

    public static final RegistryObject<Item> MIDDLETANK_ITEM =
            ITEMS.register("middletank",
                    () -> new BlockItem(MIDDLETANK.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> TERRAFORMERATM =
            BLOCKS.register("terraformeratm",
                    Terraformeratm::new
            );

    public static final RegistryObject<Item> TERRAFORMERATM_ITEM =
            ITEMS.register("terraformeratm",
                    () -> new BlockItem(TERRAFORMERATM.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> TEST =
            BLOCKS.register("test",
                    Test::new
            );
    public static final RegistryObject<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("test_block_entity",
                    () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST.get()).build(null));

    public static final RegistryObject<Item> TEST_ITEM =
            ITEMS.register("test",
                    () -> new BlockItem(TEST.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> ROCKETMOTOR =
            BLOCKS.register("rocketmotor",
                    Rocketmotor::new
            );

    public static final RegistryObject<Item> ROCKETMOTOR_ITEM =
            ITEMS.register("rocketmotor",
                    () -> new BlockItem(ROCKETMOTOR.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> BIOMESCANNER =
            BLOCKS.register("biomescanner",
                    Biomescanner::new
            );

    public static final RegistryObject<Item> BIOMESCANNER_ITEM =
            ITEMS.register("biomescanner",
                    () -> new BlockItem(BIOMESCANNER.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> ASTROBODYDATAPROCESSOR =
            BLOCKS.register("astrobodydataprocessor",
                    Astrobodydataprocessor::new
            );

    public static final RegistryObject<Item> ASTROBODYDATAPROCESSOR_ITEM =
            ITEMS.register("astrobodydataprocessor",
                    () -> new BlockItem(ASTROBODYDATAPROCESSOR.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> ADVBIPROPELLANTROCKETMOTOR =
            BLOCKS.register("advbipropellantrocketmotor",
                    Advbipropellantrocketmotor::new
            );

    public static final RegistryObject<Item> ADVBIPROPELLANTROCKETMOTOR_ITEM =
            ITEMS.register("advbipropellantrocketmotor",
                    () -> new BlockItem(ADVBIPROPELLANTROCKETMOTOR.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> CENTRIFUGE =
            BLOCKS.register("centrifuge",
                    Centrifuge::new
            );

    public static final RegistryObject<Item> CENTRIFUGE_ITEM =
            ITEMS.register("centrifuge",
                    () -> new BlockItem(CENTRIFUGE.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> OBSERVATORY =
            BLOCKS.register("observatory",
                    Observatory::new
            );

    public static final RegistryObject<Item> OBSERVATORY_ITEM =
            ITEMS.register("observatory",
                    () -> new BlockItem(OBSERVATORY.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> DRILL =
            BLOCKS.register("drill",
                    Drill::new
            );

    public static final RegistryObject<Item> DRILL_ITEM =
            ITEMS.register("drill",
                    () -> new BlockItem(DRILL.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> PRECISIONLASERETCHER =
            BLOCKS.register("precisionlaseretcher",
                    Precisionlaseretcher::new
            );

    public static final RegistryObject<Item> PRECISIONLASERETCHER_ITEM =
            ITEMS.register("precisionlaseretcher",
                    () -> new BlockItem(PRECISIONLASERETCHER.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> ADVROCKETMOTOR =
            BLOCKS.register("advrocketmotor",
                    Advrocketmotor::new
            );

    public static final RegistryObject<Item> ADVROCKETMOTOR_ITEM =
            ITEMS.register("advrocketmotor",
                    () -> new BlockItem(ADVROCKETMOTOR.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> TOPTANK =
            BLOCKS.register("toptank",
                    Toptank::new
            );

    public static final RegistryObject<Item> TOPTANK_ITEM =
            ITEMS.register("toptank",
                    () -> new BlockItem(TOPTANK.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> ENDTANK =
            BLOCKS.register("endtank",
                    Endtank::new
            );

    public static final RegistryObject<Item> ENDTANK_ITEM =
            ITEMS.register("endtank",
                    () -> new BlockItem(ENDTANK.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> ORBITALLASERDRILL =
            BLOCKS.register("orbitallaserdrill",
                    Orbitallaserdrill::new
            );

    public static final RegistryObject<Item> ORBITALLASERDRILL_ITEM =
            ITEMS.register("orbitallaserdrill",
                    () -> new BlockItem(ORBITALLASERDRILL.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> AREAGRAVITYCONTROLLER =
            BLOCKS.register("areagravitycontroller",
                    Areagravitycontroller::new
            );

    public static final RegistryObject<Item> AREAGRAVITYCONTROLLER_ITEM =
            ITEMS.register("areagravitycontroller",
                    () -> new BlockItem(AREAGRAVITYCONTROLLER.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> BEACON =
            BLOCKS.register("beacon",
                    Beacon::new
            );

    public static final RegistryObject<Item> BEACON_ITEM =
            ITEMS.register("beacon",
                    () -> new BlockItem(BEACON.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> PRECISIONASSEMBLER =
            BLOCKS.register("precisionassembler",
                    Precisionassembler::new
            );

    public static final RegistryObject<Item> PRECISIONASSEMBLER_ITEM =
            ITEMS.register("precisionassembler",
                    () -> new BlockItem(PRECISIONASSEMBLER.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> HOVERCRAFT =
            BLOCKS.register("hovercraft",
                    Hovercraft::new
            );

    public static final RegistryObject<Item> HOVERCRAFT_ITEM =
            ITEMS.register("hovercraft",
                    () -> new BlockItem(HOVERCRAFT.get(),
                            new Item.Properties())
            );
    /*
    public static final RegistryObject<Block> CUTTINGMACHINE =
            BLOCKS.register("cuttingmachine",
                    Cuttingmachine::new
            );

    public static final RegistryObject<Item> CUTTINGMACHINE_ITEM =
            ITEMS.register("cuttingmachine",
                    () -> new BlockItem(CUTTINGMACHINE.get(),
                            new Item.Properties())
            );
    */
    public static final RegistryObject<Block> NUCLEARROCKETMOTOR =
            BLOCKS.register("nuclearrocketmotor",
                    Nuclearrocketmotor::new
            );

    public static final RegistryObject<Item> NUCLEARROCKETMOTOR_ITEM =
            ITEMS.register("nuclearrocketmotor",
                    () -> new BlockItem(NUCLEARROCKETMOTOR.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> BIPROPELLANT_TANK =
            BLOCKS.register("bipropellant_tank",
                    BipropellantTank::new
            );

    public static final RegistryObject<Item> BIPROPELLANT_TANK_ITEM =
            ITEMS.register("bipropellant_tank",
                    () -> new BlockItem(BIPROPELLANT_TANK.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> NUCLEAR_GENERATOR =
            BLOCKS.register("nuclear_generator",
                    NuclearGenerator::new
            );

    public static final RegistryObject<Item> NUCLEAR_GENERATOR_ITEM =
            ITEMS.register("nuclear_generator",
                    () -> new BlockItem(NUCLEAR_GENERATOR.get(),
                            new Item.Properties())
            );

    // Rocket Fuel Fluid Blocks
    public static final RegistryObject<Block> ROCKET_FUEL_BLOCK =
            BLOCKS.register("rocket_fuel",
                    () -> new LiquidBlock(ModFluids.SOURCE_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable())
            );

    public static final RegistryObject<Block> ADVANCED_ROCKET_FUEL_BLOCK =
            BLOCKS.register("advanced_rocket_fuel",
                    () -> new LiquidBlock(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable())
            );

    public static final RegistryObject<Block> BIPROPELLANT_ROCKET_FUEL_BLOCK =
            BLOCKS.register("bipropellant_rocket_fuel",
                    () -> new LiquidBlock(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable())
            );

    public static final RegistryObject<Block> NUCLEAR_ROCKET_FUEL_BLOCK =
            BLOCKS.register("nuclear_rocket_fuel",
                    () -> new LiquidBlock(ModFluids.SOURCE_NUCLEAR_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable())
            );

    // Rocket Fuel Buckets
    public static final RegistryObject<Item> ROCKET_FUEL_BUCKET =
            ITEMS.register("rocket_fuel_bucket",
                    () -> new BucketItem(ModFluids.SOURCE_ROCKET_FUEL, new Item.Properties().stacksTo(1))
            );

    public static final RegistryObject<Item> ADVANCED_ROCKET_FUEL_BUCKET =
            ITEMS.register("advanced_rocket_fuel_bucket",
                    () -> new BucketItem(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL, new Item.Properties().stacksTo(1))
            );

    public static final RegistryObject<Item> BIPROPELLANT_ROCKET_FUEL_BUCKET =
            ITEMS.register("bipropellant_rocket_fuel_bucket",
                    () -> new BucketItem(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL, new Item.Properties().stacksTo(1))
            );

    public static final RegistryObject<Item> NUCLEAR_ROCKET_FUEL_BUCKET =
            ITEMS.register("nuclear_rocket_fuel_bucket",
                    () -> new BucketItem(ModFluids.SOURCE_NUCLEAR_ROCKET_FUEL, new Item.Properties().stacksTo(1))
            );

    /*
    public static final RegistryObject<Block> RAILGUN =
            BLOCKS.register("railgun",
                    Railgun::new
            );

    public static final RegistryObject<Item> RAILGUN_ITEM =
            ITEMS.register("railgun",
                    () -> new BlockItem(RAILGUN.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> BLACKHOLEGENERATOR =
            BLOCKS.register("blackholegenerator",
                    Blackholegenerator::new
            );

    public static final RegistryObject<Item> BLACKHOLEGENERATOR_ITEM =
            ITEMS.register("blackholegenerator",
                    () -> new BlockItem(BLACKHOLEGENERATOR.get(),
                            new Item.Properties())
            );

    public static final RegistryObject<Block> LATHE =
            BLOCKS.register("lathe",
                    Lathe::new
            );

    public static final RegistryObject<Item> LATHE_ITEM =
            ITEMS.register("lathe",
                    () -> new BlockItem(LATHE.get(),
                            new Item.Properties())
            );
    */
}
