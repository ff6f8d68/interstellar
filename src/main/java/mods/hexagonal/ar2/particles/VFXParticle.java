package mods.hexagonal.ar2.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

public class VFXParticle extends TextureSheetParticle {

    public VFXParticle(ClientLevel level, double x, double y, double z,
                       double vx, double vy, double vz,
                       VFXParticleOptions options) {
        super(level, x, y, z, vx, vy, vz);

        // 💠 COLOR
        this.rCol = options.r;
        this.gCol = options.g;
        this.bCol = options.b;
        this.alpha = options.a;

        // 💠 NO GRAVITY
        this.gravity = 0.0F;

        // 💠 ENABLE BLOCK COLLISION
        this.hasPhysics = true;

        // 💠 PREVENT AUTO-SLOWING
        this.friction = 1.0F;   // 1.0 = no drag

        // 💠 OPTIONAL: prevent velocity damping even more
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        // 💠 OTHER SETTINGS
        this.lifetime = 40;
        this.setSize(0.2F, 0.2F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}

