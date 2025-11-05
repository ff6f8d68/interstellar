package mods.hexagonal.ar2;

import mods.hexagonal.ar2.TankParser;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import mods.hexagonal.ar2.particles.RocketFlameParticleProvider;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static mods.hexagonal.ar2.ModBlocks.BLOCK_ENTITIES;

@Mod("ar2")
public class ar2 {
    public static final String MOD_ID = "ar2";

    public ar2() {
        Registry.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registry.FLUID_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        Fluids.FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModParticles.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::registerParticles);
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(ModCommands::register);
        MinecraftForge.EVENT_BUS.addListener(ModEvents::onBlockRightClick);
        modEventBus.addListener(this::setup);

    }
    public void registerParticles(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(Registry.ROCKET_FLAME.get(), RocketFlameParticleProvider::new);
    }
    private void setup(final FMLCommonSetupEvent event) {
        // Only now are RegistryObjects guaranteed to exist
        TankParser.registerConnectable(ModBlocks.ROCKETMOTOR.get());
        TankParser.registerConnectable(ModBlocks.ADVROCKETMOTOR.get());
        TankParser.registerConnectable(ModBlocks.ADVBIPROPELLANTROCKETMOTOR.get());
        TankParser.registerConnectable(ModBlocks.NUCLEARROCKETMOTOR.get());
    }
}
