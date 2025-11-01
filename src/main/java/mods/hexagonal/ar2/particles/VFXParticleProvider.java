package mods.hexagonal.ar2.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class VFXParticleProvider implements ParticleProvider<VFXParticleOptions> {

    private final SpriteSet sprites;

    public VFXParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public VFXParticle createParticle(VFXParticleOptions options, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
        VFXParticle particle = new VFXParticle(level, x, y, z, vx, vy, vz, options);
        particle.pickSprite(this.sprites);
        return particle;
    }
}