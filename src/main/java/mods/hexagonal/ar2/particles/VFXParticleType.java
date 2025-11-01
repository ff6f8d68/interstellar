package mods.hexagonal.ar2.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class VFXParticleType extends ParticleType<VFXParticleOptions> {

    public VFXParticleType() {
        super(false, VFXParticleOptions.DESERIALIZER);
    }

    @Override
    public Codec<VFXParticleOptions> codec() {
        return VFXParticleOptions.CODEC.codec();
    }
}