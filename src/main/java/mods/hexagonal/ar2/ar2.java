package mods.hexagonal.ar2;

import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import mods.hexagonal.ar2.particles.RocketFlameParticleProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("ar2")
public class ar2 {
    public static final String MOD_ID = "ar2";

    public ar2() {
        Registry.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModParticles.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::registerParticles);
        MinecraftForge.EVENT_BUS.addListener(ModCommands::register);

    }
    public void registerParticles(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(Registry.ROCKET_FLAME.get(), RocketFlameParticleProvider::new);
    }
}
