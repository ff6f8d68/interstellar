package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;
// Note: Lodestone OBJ model classes not available in current version

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles rendering of sun celestial bodies with particle effects.
 * Creates a realistic sun with corona effects and solar flares using particles.
 */
@Mod.EventBusSubscriber(modid = "interstellar", value = Dist.CLIENT)
public class SunRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();

    // OBJ model registry for 3D model rendering
    private static final OBJModelRegistry objModelRegistry = new OBJModelRegistry();

    // Particle configuration
    private static final int PARTICLE_COUNT = 150;
    private static final double PARTICLE_SPAWN_RADIUS = 1.5;
    private static final double PARTICLE_MAX_DISTANCE = 100.0;
    private static final float PARTICLE_SIZE_MIN = 0.05f;
    private static final float PARTICLE_SIZE_MAX = 0.2f;

    // Sun rendering configuration
    private static final float SUN_GLOW_INTENSITY = 1.2f;
    private static final float SUN_CORE_SIZE_MULTIPLIER = 0.8f;
    private static final float SUN_CORONA_SIZE_MULTIPLIER = 1.4f;

    // Particle system state
    private static final List<SunParticle> activeParticles = new ArrayList<>();
    private static final Random random = new Random();
    private static long lastParticleUpdate = 0;
    private static final long PARTICLE_UPDATE_INTERVAL = 50; // Update particles every 50ms

    /**
     * Checks if OBJ model is available for rendering.
     * Note: OBJ models not available in current Lodestone version.
     */
    private static boolean isOBJModelAvailable() {
        // OBJ models not available in current Lodestone version
        return false;
    }

    /**
     * Renders a sun celestial body with particle effects.
     */
    public static void renderSun(RenderLevelStageEvent event, CelestialBody sun, VertexConsumer buffer) {
        if (sun == null || !sun.isSun()) {
            return;
        }

        try {
            LOGGER.debug("Rendering sun: {}", sun.getDisplayName());

            // Check if OBJ model is available for 3D rendering
            if (isOBJModelAvailable()) {
                LOGGER.debug("Using OBJ model rendering for sun core");
                renderSunWithOBJ(event, sun);
            } else {
                LOGGER.debug("OBJ model not available, falling back to sprite rendering");
                renderSunCube(event, sun, buffer);
            }

            // Update and render particle effects (always preserved)
            updateAndRenderParticles(event, sun);

        } catch (Exception e) {
            LOGGER.error("Failed to render sun: {}", sun.getDisplayName(), e);
        }
    }

    /**
     * Renders the main sun using OBJ model with size-based scaling.
     * Note: OBJ models not available in current Lodestone version.
     */
    private static void renderSunWithOBJ(RenderLevelStageEvent event, CelestialBody sun) {
        // OBJ models not available in current Lodestone version
        LOGGER.debug("OBJ model rendering not available for sun: {} (Lodestone version limitation)", sun.getDisplayName());
        // Falls back to cube rendering in the main render method
    }

    /**
     * Renders the main sun cube using similar approach to planets but with sun-specific lighting.
     */
    private static void renderSunCube(RenderLevelStageEvent event, CelestialBody sun, VertexConsumer buffer) {
        PoseStack poseStack = event.getPoseStack();

        Matrix4f matrix;
        try {
            matrix = (Matrix4f) poseStack.last().pose().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        Vec3 posVec3 = sun.getCurrentPosition();
        Vector3d pos = new Vector3d(posVec3.x, posVec3.y, posVec3.z);

        // Position relative to camera
        matrix.translate((float) (pos.x - event.getCamera().getPosition().x),
                (float) (pos.y - event.getCamera().getPosition().y),
                (float) (pos.z - event.getCamera().getPosition().z));

        // Apply basic rotation (simplified for now)
        matrix.rotate(new Quaternionf().rotationXYZ(0, 0, 0));

        float halfSize = (float) (sun.getSize() / 2);

        // Bind the sun's texture before rendering
        bindSunTexture(sun);

        // Render sun cube faces with enhanced lighting
        renderSunCubeFaces(matrix, buffer, halfSize, sun);
    }

    /**
     * Renders the sun cube faces with enhanced lighting and glow effects.
     */
    private static void renderSunCubeFaces(Matrix4f matrix, VertexConsumer buffer, float halfSize, CelestialBody sun) {
        Vector3d lightDir = new Vector3d(0, 0, 1); // Sun light comes from center

        // Render core faces (inner cube)
        float coreHalfSize = halfSize * SUN_CORE_SIZE_MULTIPLIER;
        renderSunCubeLayer(matrix, buffer, coreHalfSize, sun, lightDir, 1.0f);

        // Render corona faces (outer glow)
        float coronaHalfSize = halfSize * SUN_CORONA_SIZE_MULTIPLIER;
        renderSunCubeLayer(matrix, buffer, coronaHalfSize, sun, lightDir, 0.3f);
    }

    /**
     * Renders a single layer of the sun cube (core or corona).
     */
    private static void renderSunCubeLayer(Matrix4f matrix, VertexConsumer buffer, float halfSize,
                                         CelestialBody sun, Vector3d lightDir, float alpha) {
        // Render all 6 faces of the cube
        addSunCubeFace(matrix, buffer, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize,
                      halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, sun, lightDir, alpha);
        addSunCubeFace(matrix, buffer, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize,
                      halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, sun, lightDir, alpha);
        addSunCubeFace(matrix, buffer, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize,
                      -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize, sun, lightDir, alpha);
        addSunCubeFace(matrix, buffer, halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize,
                      halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize, sun, lightDir, alpha);
        addSunCubeFace(matrix, buffer, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, -halfSize,
                      halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, sun, lightDir, alpha);
        addSunCubeFace(matrix, buffer, -halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize,
                      halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, sun, lightDir, alpha);
    }

    /**
     * Adds a cube face for sun rendering with enhanced lighting.
     */
    private static void addSunCubeFace(Matrix4f matrix, VertexConsumer buffer,
                                     float x1, float y1, float z1, float x2, float y2, float z2,
                                     float x3, float y3, float z3, float x4, float y4, float z4,
                                     CelestialBody sun, Vector3d lightDir, float alpha) {
        int[] rgb = hexToRgbInt(sun.getLightColor());
        int r = (int) (rgb[0] * SUN_GLOW_INTENSITY);
        int g = (int) (rgb[1] * SUN_GLOW_INTENSITY);
        int b = (int) (rgb[2] * SUN_GLOW_INTENSITY);

        // Clamp values to prevent overflow
        r = Math.min(255, r);
        g = Math.min(255, g);
        b = Math.min(255, b);

        Quaternionf rotation = new Quaternionf().rotationXYZ(0, 0, 0);

        addSunVertex(matrix, buffer, x1, y1, z1, r, g, b, alpha, lightDir, rotation);
        addSunVertex(matrix, buffer, x2, y2, z2, r, g, b, alpha, lightDir, rotation);
        addSunVertex(matrix, buffer, x3, y3, z3, r, g, b, alpha, lightDir, rotation);
        addSunVertex(matrix, buffer, x4, y4, z4, r, g, b, alpha, lightDir, rotation);
    }

    /**
     * Adds a vertex for sun rendering with enhanced lighting.
     */
    private static void addSunVertex(Matrix4f matrix, VertexConsumer buffer,
                                   float x, float y, float z, int r, int g, int b, float alpha,
                                   Vector3d lightDir, Quaternionf rotation) {
        // Calculate vertex normal and apply rotation
        org.joml.Vector3f vertexNormal = new org.joml.Vector3f(x, y, z).normalize();
        rotation.transform(vertexNormal);

        Vector3d worldNormal = new Vector3d(vertexNormal.x, vertexNormal.y, vertexNormal.z);
        float lighting = (float) Math.max(0.2, worldNormal.dot(lightDir) * 0.8 + 0.2); // Minimum 20% lighting

        // Apply lighting to color with sun glow effect
        int litR = (int) (r * lighting * SUN_GLOW_INTENSITY);
        int litG = (int) (g * lighting * SUN_GLOW_INTENSITY);
        int litB = (int) (b * lighting * SUN_GLOW_INTENSITY);

        // Clamp final values
        litR = Math.min(255, litR);
        litG = Math.min(255, litG);
        litB = Math.min(255, litB);

        // Add texture coordinates (simplified)
        float u = (x < 0 ? 0 : 0.25f) + (y < 0 ? 0 : 0.5f);
        float v = z < 0 ? 0 : 1;

        buffer.vertex(matrix, x, y, z)
              .uv(u, v)
              .color(litR, litG, litB, (int) (alpha * 255))
              .endVertex();
    }

    /**
     * Updates and renders particle effects for the sun.
     */
    private static void updateAndRenderParticles(RenderLevelStageEvent event, CelestialBody sun) {
        long currentTime = System.currentTimeMillis();

        // Update particles periodically
        if (currentTime - lastParticleUpdate > PARTICLE_UPDATE_INTERVAL) {
            updateParticles(sun);
            lastParticleUpdate = currentTime;
        }

        // Spawn new particles if needed
        spawnParticles(sun);

        // Render active particles
        renderParticles(event, sun);
    }

    /**
     * Updates existing particles (movement, aging, etc.).
     */
    private static void updateParticles(CelestialBody sun) {
        Vec3 sunPos = sun.getCurrentPosition();

        activeParticles.removeIf(particle -> {
            // Age particles
            particle.age += PARTICLE_UPDATE_INTERVAL;

            // Update position based on velocity
            particle.x += particle.velocityX * (PARTICLE_UPDATE_INTERVAL / 1000.0);
            particle.y += particle.velocityY * (PARTICLE_UPDATE_INTERVAL / 1000.0);
            particle.z += particle.velocityZ * (PARTICLE_UPDATE_INTERVAL / 1000.0);

            // Check if particle is too far from sun or too old
            double distanceFromSun = Math.sqrt(
                Math.pow(particle.x - sunPos.x, 2) +
                Math.pow(particle.y - sunPos.y, 2) +
                Math.pow(particle.z - sunPos.z, 2)
            );

            return particle.age > particle.maxAge || distanceFromSun > PARTICLE_MAX_DISTANCE;
        });

        LOGGER.debug("Updated particles: {} active", activeParticles.size());
    }

    /**
     * Spawns new particles around the sun.
     */
    private static void spawnParticles(CelestialBody sun) {
        Vec3 sunPos = sun.getCurrentPosition();
        int particlesToSpawn = Math.max(0, PARTICLE_COUNT - activeParticles.size());

        for (int i = 0; i < particlesToSpawn; i++) {
            // Spawn particle within sun's radius
            double spawnRadius = sun.getSize() * PARTICLE_SPAWN_RADIUS;
            double angle1 = random.nextDouble() * 2 * Math.PI;
            double angle2 = random.nextDouble() * 2 * Math.PI;

            double x = sunPos.x + spawnRadius * Math.sin(angle1) * Math.cos(angle2);
            double y = sunPos.y + spawnRadius * Math.sin(angle1) * Math.sin(angle2);
            double z = sunPos.z + spawnRadius * Math.cos(angle1);

            // Random velocity for particle movement
            double velocity = 2.0 + random.nextDouble() * 3.0;
            double velAngle1 = random.nextDouble() * 2 * Math.PI;
            double velAngle2 = random.nextDouble() * 2 * Math.PI;

            double velocityX = velocity * Math.sin(velAngle1) * Math.cos(velAngle2);
            double velocityY = velocity * Math.sin(velAngle1) * Math.sin(velAngle2);
            double velocityZ = velocity * Math.cos(velAngle1);

            // Random size and age
            float size = PARTICLE_SIZE_MIN + random.nextFloat() * (PARTICLE_SIZE_MAX - PARTICLE_SIZE_MIN);
            long maxAge = 3000 + random.nextInt(5000); // 3-8 seconds

            SunParticle particle = new SunParticle(x, y, z, velocityX, velocityY, velocityZ, size, maxAge);
            activeParticles.add(particle);
        }
    }

    /**
     * Renders all active particles.
     */
    private static void renderParticles(RenderLevelStageEvent event, CelestialBody sun) {
        if (activeParticles.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (level == null) {
            return;
        }

        // Set up particle rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        try {
            for (SunParticle particle : activeParticles) {
                renderParticle(event, particle, sun);
            }
        } finally {
            // Restore state
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }

    /**
     * Renders a single particle.
     */
    private static void renderParticle(RenderLevelStageEvent event, SunParticle particle, CelestialBody sun) {
        PoseStack poseStack = event.getPoseStack();

        Matrix4f matrix;
        try {
            matrix = (Matrix4f) poseStack.last().pose().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        // Position relative to camera
        matrix.translate((float) (particle.x - event.getCamera().getPosition().x),
                (float) (particle.y - event.getCamera().getPosition().y),
                (float) (particle.z - event.getCamera().getPosition().z));

        // Get buffer for particle rendering
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(CelestialRenderTypes.getSunRenderType());

        // Calculate particle color based on sun color and age
        int[] rgb = hexToRgbInt(sun.getLightColor());
        float ageRatio = (float) particle.age / particle.maxAge;
        float alpha = (1.0f - ageRatio) * 0.8f; // Fade out over time

        int r = (int) (rgb[0] * alpha);
        int g = (int) (rgb[1] * alpha);
        int b = (int) (rgb[2] * alpha);

        float halfSize = particle.size / 2;

        // Render particle as a small quad
        buffer.vertex(matrix, -halfSize, -halfSize, 0)
              .uv(0, 0)
              .color(r, g, b, (int) (alpha * 255))
              .endVertex();
        buffer.vertex(matrix, halfSize, -halfSize, 0)
              .uv(1, 0)
              .color(r, g, b, (int) (alpha * 255))
              .endVertex();
        buffer.vertex(matrix, halfSize, halfSize, 0)
              .uv(1, 1)
              .color(r, g, b, (int) (alpha * 255))
              .endVertex();
        buffer.vertex(matrix, -halfSize, halfSize, 0)
              .uv(0, 1)
              .color(r, g, b, (int) (alpha * 255))
              .endVertex();

        bufferSource.endBatch();
    }

    /**
     * Binds the texture for a sun celestial body before rendering.
     */
    private static void bindSunTexture(CelestialBody celestialBody) {
        try {
            ResourceLocation textureLocation = celestialBody.getTexture();
            LOGGER.debug("Binding texture for sun: {}: {}", celestialBody.getDisplayName(), textureLocation);

            // Get the texture manager and bind the texture
            Minecraft.getInstance().getTextureManager().bindForSetup(textureLocation);
        } catch (Exception e) {
            LOGGER.warn("Failed to bind texture for sun: {}", celestialBody.getDisplayName(), e);
        }
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

    /**
     * Represents a sun particle for the particle system.
     */
    private static class SunParticle {
        double x, y, z;
        double velocityX, velocityY, velocityZ;
        float size;
        long age;
        long maxAge;

        SunParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                   float size, long maxAge) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
            this.size = size;
            this.maxAge = maxAge;
            this.age = 0;
        }
    }
}