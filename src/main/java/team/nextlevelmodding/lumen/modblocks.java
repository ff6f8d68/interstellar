package team.nextlevelmodding.lumen;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.nextlevelmodding.ar2.ar2;
import team.nextlevelmodding.ar2.blocks.Sawblade;

public class modblocks {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, lumen.MOD_ID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, lumen.MOD_ID);
    public static final RegistryObject<Block> BIG_BULB = BLOCKS.register("bulb", bigbulbblock::new);
    public static final RegistryObject<Item> BIG_BULB_ITEM = ITEMS.register("bulb",
            () -> new BlockItem(BIG_BULB.get(), new Item.Properties()) {
                @Override public String getDescriptionId() { return "big bulb"; }
            });
}
