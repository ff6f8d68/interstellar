package team.nextlevelmodding.ar2;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.nextlevelmodding.ar2.fluids.ModFluids;
import team.nextlevelmodding.ar2.items.LinkerItem;
import team.nextlevelmodding.ar2.items.UsbItem;

import static team.nextlevelmodding.ar2.ModBlocks.BLOCKS;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ar2.MOD_ID);
    public static final RegistryObject<Item> LINKER_ITEM =
            ITEMS.register("linker",
                    LinkerItem::new
            );
    public static final RegistryObject<Item>USB_ITEM =
            ITEMS.register("usb",
                    UsbItem::new
            );
    // Rocket Fuel Fluid Blocks
    public static final RegistryObject<Block> ROCKET_FUEL_BLOCK = BLOCKS.register("rocket_fuel",
            () -> new LiquidBlock(ModFluids.SOURCE_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable()));
    public static final RegistryObject<Block> ADVANCED_ROCKET_FUEL_BLOCK = BLOCKS.register("advanced_rocket_fuel",
            () -> new LiquidBlock(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable()));
    public static final RegistryObject<Block> BIPROPELLANT_ROCKET_FUEL_BLOCK = BLOCKS.register("bipropellant_rocket_fuel",
            () -> new LiquidBlock(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL, Block.Properties.of().noCollission().strength(100f).noLootTable()));
}
