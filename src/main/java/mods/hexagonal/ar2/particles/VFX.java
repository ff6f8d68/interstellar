package mods.hexagonal.ar2.particles;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class VFX extends TextureSheetParticle {

    protected VFX(ClientLevel world, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(world, x, y, z, dx, dy, dz);
        this.pickSprite(sprites);

        this.xd = dx;
        this.yd = dy;
        this.zd = dz;

        this.lifetime = 40;
        this.gravity = 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        // fade out
        this.alpha = 1.0f - ((float) this.age / this.lifetime);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double dx, double dy, double dz) {
            return new VFX(world, x, y, z, dx, dy, dz, sprites);
        }
    }
}
