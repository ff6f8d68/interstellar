package mods.hexagonal.ar2.client;

import mods.hexagonal.ar2.ModParticles;
import mods.hexagonal.ar2.particles.VFX;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientSetup {
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.register(ModParticles.VFX.get(),
                spriteSet -> new VFX.Factory(spriteSet, 1.0f, 0.9f, 0.2f)); // yellow thrust

    }
}
