package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles integration with Minecraft's sky rendering system.
 * Manages celestial body registration, render order, and visibility.
 */
@Mod.EventBusSubscriber(modid = "interstellar", value = Dist.CLIENT)
public class SkyRenderHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Registry of celestial bodies by dimension
    private static final ConcurrentHashMap<ResourceKey<Level>, List<CelestialBody>> celestialBodiesByDimension = new ConcurrentHashMap<>();

    // Rendering configuration
    private static final float CELESTIAL_BODY_RENDER_DISTANCE = 1000.0f;
    private static final int MAX_CELESTIAL_BODIES_PER_DIMENSION = 50;

    /**
     * Registers a celestial body for rendering in a specific dimension.
     *
     * @param celestialBody The celestial body to register
     * @param dimension The dimension to register it in
     */
    public static void registerCelestialBody(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (celestialBody == null || dimension == null) {
            LOGGER.warn("Attempted to register null celestial body or dimension");
            return;
        }


        celestialBodiesByDimension.computeIfAbsent(dimension, k -> new ArrayList<>());

        List<CelestialBody> bodiesInDimension = celestialBodiesByDimension.get(dimension);

        // Check if already registered
        boolean alreadyRegistered = bodiesInDimension.stream()
                .anyMatch(existing -> existing.equals(celestialBody));

        if (!alreadyRegistered) {
            if (bodiesInDimension.size() >= MAX_CELESTIAL_BODIES_PER_DIMENSION) {
                LOGGER.warn("Maximum celestial bodies ({}) reached for dimension {}",
                    MAX_CELESTIAL_BODIES_PER_DIMENSION, dimension.location());
                return;
            }

            bodiesInDimension.add(celestialBody);
        } else {
            LOGGER.debug("Celestial body '{}' already registered in dimension '{}'",
                celestialBody.getDisplayName(), dimension.location().getPath());
        }
    }

    /**
     * Unregisters a celestial body from rendering in a specific dimension.
     *
     * @param celestialBody The celestial body to unregister
     * @param dimension The dimension to unregister it from
     */
    public static void unregisterCelestialBody(CelestialBody celestialBody, ResourceKey<Level> dimension) {
        if (celestialBody == null || dimension == null) {
            return;
        }

        List<CelestialBody> bodiesInDimension = celestialBodiesByDimension.get(dimension);
        if (bodiesInDimension != null) {
            bodiesInDimension.removeIf(existing -> existing.equals(celestialBody));
            LOGGER.debug("Unregistered celestial body {} from dimension {}",
                celestialBody.getDisplayName(), dimension.location().getPath());
        }
    }

    /**
     * Unregisters a celestial body by name from rendering in a specific dimension.
     *
     * @param name The name of the celestial body to unregister
     * @param dimension The dimension to unregister it from
     */
    public static void unregisterCelestialBodyByName(String name, ResourceKey<Level> dimension) {
        if (name == null || dimension == null) {
            LOGGER.warn("Attempted to unregister celestial body with null name or dimension");
            return;
        }

        List<CelestialBody> bodiesInDimension = celestialBodiesByDimension.get(dimension);
        if (bodiesInDimension != null) {
            boolean removed = bodiesInDimension.removeIf(existing -> name.equals(existing.getDisplayName()));
            if (removed) {
                LOGGER.debug("Unregistered celestial body '{}' from dimension {}",
                    name, dimension.location().getPath());
            } else {
                LOGGER.debug("Celestial body '{}' not found in dimension {}",
                    name, dimension.location().getPath());
            }
        } else {
            LOGGER.debug("No celestial bodies registered in dimension {}", dimension.location().getPath());
        }
    }

    /**
     * Gets all celestial bodies registered for a specific dimension.
     *
     * @param dimension The dimension to get celestial bodies for
     * @return List of celestial bodies in the dimension
     */
    public static List<CelestialBody> getCelestialBodiesForDimension(ResourceKey<Level> dimension) {
        return celestialBodiesByDimension.getOrDefault(dimension, new ArrayList<>());
    }

    /**
     * Clears all celestial bodies from a specific dimension.
     *
     * @param dimension The dimension to clear
     */
    public static void clearDimension(ResourceKey<Level> dimension) {
        List<CelestialBody> removed = celestialBodiesByDimension.remove(dimension);
        if (removed != null && !removed.isEmpty()) {
            LOGGER.info("Cleared {} celestial bodies from dimension {}",
                removed.size(), dimension.location().getPath());
        }
    }

    /**
     * Clears all celestial bodies from all dimensions.
     */
    public static void clearAllDimensions() {
        int totalCleared = celestialBodiesByDimension.values().stream()
                .mapToInt(List::size)
                .sum();
        celestialBodiesByDimension.clear();
        LOGGER.info("Cleared all celestial bodies ({} total)", totalCleared);
    }

    /**
     * Handles the sky rendering event to render celestial bodies.
     * This method is called during the sky rendering stage.
     */
    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (level == null) {
            LOGGER.debug("Render sky event: level is null");
            return;
        }

        // Get celestial bodies for current dimension
        List<CelestialBody> celestialBodies = getCelestialBodiesForDimension(level.dimension());

        if (celestialBodies.isEmpty()) {
            return;
        }

        // Check dimension validation
        if (!shouldRenderCelestialBodies(level)) {
            return;
        }

        // Update orbital positions
        updateCelestialBodyOrbits(celestialBodies);

        // Set up rendering state
        setupCelestialRendering();

        // Render celestial bodies using the updated API
        CelestialRenderer.renderCelestialBodies(event, celestialBodies);

        // Clean up rendering state
        cleanupCelestialRendering();
    }

    /**
     * Updates orbital positions for all celestial bodies.
     */
    private static void updateCelestialBodyOrbits(List<CelestialBody> celestialBodies) {
        for (CelestialBody celestialBody : celestialBodies) {
            if (celestialBody.isInOrbit()) {
                celestialBody.updateOrbit();
            }
        }
    }

    /**
     * Sets up OpenGL state for celestial body rendering.
     */
    private static void setupCelestialRendering() {
        // Enable depth testing but disable depth writing for sky objects
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        // Enable blending for smooth celestial body rendering
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                              GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Disable face culling for sphere rendering
        RenderSystem.disableCull();

        // Set up fog for distant celestial bodies
        RenderSystem.setShaderFogStart(0.0f);
        RenderSystem.setShaderFogEnd(CELESTIAL_BODY_RENDER_DISTANCE);
    }

    /**
     * Cleans up OpenGL state after celestial body rendering.
     */
    private static void cleanupCelestialRendering() {
        // Re-enable depth writing
        RenderSystem.depthMask(true);

        // Re-enable face culling
        RenderSystem.enableCull();

        // Reset fog settings
        RenderSystem.setShaderFogStart(0.0f);
        RenderSystem.setShaderFogEnd(1.0f);
    }

    /**
     * Checks if celestial bodies should be rendered in the current context.
     * This includes visibility checks and performance considerations.
     *
     * @param level The current level
     * @return true if celestial bodies should be rendered
     */
    private static boolean shouldRenderCelestialBodies(ClientLevel level) {
        // Allow rendering in all dimensions - users can create celestial bodies anywhere
        // This provides more flexibility and better user experience

        // Don't render if player is in a vehicle or underwater (performance)
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        // Check if player is underwater
        if (minecraft.player.isUnderWater()) {
            return false;
        }

        // Check if GUI is hidden (F1 mode)
        if (minecraft.options.hideGui) {
            return false;
        }

        return true;
    }

    /**
     * Gets the number of celestial bodies registered across all dimensions.
     *
     * @return Total count of registered celestial bodies
     */
    public static int getTotalCelestialBodyCount() {
        return celestialBodiesByDimension.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Gets the number of dimensions with registered celestial bodies.
     *
     * @return Number of dimensions with celestial bodies
     */
    public static int getDimensionCount() {
        return celestialBodiesByDimension.size();
    }

    /**
     * Initializes the sky render handler.
     * Called during mod initialization.
     */
    public static void initialize() {
        LOGGER.info("SkyRenderHandler initialized");
        clearAllDimensions(); // Start with clean state
    }

    /**
     * Shuts down the sky render handler.
     * Called when the mod is shutting down.
     */
    public static void shutdown() {
        LOGGER.info("SkyRenderHandler shutting down");
        clearAllDimensions();
    }
}