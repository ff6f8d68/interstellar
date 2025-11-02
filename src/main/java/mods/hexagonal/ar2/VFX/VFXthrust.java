package mods.hexagonal.ar2.VFX;

import mods.hexagonal.ar2.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Random;

public class VFXthrust {

    private static final Random RANDOM = new Random();

    public static void spawn(ClientLevel world, double x, double y, double z) {
        double offsetX = (RANDOM.nextDouble() - 0.5) * 0.2;
        double offsetY = (RANDOM.nextDouble() - 0.5) * 0.2;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.2;

        // Spawn yellow thrust wisp
        world.addParticle(ModParticles.VFX.get(),
                x + offsetX, y + offsetY, z + offsetZ,
                0, 0, 0);

        // For gray smoke, you can register a separate factory with gray RGB
        // or spawn it separately with a different particle type if needed
    }
}
