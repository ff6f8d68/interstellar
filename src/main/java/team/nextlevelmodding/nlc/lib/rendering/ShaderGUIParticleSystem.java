package team.nextlevelmodding.nlc.lib.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2f;
import team.nextlevelmodding.nlc.lib.rendering.objects.shaderguiparticleobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages shader-based GUI particles.
 * Handles spawning, updating, and rendering of shader GUI particles.
 */
public class ShaderGUIParticleSystem {

    private final List<shaderguiparticleobject> particles = new ArrayList<>();
    private final Screen screen;

    public ShaderGUIParticleSystem(Screen screen) {
        this.screen = screen;
    }

    /**
     * Spawn a new shader GUI particle with default parameters.
     */
    public void spawnParticle(String shaderId, double x, double y) {
        spawnParticle(shaderId, x, y, 0, 0, 1.0f, 40, 1.0f);
    }

    /**
     * Spawn a new shader GUI particle with custom parameters.
     */
    public void spawnParticle(String shaderId, double x, double y, double vx, double vy, float size, int lifetime, float alpha) {
        shaderguiparticleobject particle = new shaderguiparticleobject(shaderId, x, y);
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
        particles.removeIf(shaderguiparticleobject::isExpired);
        particles.forEach(shaderguiparticleobject::tick);
    }

    /**
     * Render all particles.
     */
    public void render(PoseStack poseStack, float partialTicks) {
        // TODO: Implement rendering with shaders
        // Bind shader, render particles
    }
}