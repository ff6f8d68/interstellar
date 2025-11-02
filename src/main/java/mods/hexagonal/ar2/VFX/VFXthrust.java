package mods.hexagonal.ar2.VFX;

import mods.hexagonal.ar2.ModParticles;
import mods.hexagonal.ar2.particles.VFXParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Random;

public class VFXthrust {

    private static final Random RANDOM = new Random();

    public static void spawn(ClientLevel world, double x, double y, double z) {
        double offsetX = (RANDOM.nextDouble() - 0.5) * 0.2;
        double offsetY = (RANDOM.nextDouble() - 0.5) * 0.2;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.2;

        // Yellow thrust
        world.addParticle(new VFXParticleOptions(1.0f, 0.9f, 0.2f),
                x + offsetX, y + offsetY, z + offsetZ,
                0, 0, 0);

        // Gray smoke
        world.addParticle(new VFXParticleOptions(0.5f, 0.5f, 0.5f),
                x + offsetX, y + offsetY, z + offsetZ,
                0, 0, 0);
    }
}
