package team.nextlevelmodding.nlc.lib.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Vector3f;
import team.nextlevelmodding.nlc.lib.rendering.objects.shaderparticleobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages shader-based particles in the world.
 * Handles spawning, updating, and rendering of shader particles.
 */
public class ShaderParticleSystem {

    private final List<shaderparticleobject> particles = new ArrayList<>();
    private final ClientLevel level;

    public ShaderParticleSystem(ClientLevel level) {
        this.level = level;
    }

    /**
     * Spawn a new shader particle with default parameters.
     */
    public void spawnParticle(String shaderId, double x, double y, double z) {
        spawnParticle(shaderId, x, y, z, 0, 0, 0, 1.0f, 20);
    }

    /**
     * Spawn a new shader particle with custom parameters.
     */
    public void spawnParticle(String shaderId, double x, double y, double z, double vx, double vy, double vz, float size, int lifetime) {
        shaderparticleobject particle = new shaderparticleobject(shaderId, x, y, z);
        particle.setVelocity(new Vector3f((float) vx, (float) vy, (float) vz));
        particle.setSize(size);
        particle.setLifetime(lifetime);
        particles.add(particle);
    }

    /**
     * Update all particles.
     */
    public void tick() {
        particles.removeIf(shaderparticleobject::isExpired);
        particles.forEach(shaderparticleobject::tick);
    }

    /**
     * Render all particles.
     * This would need to be called from a render event or similar.
     */
    public void render() {
        // TODO: Implement rendering with shaders
        // Bind shader based on shaderId, render particles
    }
}