package team.nextlevelmodding.nlc.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import team.nextlevelmodding.nlc.lib.rendering.GUIParticleSystem;
import team.nextlevelmodding.nlc.lib.rendering.ShaderParticleSystem;
import team.nextlevelmodding.nlc.lib.rendering.ShaderGUIParticleSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * NLCParticles API - Vanilla, Shader, and GUI particles support.
 */
public class nlcparticles {

    private static final Map<Screen, GUIParticleSystem> GUI_PARTICLE_SYSTEMS = new HashMap<>();
    private static final Map<net.minecraft.client.multiplayer.ClientLevel, ShaderParticleSystem> SHADER_PARTICLE_SYSTEMS = new HashMap<>();
    private static final Map<Screen, ShaderGUIParticleSystem> SHADER_GUI_PARTICLE_SYSTEMS = new HashMap<>();

    // -------------------------
    // Vanilla particle methods
    // -------------------------

    public static <T extends ParticleOptions> void spawn(ServerLevel level, T particle, double x, double y, double z) {
        spawn(level, particle, x, y, z, 64);
    }

    public static <T extends ParticleOptions> void spawn(ServerLevel level, T particle, double x, double y, double z, double maxDistance) {
        // TODO: Implement packet sending for server-side particle spawning
        // for (ServerPlayer player : level.players()) {
        //     if (player.distanceToSqr(x, y, z) <= maxDistance * maxDistance) {
        //         PacketHandler.sendParticleToPlayer(player, particle, x, y, z);
        //     }
        // }
    }

    public static <T extends ParticleOptions> void spawnClient(T particle, double x, double y, double z) {
        Minecraft.getInstance().level.addParticle(particle, x, y, z, 0, 0, 0);
    }

    // -------------------------
    // Shader / VFX particle stub
    // -------------------------

    /**
     * Spawn a shader or VFX particle in the world.
     * Handles both server->client and client-only cases internally.
     *
     * @param shaderId Identifier for the shader/VFX particle
     * @param level    Server level for server->client spawning
     * @param x        X position
     * @param y        Y position
     * @param z        Z position
     * @param maxDistance Optional max distance for server->client (ignored if level is null)
     */
    public static void spawnShader(String shaderId, ServerLevel level, double x, double y, double z, Double maxDistance) {
        spawnShader(shaderId, level, x, y, z, 0, 0, 0, 1.0f, 20, maxDistance);
    }

    /**
     * Spawn a shader particle with custom parameters.
     */
    public static void spawnShader(String shaderId, ServerLevel level, double x, double y, double z, double vx, double vy, double vz, float size, int lifetime, Double maxDistance) {
        if (level != null) {
            // Server-side spawning: send to clients
            // TODO: Implement packet sending for shader particles
        } else {
            // Client-only
            net.minecraft.client.multiplayer.ClientLevel clientLevel = Minecraft.getInstance().level;
            if (clientLevel != null) {
                ShaderParticleSystem system = SHADER_PARTICLE_SYSTEMS.computeIfAbsent(clientLevel, ShaderParticleSystem::new);
                system.spawnParticle(shaderId, x, y, z, vx, vy, vz, size, lifetime);
            }
        }
    }

    // -------------------------
    // GUI particle methods
    // -------------------------

    public static void spawnGui(Screen screen, String particleId, double x, double y) {
        spawnGui(screen, particleId, x, y, 0, 0, 1.0f, 40, 1.0f);
    }

    public static void spawnGui(Screen screen, String particleId, double x, double y, double vx, double vy, float size, int lifetime, float alpha) {
        GUIParticleSystem system = GUI_PARTICLE_SYSTEMS.computeIfAbsent(screen, GUIParticleSystem::new);
        system.spawnParticle(particleId, x, y, vx, vy, size, lifetime, alpha);
    }

    public static void spawnGuiShader(Screen screen, String shaderId, double x, double y) {
        spawnGuiShader(screen, shaderId, x, y, 0, 0, 1.0f, 40, 1.0f);
    }

    public static void spawnGuiShader(Screen screen, String shaderId, double x, double y, double vx, double vy, float size, int lifetime, float alpha) {
        ShaderGUIParticleSystem system = SHADER_GUI_PARTICLE_SYSTEMS.computeIfAbsent(screen, ShaderGUIParticleSystem::new);
        system.spawnParticle(shaderId, x, y, vx, vy, size, lifetime, alpha);
    }

    /**
     * Get the GUI particle system for a screen.
     */
    public static GUIParticleSystem getGuiParticleSystem(Screen screen) {
        return GUI_PARTICLE_SYSTEMS.get(screen);
    }

    /**
     * Tick all GUI particle systems.
     */
    public static void tickGuiParticles() {
        GUI_PARTICLE_SYSTEMS.values().forEach(GUIParticleSystem::tick);
    }

    /**
     * Render GUI particles for a screen.
     */
    public static void renderGuiParticles(Screen screen, PoseStack poseStack, float partialTicks) {
        GUIParticleSystem system = GUI_PARTICLE_SYSTEMS.get(screen);
        if (system != null) {
            system.render(poseStack, partialTicks);
        }
    }

    /**
     * Get the shader particle system for a level.
     */
    public static ShaderParticleSystem getShaderParticleSystem(net.minecraft.client.multiplayer.ClientLevel level) {
        return SHADER_PARTICLE_SYSTEMS.get(level);
    }

    /**
     * Tick all shader particle systems.
     */
    public static void tickShaderParticles() {
        SHADER_PARTICLE_SYSTEMS.values().forEach(ShaderParticleSystem::tick);
    }

    /**
     * Render shader particles for a level.
     */
    public static void renderShaderParticles(net.minecraft.client.multiplayer.ClientLevel level) {
        ShaderParticleSystem system = SHADER_PARTICLE_SYSTEMS.get(level);
        if (system != null) {
            system.render();
        }
    }

    /**
     * Get the shader GUI particle system for a screen.
     */
    public static ShaderGUIParticleSystem getShaderGuiParticleSystem(Screen screen) {
        return SHADER_GUI_PARTICLE_SYSTEMS.get(screen);
    }

    /**
     * Tick all shader GUI particle systems.
     */
    public static void tickShaderGuiParticles() {
        SHADER_GUI_PARTICLE_SYSTEMS.values().forEach(ShaderGUIParticleSystem::tick);
    }

    /**
     * Render shader GUI particles for a screen.
     */
    public static void renderShaderGuiParticles(Screen screen, PoseStack poseStack, float partialTicks) {
        ShaderGUIParticleSystem system = SHADER_GUI_PARTICLE_SYSTEMS.get(screen);
        if (system != null) {
            system.render(poseStack, partialTicks);
        }
    }
}
