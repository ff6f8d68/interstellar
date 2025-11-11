package team.nextlevelmodding.ar2.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import team.nextlevelmodding.ar2.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public record VFXParticleOptions(float r, float g, float b) implements ParticleOptions {

    public static final Codec<VFXParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("r").forGetter(VFXParticleOptions::r),
                    Codec.FLOAT.fieldOf("g").forGetter(VFXParticleOptions::g),
                    Codec.FLOAT.fieldOf("b").forGetter(VFXParticleOptions::b)
            ).apply(instance, VFXParticleOptions::new)
    );

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
    }

    public static final ParticleOptions.Deserializer<VFXParticleOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<VFXParticleOptions>() {
                @Override
                public VFXParticleOptions fromCommand(ParticleType<VFXParticleOptions> type, StringReader reader) throws CommandSyntaxException {
                    float r = reader.readFloat();
                    reader.expect(' ');
                    float g = reader.readFloat();
                    reader.expect(' ');
                    float b = reader.readFloat();
                    return new VFXParticleOptions(r, g, b);
                }

                @Override
                public VFXParticleOptions fromNetwork(ParticleType<VFXParticleOptions> particleType, FriendlyByteBuf buf) {
                    return new VFXParticleOptions(buf.readFloat(), buf.readFloat(), buf.readFloat());
                }


            };

    @Override
    public ParticleType<?> getType() {
        return ModParticles.VFX.get(); // the registered particle type
    }

    @Override
    public String writeToString() {
        return r + " " + g + " " + b;
    }
}
