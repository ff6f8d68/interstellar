package team.nextlevelmodding.ar2.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import team.nextlevelmodding.nlc.internal.nlcadditive;
import team.nextlevelmodding.nlc.lib.nlcrendertype;

public class RocketFlameParticle extends DustParticleBase<DustParticleOptions> {
    public float alpha = 1.0f; // fully opaque by default

    private float rotSpeed;
    private float targetSize;

    public RocketFlameParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz, new DustParticleOptions(new Vector3f(0, 0, 0), 10), spriteSet);


        boolean isSmoke = random.nextBoolean();
        this.alpha = Math.min(1.0f, (float) this.lifetime / 20f); // fades in

        if (isSmoke) {
            float f = this.random.nextFloat() * 0.5F + 0.2F;
            float SingleColor = randomizeColor(0.7f, f);
            this.rCol = SingleColor;
            this.gCol = SingleColor;
            this.bCol = SingleColor;
        } else {
            Vector3f color = new Vector3f(1F, 0.7F, 0.3F);
            float f = this.random.nextFloat() * 0.2F + 0.8F;
            this.rCol = this.randomizeColor(color.x(), f);
            this.gCol = this.randomizeColor(color.y(), f);
            this.bCol = this.randomizeColor(color.z(), f);
        }


        this.hasPhysics = true;

        if (!isSmoke) {
            this.lifetime = 20;
        } else {
            this.lifetime = (int) (200);
        }

        this.targetSize = quadSize;

        this.xd =1*vx + (Math.random() * (double) 2.0F - (double) 1.0F) * (double) 0.1F;
        this.yd = 1*vy + (Math.random() * (double) 2.0F - (double) 1.0F) * (double) 0.1F;
        this.zd = 1*vz + (Math.random() * (double) 2.0F - (double) 1.0F) * (double) 0.1F;

        this.rotSpeed = this.random.nextFloat() / 50f;

        this.roll = this.random.nextFloat();

        tick();
    }

    @Override
    public void tick() {
        super.tick();
        if (super.onGround) {
            float f = this.random.nextFloat() * 0.5F;
            yd = -yd * f;
        }
        if(this.lifetime < 20){
            this.quadSize = targetSize * (float)this.lifetime / 20f;
        }else{
            quadSize = targetSize;
        }

        this.oRoll = this.roll;
        this.roll = this.roll + this.rotSpeed;
        this.rotSpeed *= 0.99f;
    }
    @Override
    public ParticleRenderType getRenderType() {
        return nlcadditive.ADDITIVE; // respects texture alpha
    }
    @Override
    public int getLightColor(float partialTicks) {
        return 0xF000F0; // max brightness (like glow)
    }



}

