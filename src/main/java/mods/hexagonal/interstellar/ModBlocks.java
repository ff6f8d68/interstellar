package mods.hexagonal.interstellar;

import mods.hexagonal.interstellar.Interstellar;
import mods.hexagonal.interstellar.blocks.SpaceVoidBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Interstellar.MODID);

    // Register your block
    public static final RegistryObject<Block> SPACE_VOID_BLOCK =
            BLOCKS.register("space_void", SpaceVoidBlock::new);
}