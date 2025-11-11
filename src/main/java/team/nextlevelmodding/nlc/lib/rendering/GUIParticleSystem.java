package team.nextlevelmodding.nlc.lib.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import team.nextlevelmodding.nlc.lib.rendering.objects.guiparticleobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages GUI particles for a screen.
 * Handles spawning, updating, and rendering of GUI particles.
 */
public class GUIParticleSystem {

    private final List<guiparticleobject> particles = new ArrayList<>();
    private final Screen screen;

    public GUIParticleSystem(Screen screen) {
        this.screen = screen;
    }

    /**
     * Spawn a new GUI particle with default parameters.
     */
    public void spawnParticle(String particleId, double x, double y) {
        spawnParticle(particleId, x, y, 0, 0, 1.0f, 40, 1.0f);
    }

    /**
     * Spawn a new GUI particle with custom parameters.
     */
    public void spawnParticle(String particleId, double x, double y, double vx, double vy, float size, int lifetime, float alpha) {
        guiparticleobject particle = new guiparticleobject(particleId, x, y);
        particle.setVelocity(new Vector2f((float) vx, (float) vy));
        particle.setSize(size);
        particle.setLifetime(lifetime);
        particle.setAlpha(alpha);
        particles.add(particle);
    }

    /**
     * Update all particles.
     */
    public void tick() {
        particles.removeIf(guiparticleobject::isExpired);
        particles.forEach(guiparticleobject::tick);
    }

    /**
     * Render all particles.
     */
    public void render(PoseStack poseStack, float partialTicks) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, new ResourceLocation("nlc", "textures/particles/" + "default.png")); // Default texture, should be overridden

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (guiparticleobject particle : particles) {
            renderParticle(particle, bufferBuilder, partialTicks);
        }

        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    private void renderParticle(guiparticleobject particle, BufferBuilder bufferBuilder, float partialTicks) {
        Vector2f pos = particle.getPosition();
        float size = particle.getSize();
        float alpha = particle.getAlpha();

        // Bind texture for this particle
        // For simplicity, assuming texture is based on particleId
        // In real implementation, you'd have a texture atlas or registry
        ResourceLocation texture = new ResourceLocation("nlc", "textures/particles/" + particle.getParticleId() + ".png");
        RenderSystem.setShaderTexture(0, texture);

        // Simple quad rendering
        float x1 = pos.x - size / 2;
        float y1 = pos.y - size / 2;
        float x2 = pos.x + size / 2;
        float y2 = pos.y + size / 2;

        bufferBuilder.vertex(x1, y1, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 0).endVertex();
        bufferBuilder.vertex(x1, y2, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(0, 1).endVertex();
        bufferBuilder.vertex(x2, y2, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 1).endVertex();
        bufferBuilder.vertex(x2, y1, 0).color(1.0f, 1.0f, 1.0f, alpha).uv(1, 0).endVertex();
    }
}