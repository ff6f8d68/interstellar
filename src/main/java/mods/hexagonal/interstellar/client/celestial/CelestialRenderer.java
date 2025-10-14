package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;

import java.util.List;

/**
 * Handles rendering of celestial bodies using the correct VertexConsumer approach.
 * Based on the Genesis PlanetRenderer for proper rendering pipeline integration.
 */
public class CelestialRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Renders all celestial bodies using the event-based approach.
     */
    public static void renderCelestialBodies(RenderLevelStageEvent event, List<CelestialBody> celestialBodies) {
        if (celestialBodies.isEmpty()) {
            return;
        }

        Level level = event.getCamera().getEntity().level();
        if (level == null) {
            return;
        }

        // Log dimension for debugging
        LOGGER.debug("CelestialRenderer: rendering in dimension {}", level.dimension().location().getPath());

        // Set up proper OpenGL state like Genesis example
        setupRenderingState();

        try {
            // Render planets first
            renderPlanets(event, celestialBodies);

            // Render suns
            renderSuns(event, celestialBodies);
        } finally {
            // Always restore state
            restoreRenderingState();
        }
    }

    /**
     * Checks if we're in a space dimension.
     */
    private static boolean isSpaceDimension(Level level) {
        boolean isSpace = level.dimension().location().getPath().equals("space_dimension");
        return isSpace;
    }

    /**
     * Sets up OpenGL state for celestial body rendering (matching Genesis approach).
     */
    private static void setupRenderingState() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.enableCull();
    }

    /**
     * Restores OpenGL state after celestial body rendering.
     */
    private static void restoreRenderingState() {
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    /**
      * Renders all planet-type celestial bodies.
      */
     private static void renderPlanets(RenderLevelStageEvent event, List<CelestialBody> celestialBodies) {
         LOGGER.debug("Rendering {} planet-type celestial bodies", celestialBodies.stream().mapToInt(body -> body.isSun() ? 0 : 1).sum());

         MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

         // Use the custom planet render type for proper celestial body rendering
         VertexConsumer planetBuffer = bufferSource.getBuffer(CelestialRenderTypes.getPlanetRenderType());

        for (CelestialBody celestialBody : celestialBodies) {
            if (!celestialBody.isSun()) {
                LOGGER.debug("Rendering planet: {}", celestialBody.getDisplayName());
                renderPlanet(event, celestialBody, planetBuffer);
            }
        }

        bufferSource.endBatch();
    }

    /**
      * Renders all sun-type celestial bodies.
      */
     private static void renderSuns(RenderLevelStageEvent event, List<CelestialBody> celestialBodies) {
         LOGGER.debug("Rendering {} sun-type celestial bodies", celestialBodies.stream().mapToInt(body -> body.isSun() ? 1 : 0).sum());

         MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

         // Use the custom sun render type for proper celestial body rendering
         VertexConsumer sunBuffer = bufferSource.getBuffer(CelestialRenderTypes.getSunRenderType());

        for (CelestialBody celestialBody : celestialBodies) {
            if (celestialBody.isSun()) {
                LOGGER.debug("Rendering sun: {}", celestialBody.getDisplayName());
                renderSun(event, celestialBody, sunBuffer);
            }
        }

        bufferSource.endBatch();
    }

    /**
     * Renders a single planet celestial body using VertexConsumer approach.
     */
    private static void renderPlanet(RenderLevelStageEvent event, CelestialBody planet, VertexConsumer buffer) {
        PoseStack poseStack = event.getPoseStack();

        Matrix4f matrix;
        try {
            matrix = (Matrix4f) poseStack.last().pose().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        Vec3 posVec3 = planet.getCurrentPosition();
        Vector3d pos = new Vector3d(posVec3.x, posVec3.y, posVec3.z);

        // Position relative to camera
        matrix.translate((float) (pos.x - event.getCamera().getPosition().x),
                (float) (pos.y - event.getCamera().getPosition().y),
                (float) (pos.z - event.getCamera().getPosition().z));

        // Apply basic rotation (simplified for now)
        matrix.rotate(new Quaternionf().rotationXYZ(0, 0, 0));

        float halfSize = (float) (planet.getSize() / 2);

        // Render cube faces for the planet
        renderPlanetCube(matrix, buffer, halfSize, planet);
    }

    /**
     * Renders a single sun celestial body using VertexConsumer approach.
     */
    private static void renderSun(RenderLevelStageEvent event, CelestialBody sun, VertexConsumer buffer) {
        SunRenderer.renderSun(event, buffer);
    }

    /**
     * Renders a planet as a cube with proper lighting.
     */
    private static void renderPlanetCube(Matrix4f matrix, VertexConsumer buffer, float halfSize, CelestialBody planet) {
        Vec3 planetPosVec3 = planet.getCurrentPosition();
        Vector3d planetPos = new Vector3d(planetPosVec3.x, planetPosVec3.y, planetPosVec3.z);
        Vector3d lightDir = new Vector3d(-planetPos.x, -planetPos.y, -planetPos.z).normalize();

        // Render all 6 faces of the cube
        addCubeFacePlanet(matrix, buffer, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, planet, lightDir);
        addCubeFacePlanet(matrix, buffer, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, planet, lightDir);
        addCubeFacePlanet(matrix, buffer, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize, planet, lightDir);
        addCubeFacePlanet(matrix, buffer, halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize, planet, lightDir);
        addCubeFacePlanet(matrix, buffer, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, planet, lightDir);
        addCubeFacePlanet(matrix, buffer, -halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, planet, lightDir);
    }

    /**
     * Adds a cube face for planet rendering with proper lighting.
     */
    private static void addCubeFacePlanet(Matrix4f matrix, VertexConsumer buffer,
                                        float x1, float y1, float z1, float x2, float y2, float z2,
                                        float x3, float y3, float z3, float x4, float y4, float z4,
                                        CelestialBody planet, Vector3d lightDir) {
        int[] rgb = hexToRgbInt(planet.getLightColor());
        int r = rgb[0], g = rgb[1], b = rgb[2];

        Quaternionf rotation = new Quaternionf().rotationXYZ(0, 0, 0);

        addVertexWithLighting(matrix, buffer, x1, y1, z1, r, g, b, lightDir, rotation);
        addVertexWithLighting(matrix, buffer, x2, y2, z2, r, g, b, lightDir, rotation);
        addVertexWithLighting(matrix, buffer, x3, y3, z3, r, g, b, lightDir, rotation);
        addVertexWithLighting(matrix, buffer, x4, y4, z4, r, g, b, lightDir, rotation);
    }

    /**
     * Adds a vertex with proper lighting calculations.
     */
    private static void addVertexWithLighting(Matrix4f matrix, VertexConsumer buffer,
                                            float x, float y, float z, int r, int g, int b,
                                            Vector3d lightDir, Quaternionf rotation) {
        // Calculate vertex normal and apply rotation
        org.joml.Vector3f vertexNormal = new org.joml.Vector3f(x, y, z).normalize();
        rotation.transform(vertexNormal);

        Vector3d worldNormal = new Vector3d(vertexNormal.x, vertexNormal.y, vertexNormal.z);
        float lighting = (float) Math.max(0.0, worldNormal.dot(lightDir));

        // Apply lighting to color
        int litR = (int) (r * lighting);
        int litG = (int) (g * lighting);
        int litB = (int) (b * lighting);

        // Add some texture coordinates (simplified)
        float u = (x < 0 ? 0 : 0.25f) + (y < 0 ? 0 : 0.5f);
        float v = z < 0 ? 0 : 1;

        buffer.vertex(matrix, x, y, z)
              .color(litR, litG, litB, 255)
              .uv(u, v)
              .endVertex();
    }

    /**
     * Converts hex color to RGB int array.
     */
    private static int[] hexToRgbInt(int hexColor) {
        int r = (hexColor >> 16) & 0xFF;
        int g = (hexColor >> 8) & 0xFF;
        int b = hexColor & 0xFF;
        return new int[]{r, g, b};
    }
}