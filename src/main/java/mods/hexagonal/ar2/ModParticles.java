package mods.hexagonal.ar2;

import mods.hexagonal.ar2.particles.VFXParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ar2.MOD_ID);

    public static final RegistryObject<VFXParticleType> VFX_SOFT =
            PARTICLES.register("vfx_soft", VFXParticleType::new);
}
