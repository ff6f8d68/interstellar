package mods.hexagonal.ar2.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import mods.hexagonal.ar2.ModParticles; // adjust to your actual registry class

import java.util.Locale;

public class VFXParticleOptions implements ParticleOptions {
    // Codec used for data-driven (if you ever need it) and for commands parsing via codec system
    public static final MapCodec<VFXParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.FLOAT.fieldOf("r").forGetter(o -> o.r),
                    Codec.FLOAT.fieldOf("g").forGetter(o -> o.g),
                    Codec.FLOAT.fieldOf("b").forGetter(o -> o.b),
                    Codec.FLOAT.fieldOf("a").forGetter(o -> o.a)
            ).apply(instance, VFXParticleOptions::new)
    );

    // Deserializer used by ParticleType to read options from network/command (1.20.1 style)
    public static final ParticleOptions.Deserializer<VFXParticleOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<VFXParticleOptions>() {
                @Override
                public VFXParticleOptions fromCommand(ParticleType<VFXParticleOptions> type, StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
                    // command format: [r g b a] (floats, all optional with default 1.0)
                    float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;
                    
                    // Check if there are more arguments
                    if (reader.canRead() && reader.peek() == ' ') {
                        reader.expect(' ');
                        r = (float) reader.readDouble();
                        if (reader.canRead() && reader.peek() == ' ') {
                            reader.expect(' ');
                            g = (float) reader.readDouble();
                            if (reader.canRead() && reader.peek() == ' ') {
                                reader.expect(' ');
                                b = (float) reader.readDouble();
                                if (reader.canRead() && reader.peek() == ' ') {
                                    reader.expect(' ');
                                    a = (float) reader.readDouble();
                                }
                            }
                        }
                    }
                    return new VFXParticleOptions(r, g, b, a);
                }

                @Override
                public VFXParticleOptions fromNetwork(ParticleType<VFXParticleOptions> type, FriendlyByteBuf buf) {
                    float r = buf.readFloat();
                    float g = buf.readFloat();
                    float b = buf.readFloat();
                    float a = buf.readFloat();
                    return new VFXParticleOptions(r, g, b, a);
                }
            };

    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public VFXParticleOptions(float r, float g, float b, float a) {
        // clamp to 0..1 for safety
        this.r = Mth.clamp(r, 0f, 1f);
        this.g = Mth.clamp(g, 0f, 1f);
        this.b = Mth.clamp(b, 0f, 1f);
        this.a = Mth.clamp(a, 0f, 1f);
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.VFX_SOFT.get(); // <-- make sure this matches your registration
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.r);
        buf.writeFloat(this.g);
        buf.writeFloat(this.b);
        buf.writeFloat(this.a);
    }

    @Override
    public String writeToString() {
        // used by debug/command output; format as "r g b a"
        return String.format(Locale.ROOT, "%f %f %f %f", r, g, b, a);
    }


    
    public MapCodec<? extends ParticleOptions> codec() {
        return CODEC;
    }
}
