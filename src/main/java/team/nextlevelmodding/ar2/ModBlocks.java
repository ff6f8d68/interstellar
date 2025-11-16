package team.nextlevelmodding.ar2;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.nextlevelmodding.ar2.ModMenus.*;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.nextlevelmodding.ar2.blocks.*;
import team.nextlevelmodding.ar2.fluids.ModFluids;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ar2.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ar2.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ar2.MOD_ID);

    // Tank Block Entity
    public static final RegistryObject<BlockEntityType<TankBlockEntity>> TANK =
            BLOCK_ENTITIES.register("tank",
                    () -> {
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
                                ModBlocks.BIPROPELLANT_TOPTANK.get(),
                                ModBlocks.BIPROPELLANT_MIDDLETANK.get(),
                                ModBlocks.BIPROPELLANT_BOTTOMTANK.get()
                        ).build(null);

                        typeHolder.set(type);
                        return type;
                    }
            );

    // ---------------------------
    // Blocks & Items
    // ---------------------------

    // Simple blocks (display name = registry name)
    public static final RegistryObject<Block> SAWBLADE = BLOCKS.register("sawblade", Sawblade::new);

    public static final RegistryObject<Block> GUIDANCE_COMPUTER = BLOCKS.register("guidancecomputer", Sawblade::new);
    public static final RegistryObject<Item> GUIDANCE_COMPUTER_ITEM = ITEMS.register("guidancecomputer",
            () -> new BlockItem(GUIDANCE_COMPUTER.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "guidance computer"; }
            });

    public static final RegistryObject<Block> BOTTOMTANK = BLOCKS.register("bottomtank", Bottomtank::new);


    public static final RegistryObject<Block> MIDDLETANK = BLOCKS.register("middletank", Middletank::new);


    public static final RegistryObject<Block> TEST = BLOCKS.register("test", Test::new);
    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test",
            () -> new BlockItem(TEST.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "test"; }
            });

    public static final RegistryObject<Block> ROCKETMOTOR = BLOCKS.register("rocketmotor", Rocketmotor::new);
    public static final RegistryObject<Item> ROCKETMOTOR_ITEM = ITEMS.register("rocketmotor",
            () -> new BlockItem(ROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "rocketmotor"; }
            });

    public static final RegistryObject<Block> ADVROCKETMOTOR = BLOCKS.register("advrocketmotor", Advrocketmotor::new);
    public static final RegistryObject<Item> ADVROCKETMOTOR_ITEM = ITEMS.register("advrocketmotor",
            () -> new BlockItem(ADVROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "advrocketmotor"; }
            });

    public static final RegistryObject<Block> TOPTANK = BLOCKS.register("toptank", Toptank::new);
    public static final RegistryObject<Item> TOPTANK_ITEM =
            ITEMS.register("toptank",
                    () -> new BlockItem(TOPTANK.get(), new Item.Properties()) {
                        @Override
                        public String getDescriptionId() {
                            return "fuel tank";
                        }
                    });

    // Blocks with human-readable names
    public static final RegistryObject<Block> BIPROPELLANT_TOPTANK = BLOCKS.register("bipropellant_toptank", BipropellantToptank::new);
    public static final RegistryObject<Item> BIPROPELLANT_TOPTANK_ITEM = ITEMS.register("bipropellant_toptank",
            () -> new BlockItem(BIPROPELLANT_TOPTANK.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "bi-propellant fuel tank"; }
            });

    public static final RegistryObject<Block> BIPROPELLANT_MIDDLETANK = BLOCKS.register("bipropellant_middletank", BipropellantMiddletank::new);

    public static final RegistryObject<Block> BIPROPELLANT_BOTTOMTANK = BLOCKS.register("bipropellant_bottomtank", BipropellantBottomtank::new);


    public static final RegistryObject<Block> ADVBIPROPELLANTROCKETMOTOR = BLOCKS.register("advbipropellantrocketmotor", Advbipropellantrocketmotor::new);
    public static final RegistryObject<Item> ADVBIPROPELLANTROCKETMOTOR_ITEM = ITEMS.register("advbipropellantrocketmotor",
            () -> new BlockItem(ADVBIPROPELLANTROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "advanced bi-propellant rocket motor"; }
            });

    public static final RegistryObject<Block> NUCLEARROCKETMOTOR = BLOCKS.register("nuclearrocketmotor", Nuclearrocketmotor::new);
    public static final RegistryObject<Item> NUCLEARROCKETMOTOR_ITEM = ITEMS.register("nuclearrocketmotor",
            () -> new BlockItem(NUCLEARROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "nuclear rocket motor"; }
            });

    public static final RegistryObject<Block> NUCLEAR_GENERATOR = BLOCKS.register("nuclear_generator", NuclearGenerator::new);
    public static final RegistryObject<Item> NUCLEAR_GENERATOR_ITEM = ITEMS.register("nuclear_generator",
            () -> new BlockItem(NUCLEAR_GENERATOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "nuclear generator"; }
            });

    // Rocket Fuel Fluid Blocks
    public static final RegistryObject<Block> ROCKET_FUEL_BLOCK = BLOCKS.register("rocket_fuel",
            () -> new LiquidBlock(ModFluids.SOURCE_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable()));
    public static final RegistryObject<Block> ADVANCED_ROCKET_FUEL_BLOCK = BLOCKS.register("advanced_rocket_fuel",
            () -> new LiquidBlock(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable()));
    public static final RegistryObject<Block> BIPROPELLANT_ROCKET_FUEL_BLOCK = BLOCKS.register("bipropellant_rocket_fuel",
            () -> new LiquidBlock(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable()));

    // Rocket Fuel Buckets
    // Rocket Fuel Buckets with proper in-game names
    public static final RegistryObject<Item> ROCKET_FUEL_BUCKET = ITEMS.register("rocket_fuel_bucket",
            () -> new BucketItem(ModFluids.SOURCE_ROCKET_FUEL, new Item.Properties().stacksTo(1)) {
                @Override
                public String getDescriptionId() {
                    return "rocket fuel";
                }
            });

    public static final RegistryObject<Item> ADVANCED_ROCKET_FUEL_BUCKET = ITEMS.register("advanced_rocket_fuel_bucket",
            () -> new BucketItem(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL, new Item.Properties().stacksTo(1)) {
                @Override
                public String getDescriptionId() {
                    return "advanced rocket fuel";
                }
            });

    public static final RegistryObject<Item> BIPROPELLANT_ROCKET_FUEL_BUCKET = ITEMS.register("bipropellant_rocket_fuel_bucket",
            () -> new BucketItem(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL, new Item.Properties().stacksTo(1)) {
                @Override
                public String getDescriptionId() {
                    return "bi-propellant rocket fuel";
                }
            });


    // Block Entities
    public static final RegistryObject<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("test_block_entity", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST.get()).build(null));

    public static final RegistryObject<BlockEntityType<NuclearGeneratorBlockEntity>> NUCLEAR_GENERATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("nuclear_generator_block_entity",
                    () -> BlockEntityType.Builder.of(NuclearGeneratorBlockEntity::new, NUCLEAR_GENERATOR.get()).build(null));
}
