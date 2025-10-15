package mods.hexagonal.interstellar.client.celestial;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Registry for OBJ models used in celestial body rendering.
 * Custom OBJ loader implementation for Minecraft 1.20.1 without Lodestone dependency.
 */
@Mod.EventBusSubscriber(modid = "interstellar", value = Dist.CLIENT)
public class OBJModelRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MOD_ID = "interstellar";

    // Model registry keys
    public static final ResourceLocation PLANET_MODEL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "planet");

    // Model data storage
    private static OBJModel planetModel;
    private static boolean modelsRegistered = false;

    /**
     * Registers all OBJ models during client setup.
     * This method is called automatically by the event bus.
     */
    @SubscribeEvent
    public static void registerModels(FMLClientSetupEvent event) {
        LOGGER.info("Registering celestial OBJ models...");

        event.enqueueWork(() -> {
            try {
                registerPlanetModel();
                modelsRegistered = true;
                LOGGER.info("Celestial OBJ model registration completed successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to register celestial OBJ models", e);
                modelsRegistered = false;
            }
        });
    }

    /**
     * Registers the planet model by loading the OBJ file.
     */
    private static void registerPlanetModel() {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/planet.obj");

            var resourceOptional = resourceManager.getResource(modelLocation);
            if (resourceOptional.isEmpty()) {
                throw new IOException("Could not find planet.obj resource at: " + modelLocation);
            }
            Resource resource = resourceOptional.get();

            try (InputStream inputStream = resource.open();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                planetModel = parseOBJFile(reader);
                LOGGER.info("Successfully loaded planet.obj with {} vertices and {} faces",
                          planetModel.vertices.size(), planetModel.faces.size());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to register planet model", e);
            throw new RuntimeException("Planet model registration failed", e);
        }
    }

    /**
     * Parses an OBJ file and creates an OBJModel.
     */
    private static OBJModel parseOBJFile(BufferedReader reader) throws IOException {
        OBJModel model = new OBJModel();
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip empty lines and comments
            }

            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;

            String type = parts[0];

            switch (type) {
                case "v":
                    // Vertex position: v x y z
                    if (parts.length >= 4) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        float z = Float.parseFloat(parts[3]);
                        model.vertices.add(new Vector3f(x, y, z));
                    }
                    break;

                case "vt":
                    // Vertex texture coordinate: vt u v
                    if (parts.length >= 3) {
                        float u = Float.parseFloat(parts[1]);
                        float v = Float.parseFloat(parts[2]);
                        model.texCoords.add(new Vector3f(u, v, 0));
                    }
                    break;

                case "vn":
                    // Vertex normal: vn x y z
                    if (parts.length >= 4) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        float z = Float.parseFloat(parts[3]);
                        model.normals.add(new Vector3f(x, y, z));
                    }
                    break;

                case "f":
                    // Face: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
                    if (parts.length >= 4) {
                        OBJFace face = new OBJFace();
                        for (int i = 1; i < parts.length; i++) {
                            String[] indices = parts[i].split("/");
                            if (indices.length >= 1 && !indices[0].isEmpty()) {
                                int vertexIndex = Integer.parseInt(indices[0]) - 1; // OBJ indices are 1-based
                                face.vertexIndices.add(vertexIndex);

                                if (indices.length >= 2 && !indices[1].isEmpty()) {
                                    int texCoordIndex = Integer.parseInt(indices[1]) - 1;
                                    face.texCoordIndices.add(texCoordIndex);
                                }

                                if (indices.length >= 3 && !indices[2].isEmpty()) {
                                    int normalIndex = Integer.parseInt(indices[2]) - 1;
                                    face.normalIndices.add(normalIndex);
                                }
                            }
                        }
                        model.faces.add(face);
                    }
                    break;
            }
        }

        return model;
    }

    /**
     * Gets the registered planet model for use in renderers.
     */
    public static OBJModel getPlanetModel() {
        return planetModel;
    }

    /**
     * Checks if the planet model is registered and available.
     */
    public static boolean isPlanetModelRegistered() {
        return modelsRegistered && planetModel != null;
    }

    /**
     * Sets the scale for the planet model based on celestial object size.
     */
    public static void setPlanetModelScale(float scale) {
        if (planetModel != null) {
            planetModel.scale = scale;
        }
    }

    /**
     * Internal class representing an OBJ model.
     */
    public static class OBJModel {
        public List<Vector3f> vertices = new ArrayList<>();
        public List<Vector3f> texCoords = new ArrayList<>();
        public List<Vector3f> normals = new ArrayList<>();
        public List<OBJFace> faces = new ArrayList<>();
        public float scale = 1.0f;
    }

    /**
     * Internal class representing an OBJ face.
     */
    public static class OBJFace {
        public List<Integer> vertexIndices = new ArrayList<>();
        public List<Integer> texCoordIndices = new ArrayList<>();
        public List<Integer> normalIndices = new ArrayList<>();
    }
}