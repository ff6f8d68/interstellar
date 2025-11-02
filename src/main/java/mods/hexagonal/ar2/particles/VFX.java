package mods.hexagonal.ar2.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class VFX extends TextureSheetParticle {

    protected final float r;
    protected final float g;
    protected final float b;

    protected VFX(ClientLevel world, double x, double y, double z,
                  double dx, double dy, double dz,
                  SpriteSet sprites,
                  float r, float g, float b) {
        super(world, x, y, z, dx, dy, dz);
        this.pickSprite(sprites);

        this.xd = dx;
        this.yd = dy;
        this.zd = dz;

        this.lifetime = 40;
        this.gravity = 0.0f;

        this.r = r;
        this.g = g;
        this.b = b;

        // Set initial color
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1.0f - ((float) this.age / this.lifetime);

    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    // --- Factory ---
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final float r, g, b;

        public Factory(SpriteSet sprites, float r, float g, float b) {
            this.sprites = sprites;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public Particle createParticle(SimpleParticleType type,
                                       ClientLevel world,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new VFX(world, x, y, z, dx, dy, dz, sprites, r, g, b);
        }
    }
}
