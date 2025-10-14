package mods.hexagonal.interstellar;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = "interstellar", bus = Mod.EventBusSubscriber.Bus.MOD)
public class BeyondOxygenIntegrator {
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("beyond_oxygen-common.toml");

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                if (!Files.exists(CONFIG_PATH)) {
                    // If the config doesn't exist yet, create it with your entry
                    Files.createDirectories(CONFIG_PATH.getParent());
                    Files.writeString(CONFIG_PATH,
                            "unbreathableDimensions = [\"interstellar:space\"]\n");
                    return;
                }

                // Read the file
                String content = Files.readString(CONFIG_PATH);
                if (!content.contains("interstellar:space")) {
                    // Insert your dimension safely
                    int idx = content.indexOf("unbreathableDimensions");
                    if (idx != -1) {
                        content = content.replace("]", ", \"interstellar:space\"]");
                    }
                    Files.writeString(CONFIG_PATH, content);
                }
            } catch (IOException e) {
                System.err.println("[Interstellar] Failed to patch Beyond Oxygen config: " + e.getMessage());
            }
        });
    }
}
