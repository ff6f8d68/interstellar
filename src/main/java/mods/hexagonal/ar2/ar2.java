package mods.hexagonal.ar2;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("ar2")
public class ar2 {
    public static final String MOD_ID = "ar2";

    public ar2() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModParticles.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModCreativeTabs.register(modEventBus);
    }
}
