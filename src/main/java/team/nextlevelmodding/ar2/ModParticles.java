package team.nextlevelmodding.ar2;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ar2.MOD_ID);

    // --- Particle registrations ---


    // Example: colored particles similar to Visuality


    // --- Register deferred register to the mod event bus ---
    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }

    // --- Client-side: register particle factories ---

}
