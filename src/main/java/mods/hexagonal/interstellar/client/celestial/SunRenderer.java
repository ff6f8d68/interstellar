package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Handles rendering of sun celestial bodies with proper lighting and shader support.
 * Based on the Genesis PlanetRenderer approach for consistent rendering.
 */
public class SunRenderer {

    private static final float SUN_SIZE = 720.0f;

    /**
     * Renders the sun using proper vertex consumer and shader uniforms.
     */
    public static void renderSun(RenderLevelStageEvent event, VertexConsumer buffer) {
        PoseStack poseStack = event.getPoseStack();

        // Set camera position uniform for shaders
        setupSunShaderUniforms(event);

        Matrix4f matrix;
        try {
            matrix = (Matrix4f) poseStack.last().pose().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        // Position relative to camera
        matrix.translate((float) -event.getCamera().getPosition().x,
                (float) -event.getCamera().getPosition().y,
                (float) -event.getCamera().getPosition().z);

        float halfSize = SUN_SIZE;

        // Render cube faces for sun
        addCubeFaceSun(matrix, buffer, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize);
        addCubeFaceSun(matrix, buffer, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize);
        addCubeFaceSun(matrix, buffer, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize);
        addCubeFaceSun(matrix, buffer, halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize);
        addCubeFaceSun(matrix, buffer, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize);
        addCubeFaceSun(matrix, buffer, -halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize);
    }

    /**
     * Sets up shader uniforms for sun rendering.
     */
    private static void setupSunShaderUniforms(RenderLevelStageEvent event) {
        Vector3f cameraPos = new Vector3f(
            (float) event.getCamera().getPosition().x,
            (float) event.getCamera().getPosition().y,
            (float) event.getCamera().getPosition().z
        );

        // Set camera position uniform if shader supports it
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            try {
                Uniform uniform = shader.getUniform("CameraPosition");
                if (uniform != null) {
                    uniform.set(cameraPos.x, cameraPos.y, cameraPos.z);
                } else {
                    // Log that the uniform is not available for debugging purposes
                    // This is normal if using default shaders that don't have this uniform
                }
            } catch (Exception e) {
                // Log shader uniform errors for debugging
                // This can happen if the shader doesn't support the uniform
            }
        }
    }

    /**
     * Adds a cube face for sun rendering with proper color calculations.
     */
    private static void addCubeFaceSun(Matrix4f matrix, VertexConsumer buffer,
                                     float x1, float y1, float z1, float x2, float y2, float z2,
                                     float x3, float y3, float z3, float x4, float y4, float z4) {
        // Calculate colors based on vertex positions for sun-like appearance
        buffer.vertex(matrix, x1, y1, z1)
              .color((int)(255 * (x1 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (y1 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (z1 + SUN_SIZE) / (SUN_SIZE * 2)), 255)
              .endVertex();

        buffer.vertex(matrix, x2, y2, z2)
              .color((int)(255 * (x2 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (y2 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (z2 + SUN_SIZE) / (SUN_SIZE * 2)), 255)
              .endVertex();

        buffer.vertex(matrix, x3, y3, z3)
              .color((int)(255 * (x3 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (y3 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (z3 + SUN_SIZE) / (SUN_SIZE * 2)), 255)
              .endVertex();

        buffer.vertex(matrix, x4, y4, z4)
              .color((int)(255 * (x4 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (y4 + SUN_SIZE) / (SUN_SIZE * 2)),
                     (int)(255 * (z4 + SUN_SIZE) / (SUN_SIZE * 2)), 255)
              .endVertex();
    }
}