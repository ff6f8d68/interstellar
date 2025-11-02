package mods.hexagonal.ar2.client;

import mods.hexagonal.ar2.ModParticles;
import mods.hexagonal.ar2.particles.VFX;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientSetup {
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.VFX.get(), VFX.Factory::new);
    }
}
