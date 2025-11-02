package mods.hexagonal.ar2.VFX;

import mods.hexagonal.ar2.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Random;

public class VFXthrust {

    private static final Random RANDOM = new Random();

    public static void spawn(ClientLevel world, double x, double y, double z) {

        // Spawn yellow thrust wisp
        if (world.isClientSide()) { // always spawn particles on the client
            for (int i = 0; i < 50; i++) { // spawn 5 particles per tick


                world.addParticle(
                        Registry.ROCKET_FLAME.get(),
                        x, y, z,
                        0, 0, 0
                );
            }
        }


        // For gray smoke, you can register a separate factory with gray RGB
        // or spawn it separately with a different particle type if needed
    }
}
