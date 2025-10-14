package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.List;

/**
 * Handles rendering of celestial bodies in the sky with cubemap texture support.
 * Supports both sun and planet rendering with proper scaling and lighting effects.
 */
public class CelestialRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Shader uniforms for celestial body rendering
    private static final String SUN_BRIGHTNESS_UNIFORM = "SunBrightness";
    private static final String LIGHT_COLOR_UNIFORM = "LightColor";

    // Rendering constants
    private static final float SKY_DISTANCE = 100.0f;
    private static final float BASE_SPHERE_SIZE = 30.0f;

    /**
     * Renders all provided celestial bodies in the sky.
     *
     * @param celestialBodies List of celestial bodies to render
     * @param partialTick Partial tick for smooth animation
     * @param poseStack Current pose stack
     * @param buffer Buffer source for rendering
     */
    public static void renderCelestialBodies(List<CelestialBody> celestialBodies, float partialTick,
                                           PoseStack poseStack, MultiBufferSource buffer) {
        if (celestialBodies.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        ClientLevel level = minecraft.level;

        if (level == null) {
            return;
        }

        // Enable blending for transparent celestial bodies
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Render each celestial body
        for (CelestialBody celestialBody : celestialBodies) {
            if (shouldRenderCelestialBody(celestialBody, level)) {
                renderCelestialBody(celestialBody, camera, poseStack, buffer, partialTick);
            }
        }

        RenderSystem.disableBlend();
    }

    /**
     * Determines if a celestial body should be rendered based on current conditions.
     */
    private static boolean shouldRenderCelestialBody(CelestialBody celestialBody, ClientLevel level) {
        // Only render in space dimension or if specifically configured
        return level.dimension().location().getPath().equals("space_dimension");
    }

    /**
     * Renders a single celestial body as a sphere with cubemap texture.
     */
    private static void renderCelestialBody(CelestialBody celestialBody, Camera camera,
                                          PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        Vec3 bodyPos = celestialBody.getCurrentPosition();

        // Calculate direction and distance from camera to celestial body
        Vec3 direction = bodyPos.subtract(cameraPos);
        double distance = direction.length();

        // Skip if too far away (optimization)
        if (distance > 1000.0) {
            return;
        }

        // Calculate screen position for the celestial body
        Vec3 normalizedDirection = direction.normalize();
        Vec3 renderPos = cameraPos.add(normalizedDirection.scale(SKY_DISTANCE));

        // Calculate size based on distance and celestial body size
        float size = calculateRenderSize(celestialBody, distance);

        // Set up transformations
        poseStack.pushPose();
        poseStack.translate(renderPos.x - cameraPos.x, renderPos.y - cameraPos.y, renderPos.z - cameraPos.z);

        // Rotate to face camera
        rotateToFaceCamera(poseStack, camera);

        // Scale based on size and distance
        poseStack.scale(size, size, size);

        // Render the celestial body
        if (celestialBody.isSun()) {
            renderSun(celestialBody, poseStack, buffer);
        } else {
            renderPlanet(celestialBody, poseStack, buffer);
        }

        poseStack.popPose();
    }

    /**
     * Calculates the appropriate render size for a celestial body based on its size and distance.
     */
    private static float calculateRenderSize(CelestialBody celestialBody, double distance) {
        float baseSize = BASE_SPHERE_SIZE;
        float sizeMultiplier = celestialBody.getSize() / 100.0f; // Normalize size

        // Scale based on distance (closer = larger)
        float distanceScale = (float) (SKY_DISTANCE / Math.max(distance, 10.0));

        return baseSize * sizeMultiplier * distanceScale;
    }

    /**
     * Rotates the pose stack to face the camera for proper billboard effect.
     */
    private static void rotateToFaceCamera(PoseStack poseStack, Camera camera) {
        // Get camera rotation
        float cameraYaw = camera.getYRot();
        float cameraPitch = camera.getXRot();

        // Apply rotations to face camera
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraPitch));
    }

    /**
     * Renders a sun celestial body with layered lighting effects for realistic appearance.
     */
    private static void renderSun(CelestialBody sun, PoseStack poseStack, MultiBufferSource buffer) {
        // Render sun with layered halo effect
        renderSunLayered(sun, poseStack, buffer);
    }

    /**
     * Renders a sun with multiple layered spheres to create a realistic glowing effect.
     * Layer structure:
     * - Layer 1 (core): 1.0x size, 1.0 opacity - main sun body
     * - Layer 2: 1.2x size, 0.7 opacity - inner halo
     * - Layer 3: 1.5x size, 0.4 opacity - middle halo
     * - Layer 4: 2.0x size, 0.2 opacity - outer halo
     *
     * Performance optimizations:
     * - Early return for very small suns that won't be visible
     * - Skip layers with very low opacity for performance
     * - Reuse shader uniforms where possible
     */
    private static void renderSunLayered(CelestialBody sun, PoseStack poseStack, MultiBufferSource buffer) {
        // Early return for very small suns to avoid unnecessary processing
        float baseSize = calculateRenderSize(sun, 100.0); // Assume 100 unit distance for size check
        if (baseSize < 0.1f) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Set sun brightness and light color uniforms if shader supports them
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            float[] lightColor = hexToRgbFloat(sun.getLightColor());
            shader.safeGetUniform(LIGHT_COLOR_UNIFORM).set(lightColor[0], lightColor[1], lightColor[2]);
        }

        // Bind cubemap texture for the core layer
        bindCubemapTexture(sun.getTexture());

        // Define sun layers: size multiplier, opacity
        float[][] sunLayers = {
            {1.0f, 1.0f},  // Core layer - full size, full opacity
            {1.2f, 0.7f},  // Inner halo - 20% larger, 70% opacity
            {1.5f, 0.4f},  // Middle halo - 50% larger, 40% opacity
            {2.0f, 0.2f}   // Outer halo - 100% larger, 20% opacity
        };

        // Render each layer (skip very low opacity layers for performance)
        for (int i = 0; i < sunLayers.length; i++) {
            float sizeMultiplier = sunLayers[i][0];
            float opacity = sunLayers[i][1];

            // Skip layers that would be too faint to see (opacity < 0.05)
            if (opacity < 0.05f) {
                continue;
            }

            // Set brightness based on layer opacity for sun effect
            float brightness = opacity;

            if (shader != null) {
                shader.safeGetUniform(SUN_BRIGHTNESS_UNIFORM).set(brightness);
            }

            // Push pose for this layer's transformations
            poseStack.pushPose();

            // Scale the layer based on its size multiplier
            poseStack.scale(sizeMultiplier, sizeMultiplier, sizeMultiplier);

            // Render this layer with appropriate opacity
            renderSunLayer(poseStack, buffer, opacity);

            // Pop pose to restore transformations for next layer
            poseStack.popPose();
        }

        // Reset shader
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    /**
     * Renders a single sun layer with specified opacity.
     */
    private static void renderSunLayer(PoseStack poseStack, MultiBufferSource buffer, float opacity) {
        // Set up alpha blending for translucent layers
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Set overall opacity for this layer
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        // Render the sphere for this layer
        renderSphere(poseStack, buffer, true);

        // Restore default shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.disableBlend();
    }

    /**
     * Renders a planet celestial body with surface textures.
     */
    private static void renderPlanet(CelestialBody planet, PoseStack poseStack, MultiBufferSource buffer) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // Bind cubemap texture
        bindCubemapTexture(planet.getTexture());

        // Render planet sphere
        renderSphere(poseStack, buffer, false);
    }

    /**
     * Binds a cubemap texture for rendering.
     */
    private static void bindCubemapTexture(ResourceLocation textureLocation) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        RenderSystem.setShaderTexture(0, textureLocation);
    }

    /**
     * Renders a sphere using cubemap UV mapping.
     */
    private static void renderSphere(PoseStack poseStack, MultiBufferSource buffer, boolean isSun) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        Matrix4f matrix = poseStack.last().pose();

        // Start drawing
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // Render sphere as subdivided cube for cubemap support
        renderCubemapSphere(bufferBuilder, matrix, isSun);

        // Finish drawing
        tesselator.end();
    }

    /**
     * Renders a sphere using cubemap UV coordinates.
     * This creates a cube-like sphere that properly maps cubemap textures.
     */
    private static void renderCubemapSphere(BufferBuilder bufferBuilder, Matrix4f matrix, boolean isSun) {
        float size = 1.0f; // Normalized size, actual scaling handled by pose stack

        // Define cube vertices for cubemap rendering
        Vector3f[] vertices = {
            new Vector3f(-size, -size, -size), // 0
            new Vector3f(size, -size, -size),  // 1
            new Vector3f(size, size, -size),   // 2
            new Vector3f(-size, size, -size),  // 3
            new Vector3f(-size, -size, size),  // 4
            new Vector3f(size, -size, size),   // 5
            new Vector3f(size, size, size),    // 6
            new Vector3f(-size, size, size)    // 7
        };

        // Define cubemap UV coordinates for each face
        float[][][] faceUVs = {
            // Front face (positive Z)
            {{0.25f, 0.333f}, {0.5f, 0.333f}, {0.5f, 0.666f}, {0.25f, 0.666f}},
            // Back face (negative Z)
            {{0.75f, 0.333f}, {1.0f, 0.333f}, {1.0f, 0.666f}, {0.75f, 0.666f}},
            // Right face (positive X)
            {{0.5f, 0.333f}, {0.75f, 0.333f}, {0.75f, 0.666f}, {0.5f, 0.666f}},
            // Left face (negative X)
            {{0.0f, 0.333f}, {0.25f, 0.333f}, {0.25f, 0.666f}, {0.0f, 0.666f}},
            // Top face (positive Y)
            {{0.25f, 0.0f}, {0.5f, 0.0f}, {0.5f, 0.333f}, {0.25f, 0.333f}},
            // Bottom face (negative Y)
            {{0.25f, 0.666f}, {0.5f, 0.666f}, {0.5f, 1.0f}, {0.25f, 1.0f}}
        };

        // Render each face of the cube
        for (int face = 0; face < 6; face++) {
            renderCubeFace(bufferBuilder, matrix, vertices, face, faceUVs[face], isSun);
        }
    }

    /**
     * Renders a single face of the cube with proper cubemap UV mapping.
     */
    private static void renderCubeFace(BufferBuilder bufferBuilder, Matrix4f matrix,
                                     Vector3f[] vertices, int face, float[][] uvs, boolean isSun) {
        int[] indices = getFaceIndices(face);

        for (int i = 0; i < 4; i++) {
            Vector3f vertex = vertices[indices[i]];
            float[] uv = uvs[i];

            // Add brightness for sun rendering
            float brightness = isSun ? 1.0f : 0.8f;

            bufferBuilder.vertex(matrix, vertex.x, vertex.y, vertex.z)
                        .uv(uv[0], uv[1])
                        .color(brightness, brightness, brightness, 1.0f)
                        .endVertex();
        }
    }

    /**
     * Gets vertex indices for a specific cube face.
     */
    private static int[] getFaceIndices(int face) {
        return switch (face) {
            case 0 -> new int[]{1, 0, 3, 2}; // Front
            case 1 -> new int[]{5, 1, 2, 6}; // Back
            case 2 -> new int[]{5, 4, 7, 6}; // Right
            case 3 -> new int[]{4, 0, 1, 5}; // Left
            case 4 -> new int[]{4, 5, 6, 7}; // Top
            case 5 -> new int[]{0, 4, 7, 3}; // Bottom
            default -> new int[]{0, 1, 2, 3};
        };
    }

    /**
     * Converts hex color to RGB float array.
     */
    private static float[] hexToRgbFloat(int hexColor) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    /**
     * Updates shader uniforms for sun rendering.
     */
    public static void updateSunUniforms(ShaderInstance shader, CelestialBody sun) {
        if (shader != null && sun != null && sun.isSun()) {
            float brightness = 1.0f;
            float[] lightColor = hexToRgbFloat(sun.getLightColor());

            shader.safeGetUniform(SUN_BRIGHTNESS_UNIFORM).set(brightness);
            shader.safeGetUniform(LIGHT_COLOR_UNIFORM).set(lightColor[0], lightColor[1], lightColor[2]);
        }
    }
}