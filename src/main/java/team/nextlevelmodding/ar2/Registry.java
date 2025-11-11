package team.nextlevelmodding.ar2;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class Registry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ar2.MOD_ID);


    public static final Supplier<SimpleParticleType> ROCKET_FLAME =
            PARTICLES.register("rocketflame", () -> new SimpleParticleType(true));

}
