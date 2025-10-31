package mods.hexagonal.ar2;

import mods.hexagonal.ar2.ar2;
import mods.hexagonal.ar2.blocks.ThrusterBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ar2.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ar2.MOD_ID);

    // Register block
    public static final RegistryObject<Block> THRUSTER_BLOCK =
            BLOCKS.register("thrusterblock", // lowercase!
                    () -> new ThrusterBlock()
            );

    public static final RegistryObject<Item> THRUSTER_BLOCK_ITEM =
            ITEMS.register("thrusterblock", // lowercase!
                    () -> new BlockItem(THRUSTER_BLOCK.get(), new Item.Properties())
            );


}
