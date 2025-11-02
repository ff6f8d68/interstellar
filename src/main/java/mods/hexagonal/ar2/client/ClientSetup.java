package mods.hexagonal.ar2.client;

import mods.hexagonal.ar2.ModParticles;
import mods.hexagonal.ar2.Registry;
import mods.hexagonal.ar2.ar2;
import mods.hexagonal.ar2.particles.RocketFlameParticleProvider;
import mods.hexagonal.ar2.particles.VFX;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ar2.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Minecraft.getInstance().particleEngine.register(
                Registry.ROCKET_FLAME.get(),
                RocketFlameParticleProvider::new
        );
    }
}

