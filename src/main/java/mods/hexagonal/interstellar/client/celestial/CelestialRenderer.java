package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import mods.hexagonal.interstellar.celestial.CelestialBody;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;
// Note: Lodestone OBJ model classes not available in current version

import java.util.List;

/**
 * Handles rendering of celestial bodies using the correct VertexConsumer approach.
 * Based on the Genesis PlanetRenderer for proper rendering pipeline integration.
 */
public class CelestialRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();

    // OBJ model registry for 3D model rendering
    private static final OBJModelRegistry objModelRegistry = new OBJModelRegistry();

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
     * Checks if OBJ model is available for rendering.
     */
    private static boolean isOBJModelAvailable() {
        return OBJModelRegistry.isPlanetModelRegistered();
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
        // Check if OBJ model is available for 3D rendering
        if (isOBJModelAvailable()) {
            renderPlanetsWithOBJ(event, celestialBodies);
        } else {
            renderPlanetsWithSprites(event, celestialBodies);
        }
    }

    /**
      * Renders all sun-type celestial bodies.
      */
     private static void renderSuns(RenderLevelStageEvent event, List<CelestialBody> celestialBodies) {
         MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        for (CelestialBody celestialBody : celestialBodies) {
            if (celestialBody.isSun()) {
                // Bind the sun's texture before rendering
                RenderSystem.setShaderTexture(0, celestialBody.getTexture());

                // Create a render type with the sun's texture
                RenderType sunRenderType = createSunRenderTypeWithTexture(celestialBody.getTexture());
                VertexConsumer sunBuffer = bufferSource.getBuffer(sunRenderType);
                renderSun(event, celestialBody, sunBuffer);
            }
        }

        bufferSource.endBatch();
    }

    /**
     * Renders planets using OBJ models with size-based scaling.
     */
    private static void renderPlanetsWithOBJ(RenderLevelStageEvent event, List<CelestialBody> celestialBodies) {
        for (CelestialBody celestialBody : celestialBodies) {
            if (!celestialBody.isSun()) {
                renderPlanetWithOBJ(event, celestialBody);
            }
        }
    }

    /**
     * Renders planets using sprite-based approach (fallback method).
     */
    private static void renderPlanetsWithSprites(RenderLevelStageEvent event, List<CelestialBody> celestialBodies) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        for (CelestialBody celestialBody : celestialBodies) {
            if (!celestialBody.isSun()) {
                // Bind the planet's texture before rendering
                RenderSystem.setShaderTexture(0, celestialBody.getTexture());

                // Create a render type with the specific planet's texture
                RenderType planetRenderType = createPlanetRenderTypeWithTexture(celestialBody.getTexture());
                VertexConsumer planetBuffer = bufferSource.getBuffer(planetRenderType);
                renderPlanet(event, celestialBody, planetBuffer);
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
     * Renders a single planet celestial body using OBJ model with size-based scaling.
     */
    private static void renderPlanetWithOBJ(RenderLevelStageEvent event, CelestialBody planet) {
        OBJModelRegistry.OBJModel model = OBJModelRegistry.getPlanetModel();
        if (model == null || model.faces.isEmpty()) {
            // Fallback to cube rendering if OBJ model is not available
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            RenderSystem.setShaderTexture(0, planet.getTexture());
            RenderType planetRenderType = createPlanetRenderTypeWithTexture(planet.getTexture());
            VertexConsumer planetBuffer = bufferSource.getBuffer(planetRenderType);
            renderPlanet(event, planet, planetBuffer);
            bufferSource.endBatch();
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        try {
            // Set up the transformation matrix
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

            // Apply scaling based on planet size
            float scale = (float) planet.getSize() * model.scale;
            matrix.scale(scale, scale, scale);

            // Apply basic rotation (can be enhanced with orbital mechanics)
            matrix.rotate(new Quaternionf().rotationXYZ(0, 0, 0));

            // Set texture
            RenderSystem.setShaderTexture(0, planet.getTexture());
            RenderType planetRenderType = createPlanetRenderTypeWithTexture(planet.getTexture());
            VertexConsumer buffer = bufferSource.getBuffer(planetRenderType);

            // Calculate lighting direction
            Vector3d lightDir = new Vector3d(-pos.x, -pos.y, -pos.z).normalize();

            // Render the OBJ model
            renderOBJModel(matrix, buffer, model, planet, lightDir);

        } finally {
            bufferSource.endBatch();
        }
    }

    /**
     * Renders an OBJ model with proper lighting and texturing.
     */
    private static void renderOBJModel(Matrix4f matrix, VertexConsumer buffer,
                                     OBJModelRegistry.OBJModel model, CelestialBody planet, Vector3d lightDir) {
        int[] rgb = hexToRgbInt(planet.getLightColor());
        int baseR = rgb[0], baseG = rgb[1], baseB = rgb[2];

        // Render each face of the model
        for (OBJModelRegistry.OBJFace face : model.faces) {
            if (face.vertexIndices.size() >= 3) {
                // For simplicity, render as triangle fan (most OBJ faces are triangles or quads)
                renderOBJFace(matrix, buffer, model, face, baseR, baseG, baseB, lightDir);
            }
        }
    }

    /**
     * Renders a single OBJ face as a triangle fan.
     */
    private static void renderOBJFace(Matrix4f matrix, VertexConsumer buffer,
                                    OBJModelRegistry.OBJModel model, OBJModelRegistry.OBJFace face,
                                    int baseR, int baseG, int baseB, Vector3d lightDir) {
        if (face.vertexIndices.size() < 3) return;

        // Get the first three vertices to form the first triangle
        int vertexIndex1 = face.vertexIndices.get(0);
        int vertexIndex2 = face.vertexIndices.get(1);
        int vertexIndex3 = face.vertexIndices.get(2);

        if (vertexIndex1 >= model.vertices.size() || vertexIndex2 >= model.vertices.size() || vertexIndex3 >= model.vertices.size()) {
            return; // Invalid vertex indices
        }

        Vector3f vertex1 = model.vertices.get(vertexIndex1);
        Vector3f vertex2 = model.vertices.get(vertexIndex2);
        Vector3f vertex3 = model.vertices.get(vertexIndex3);

        // Calculate face normal for lighting
        Vector3f edge1 = new Vector3f(vertex2).sub(vertex1);
        Vector3f edge2 = new Vector3f(vertex3).sub(vertex1);
        Vector3f normal = new Vector3f(edge1).cross(edge2).normalize();

        float lighting = Math.max(0.0f, normal.dot((float)lightDir.x, (float)lightDir.y, (float)lightDir.z));

        // Apply lighting to colors
        int r1 = (int) (baseR * lighting), g1 = (int) (baseG * lighting), b1 = (int) (baseB * lighting);
        int r2 = (int) (baseR * lighting), g2 = (int) (baseG * lighting), b2 = (int) (baseB * lighting);
        int r3 = (int) (baseR * lighting), g3 = (int) (baseG * lighting), b3 = (int) (baseB * lighting);

        // Get texture coordinates if available
        float u1 = 0.0f, v1 = 0.0f, u2 = 1.0f, v2 = 0.0f, u3 = 0.5f, v3 = 1.0f;
        if (face.texCoordIndices.size() >= 3) {
            int texIndex1 = face.texCoordIndices.get(0);
            int texIndex2 = face.texCoordIndices.get(1);
            int texIndex3 = face.texCoordIndices.get(2);
            if (texIndex1 < model.texCoords.size() && texIndex2 < model.texCoords.size() && texIndex3 < model.texCoords.size()) {
                Vector3f tex1 = model.texCoords.get(texIndex1);
                Vector3f tex2 = model.texCoords.get(texIndex2);
                Vector3f tex3 = model.texCoords.get(texIndex3);
                u1 = tex1.x; v1 = tex1.y;
                u2 = tex2.x; v2 = tex2.y;
                u3 = tex3.x; v3 = tex3.y;
            }
        }

        // Render first triangle
        buffer.vertex(matrix, vertex1.x, vertex1.y, vertex1.z)
              .uv(u1, v1)
              .color(r1, g1, b1, 255)
              .normal(normal.x, normal.y, normal.z)
              .endVertex();
        buffer.vertex(matrix, vertex2.x, vertex2.y, vertex2.z)
              .uv(u2, v2)
              .color(r2, g2, b2, 255)
              .normal(normal.x, normal.y, normal.z)
              .endVertex();
        buffer.vertex(matrix, vertex3.x, vertex3.y, vertex3.z)
              .uv(u3, v3)
              .color(r3, g3, b3, 255)
              .normal(normal.x, normal.y, normal.z)
              .endVertex();

        // Render additional triangles for faces with more than 3 vertices
        for (int i = 3; i < face.vertexIndices.size(); i++) {
            int vertexIndexI = face.vertexIndices.get(i);
            if (vertexIndexI >= model.vertices.size()) continue;

            Vector3f vertexI = model.vertices.get(vertexIndexI);

            // Use texture coordinates if available
            float ui = 0.5f, vi_coord = 1.0f;
            if (i < face.texCoordIndices.size()) {
                int texIndexI = face.texCoordIndices.get(i);
                if (texIndexI < model.texCoords.size()) {
                    Vector3f texI = model.texCoords.get(texIndexI);
                    ui = texI.x;
                    vi_coord = texI.y;
                }
            }

            int ri = (int) (baseR * lighting), gi = (int) (baseG * lighting), bi = (int) (baseB * lighting);

            // Render triangle using first vertex and current vertex
            buffer.vertex(matrix, vertex1.x, vertex1.y, vertex1.z)
                  .uv(u1, v1)
                  .color(r1, g1, b1, 255)
                  .normal(normal.x, normal.y, normal.z)
                  .endVertex();
            buffer.vertex(matrix, vertex3.x, vertex3.y, vertex3.z)
                  .uv(u3, v3)
                  .color(r3, g3, b3, 255)
                  .normal(normal.x, normal.y, normal.z)
                  .endVertex();
            buffer.vertex(matrix, vertexI.x, vertexI.y, vertexI.z)
                  .uv(ui, vi_coord)
                  .color(ri, gi, bi, 255)
                  .normal(normal.x, normal.y, normal.z)
                  .endVertex();

            // Update vertex3 for next iteration
            vertex3 = vertexI;
            u3 = ui;
            v3 = vi_coord;
            r3 = ri;
            g3 = gi;
            b3 = bi;
        }
    }

    /**
      * Renders a single sun celestial body using VertexConsumer approach.
      */
    private static void renderSun(RenderLevelStageEvent event, CelestialBody sun, VertexConsumer buffer) {
        SunRenderer.renderSun(event, sun, buffer);
    }

    /**
     * Renders a planet as an optimized sphere-like shape with proper lighting.
     */
    private static void renderPlanetCube(Matrix4f matrix, VertexConsumer buffer, float halfSize, CelestialBody planet) {
        Vec3 planetPosVec3 = planet.getCurrentPosition();
        Vector3d planetPos = new Vector3d(planetPosVec3.x, planetPosVec3.y, planetPosVec3.z);
        Vector3d lightDir = new Vector3d(-planetPos.x, -planetPos.y, -planetPos.z).normalize();

        // Render optimized sphere-like shape using fewer faces for better performance
        renderOptimizedPlanet(matrix, buffer, halfSize, planet, lightDir);
    }

    /**
     * Renders a planet using a simple cube approach for stability.
     */
    private static void renderOptimizedPlanet(Matrix4f matrix, VertexConsumer buffer, float halfSize, CelestialBody planet, Vector3d lightDir) {
        int[] rgb = hexToRgbInt(planet.getLightColor());
        int r = rgb[0], g = rgb[1], b = rgb[2];

        // Render simple cube faces for stability - 6 faces, 4 vertices each
        // Front face
        addSimpleQuad(matrix, buffer, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, r, g, b, lightDir);
        // Back face
        addSimpleQuad(matrix, buffer, halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, r, g, b, lightDir);
        // Left face
        addSimpleQuad(matrix, buffer, -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, r, g, b, lightDir);
        // Right face
        addSimpleQuad(matrix, buffer, halfSize, -halfSize, halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, r, g, b, lightDir);
        // Top face
        addSimpleQuad(matrix, buffer, -halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, -halfSize, halfSize, -halfSize, r, g, b, lightDir);
        // Bottom face
        addSimpleQuad(matrix, buffer, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, r, g, b, lightDir);
    }

    /**
     * Adds a simple quad with proper lighting and UV mapping.
     */
    private static void addSimpleQuad(Matrix4f matrix, VertexConsumer buffer,
                                     float x1, float y1, float z1, float x2, float y2, float z2,
                                     float x3, float y3, float z3, float x4, float y4, float z4,
                                     int r, int g, int b, Vector3d lightDir) {

        // Calculate face normal for lighting
        Vector3d v1v2 = new Vector3d(x2 - x1, y2 - y1, z2 - z1);
        Vector3d v1v4 = new Vector3d(x4 - x1, y4 - y1, z4 - z1);
        Vector3d normal = new Vector3d(v1v2.y * v1v4.z - v1v2.z * v1v4.y,
                                     v1v2.z * v1v4.x - v1v2.x * v1v4.z,
                                     v1v2.x * v1v4.y - v1v2.y * v1v4.x).normalize();

        float lighting = (float) Math.max(0.0, normal.dot(lightDir));

        // Apply lighting to colors
        int litR1 = (int) (r * lighting), litG1 = (int) (g * lighting), litB1 = (int) (b * lighting);
        int litR2 = (int) (r * lighting), litG2 = (int) (g * lighting), litB2 = (int) (b * lighting);
        int litR3 = (int) (r * lighting), litG3 = (int) (g * lighting), litB3 = (int) (b * lighting);
        int litR4 = (int) (r * lighting), litG4 = (int) (g * lighting), litB4 = (int) (b * lighting);

        // Add vertices with proper lighting and UV coordinates (POSITION_TEX_COLOR_NORMAL format: position, uv, color, normal)
        buffer.vertex(matrix, x1, y1, z1)
              .uv(0.0f, 0.0f)  // UV coordinates for texture mapping
              .color(litR1, litG1, litB1, 255)
              .normal((float)normal.x, (float)normal.y, (float)normal.z)
              .endVertex();
        buffer.vertex(matrix, x2, y2, z2)
              .uv(1.0f, 0.0f)  // UV coordinates for texture mapping
              .color(litR2, litG2, litB2, 255)
              .normal((float)normal.x, (float)normal.y, (float)normal.z)
              .endVertex();
        buffer.vertex(matrix, x3, y3, z3)
              .uv(1.0f, 1.0f)  // UV coordinates for texture mapping
              .color(litR3, litG3, litB3, 255)
              .normal((float)normal.x, (float)normal.y, (float)normal.z)
              .endVertex();
        buffer.vertex(matrix, x4, y4, z4)
              .uv(0.0f, 1.0f)  // UV coordinates for texture mapping
              .color(litR4, litG4, litB4, 255)
              .normal((float)normal.x, (float)normal.y, (float)normal.z)
              .endVertex();
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


        // Fill all required elements for POSITION_COLOR_NORMAL vertex format (position, color, normal)
        buffer.vertex(matrix, x, y, z)
              .color(litR, litG, litB, 255)
              .normal(vertexNormal.x, vertexNormal.y, vertexNormal.z)  // Normal vector
              .endVertex();
    }

    /**
     * Creates a render type with a specific texture for a planet celestial body.
     */
    private static RenderType createPlanetRenderTypeWithTexture(ResourceLocation textureLocation) {
        // Use the custom planet render type designed for celestial bodies
        return CelestialRenderTypes.getPlanetRenderType();
    }

    /**
     * Creates a render type with a specific texture for a sun celestial body.
     */
    private static RenderType createSunRenderTypeWithTexture(ResourceLocation textureLocation) {
        // Use the custom sun render type designed for celestial bodies
        return CelestialRenderTypes.getSunRenderType();
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