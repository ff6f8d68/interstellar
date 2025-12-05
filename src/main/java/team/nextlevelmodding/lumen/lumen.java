package team.nextlevelmodding.lumen;


import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.nextlevelmodding.lumen.modblocks;

@Mod("lumen")
public class lumen {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    public static final String MOD_ID = "lumen";
    public lumen() {
        modblocks.BLOCKS.register(modEventBus);
        modblocks.ITEMS.register(modEventBus);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        // Tell Minecraft to render the block as translucent
        ItemBlockRenderTypes.setRenderLayer(modblocks.BIG_BULB.get(), RenderType.cutout());
    }
}
