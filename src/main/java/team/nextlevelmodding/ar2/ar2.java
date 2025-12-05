package team.nextlevelmodding.ar2;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import team.nextlevelmodding.ar2.entity.modentitys;
import team.nextlevelmodding.ar2.entity.seatrenderer;
import team.nextlevelmodding.ar2.fluids.ModFluidTypes;
import team.nextlevelmodding.ar2.client.ClientSetup;
import team.nextlevelmodding.ar2.fluids.ModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.nextlevelmodding.ar2.particles.RocketFlameParticleProvider;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static team.nextlevelmodding.ar2.ModBlocks.BLOCK_ENTITIES;
import static team.nextlevelmodding.ar2.ModMenus.REGISTRY;
import team.nextlevelmodding.ar2.ModMenus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.nextlevelmodding.ar2.planetry.RegisterOnReload;

import java.awt.*;

@Mod("ar2")
public class ar2 {
    public static final String MOD_ID = "ar2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ar2() {
        @SuppressWarnings("removal") IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        Registry.PARTICLES.register(modEventBus);
        ModFluids.register(modEventBus);
        ModFluidTypes.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::registerParticles);
        BLOCK_ENTITIES.register(modEventBus);
        REGISTRY.register(modEventBus);
        team.nextlevelmodding.ar2.gui.FlightcontrolMenu.MENUS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(ModCommands::register);
        MinecraftForge.EVENT_BUS.addListener(ModEvents::onBlockRightClick);
        modEventBus.addListener(this::setup);
        ModItems.ITEMS.register(modEventBus);
        modentitys.register(modEventBus);





    }
    public void registerParticles(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(Registry.ROCKET_FLAME.get(), RocketFlameParticleProvider::new);
    }
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("AR2 mod setup starting");
        // Only now are RegistryObjects guaranteed to exist
        TankParser.registerConnectable(ModBlocks.ROCKETMOTOR.get());
        TankParser.registerConnectable(ModBlocks.ADVROCKETMOTOR.get());
        TankParser.registerConnectable(ModBlocks.ADVBIPROPELLANTROCKETMOTOR.get());
        TankParser.registerConnectable(ModBlocks.ENERGY_ROCKET_MOTOR.get());
        RegisterOnReload.createEmptyConfig();
    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(modentitys.seat.get(), seatrenderer::new);
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_ROCKET_FUEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ROCKET_FUEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_ADVANCED_ROCKET_FUEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ADVANCED_ROCKET_FUEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_BIPROPELLANT_ROCKET_FUEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_BIPROPELLANT_ROCKET_FUEL.get(), RenderType.translucent());
            ModScreens.registerScreens();
        }
    }
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RegisterOnReload.loadAndRegister();
            // This triggers when a player joins a server OR starts a singleplayer world
        }
    }

}
