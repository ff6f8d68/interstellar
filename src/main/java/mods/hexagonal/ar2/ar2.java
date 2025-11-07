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
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        Registry.PARTICLES.register(modEventBus);
        Registry.FLUID_TYPES.register(modEventBus);
        Fluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::registerParticles);
        BLOCK_ENTITIES.register(modEventBus);
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
