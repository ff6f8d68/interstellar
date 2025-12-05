package team.nextlevelmodding.ar2;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
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

    // ---------------------------
    // Blocks
    // ---------------------------
    public static final RegistryObject<Block> SAWBLADE = BLOCKS.register("sawblade", Sawblade::new);
    public static final RegistryObject<Block> GUIDANCE_COMPUTER = BLOCKS.register("guidancecomputer", GuidanceComputerBlock::new);
    public static final RegistryObject<Block> BOTTOMTANK = BLOCKS.register("bottomtank", Bottomtank::new);
    public static final RegistryObject<Block> MIDDLETANK = BLOCKS.register("middletank", Middletank::new);
    public static final RegistryObject<Block> TOPTANK = BLOCKS.register("toptank", Toptank::new);
    public static final RegistryObject<Block> TEST = BLOCKS.register("test", Test::new);
    public static final RegistryObject<Block> ROCKETMOTOR = BLOCKS.register("rocketmotor", Rocketmotor::new);
    public static final RegistryObject<Block> ADVROCKETMOTOR = BLOCKS.register("advrocketmotor", Advrocketmotor::new);
    public static final RegistryObject<Block> BIPROPELLANT_TOPTANK = BLOCKS.register("bipropellant_toptank", BipropellantToptank::new);
    public static final RegistryObject<Block> BIPROPELLANT_MIDDLETANK = BLOCKS.register("bipropellant_middletank", BipropellantMiddletank::new);
    public static final RegistryObject<Block> BIPROPELLANT_BOTTOMTANK = BLOCKS.register("bipropellant_bottomtank", BipropellantBottomtank::new);
    public static final RegistryObject<Block> ADVBIPROPELLANTROCKETMOTOR = BLOCKS.register("advbipropellantrocketmotor", Advbipropellantrocketmotor::new);
    public static final RegistryObject<Block> FLIGHTCONTROLLCOMPUTER = BLOCKS.register("flight_control_computer", flight_control_computer::new);
    public static final RegistryObject<Block> ENERGY_ROCKET_MOTOR = BLOCKS.register("energy_rocket_motor", EnergyRocketMotor::new);
    public static final RegistryObject<Block> SEAT = BLOCKS.register("seat", Seat::new);

    // ---------------------------
    // Items (exactly as you had them)
    // ---------------------------
    public static final RegistryObject<Item> GUIDANCE_COMPUTER_ITEM = ITEMS.register("guidancecomputer",
            () -> new BlockItem(GUIDANCE_COMPUTER.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "guidance computer"; }
            });
    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test",
            () -> new BlockItem(TEST.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "test"; }
            });
    public static final RegistryObject<Item> ROCKETMOTOR_ITEM = ITEMS.register("rocketmotor",
            () -> new BlockItem(ROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "rocket motor"; }
            });
    public static final RegistryObject<Item> ADVROCKETMOTOR_ITEM = ITEMS.register("advrocketmotor",
            () -> new BlockItem(ADVROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "advanced rocket motor"; }
            });
    public static final RegistryObject<Item> TOPTANK_ITEM = ITEMS.register("toptank",
            () -> new BlockItem(TOPTANK.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "fuel tank"; }
            });
    public static final RegistryObject<Item> BIPROPELLANT_TOPTANK_ITEM = ITEMS.register("bipropellant_toptank",
            () -> new BlockItem(BIPROPELLANT_TOPTANK.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "bi-propellant fuel tank"; }
            });
    public static final RegistryObject<Item> ADVBIPROPELLANTROCKETMOTOR_ITEM = ITEMS.register("advbipropellantrocketmotor",
            () -> new BlockItem(ADVBIPROPELLANTROCKETMOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "advanced bi-propellant rocket motor"; }
            });
    public static final RegistryObject<Item> FLIGHTCONTROLLCOMPUTER_ITEM = ITEMS.register("flight_control_computer",
            () -> new BlockItem(FLIGHTCONTROLLCOMPUTER.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "flight control computer"; }
            });
    public static final RegistryObject<Item> ENERGY_ROCKET_MOTOR_ITEM = ITEMS.register("energy_rocket_motor",
            () -> new BlockItem(ENERGY_ROCKET_MOTOR.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "energy-based rocket motor"; }
            });
    public static final RegistryObject<Item> SEAT_ITEM = ITEMS.register("seat",
            () -> new BlockItem(SEAT.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "seat"; }
            });

    // Rocket fuel buckets (unchanged)
    public static final RegistryObject<Item> ROCKET_FUEL_BUCKET = ITEMS.register("rocket_fuel_bucket",
            () -> new BucketItem(ModFluids.SOURCE_ROCKET_FUEL, new Item.Properties().stacksTo(1)) {
                @Override public String getDescriptionId() { return "rocket fuel"; }
            });
    public static final RegistryObject<Item> ADVANCED_ROCKET_FUEL_BUCKET = ITEMS.register("advanced_rocket_fuel_bucket",
            () -> new BucketItem(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL, new Item.Properties().stacksTo(1)) {
                @Override public String getDescriptionId() { return "advanced rocket fuel"; }
            });
    public static final RegistryObject<Item> BIPROPELLANT_ROCKET_FUEL_BUCKET = ITEMS.register("bipropellant_rocket_fuel_bucket",
            () -> new BucketItem(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL, new Item.Properties().stacksTo(1)) {
                @Override public String getDescriptionId() { return "bi-propellant rocket fuel"; }
            });

    // ---------------------------
    // Block Entities
    // ---------------------------
    public static final RegistryObject<BlockEntityType<TankBlockEntity>> TANK = BLOCK_ENTITIES.register("tank",
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
                        TOPTANK.get(),
                        MIDDLETANK.get(),
                        BOTTOMTANK.get(),
                        BIPROPELLANT_TOPTANK.get(),
                        BIPROPELLANT_MIDDLETANK.get(),
                        BIPROPELLANT_BOTTOMTANK.get()
                ).build(null);

                typeHolder.set(type);
                return type;
            });

    public static final RegistryObject<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("test_block_entity", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<GuidanceComputerBlockEntity>> GUIDANCE_COMPUTER_BE =
            BLOCK_ENTITIES.register("guidance_computer_be", () -> BlockEntityType.Builder.of(GuidanceComputerBlockEntity::new, GUIDANCE_COMPUTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<FlightControlComputerBlockEntity>> FLIGHT_CONTROL_COMPUTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("flight_control_computer_block_entity", () -> {
                java.util.concurrent.atomic.AtomicReference<BlockEntityType<FlightControlComputerBlockEntity>> typeHolder =
                        new java.util.concurrent.atomic.AtomicReference<>();

                BlockEntityType<FlightControlComputerBlockEntity> type = BlockEntityType.Builder.of((pos, state) -> {
                            BlockEntityType<FlightControlComputerBlockEntity> t = typeHolder.get();
                            if (t == null) {
                                throw new IllegalStateException("FlightControlComputerBlockEntity type not initialized");
                            }
                            return new FlightControlComputerBlockEntity(t, pos, state);
                        },
                        FLIGHTCONTROLLCOMPUTER.get()
                ).build(null);

                typeHolder.set(type);
                return type;
            });

    // ---------------------------
    // Register method
    // ---------------------------
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}
