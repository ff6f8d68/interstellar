package team.nextlevelmodding.nlc.lib;

import net.minecraft.client.particle.ParticleRenderType;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry/provider for custom particle render types.
 * Allows other classes to define and register new render types.
 */
public abstract class nlcrendertype {

    // Map of registered render types
    private static final Map<String, ParticleRenderType> RENDER_TYPES = new HashMap<>();

    /**
     * Register a new particle render type.
     *
     * @param name Unique name for the render type
     * @param type ParticleRenderType instance
     */
    protected static void registerRenderType(String name, ParticleRenderType type) {
        if (RENDER_TYPES.containsKey(name)) {
            throw new IllegalArgumentException("RenderType '" + name + "' is already registered!");
        }
        RENDER_TYPES.put(name, type);
    }

    /**
     * Get a registered particle render type by name.
     *
     * @param name Name of the render type
     * @return ParticleRenderType instance
     */
    public static ParticleRenderType getRenderType(String name) {
        return RENDER_TYPES.get(name);
    }

    /**
     * Check if a render type with the given name exists.
     *
     * @param name Name of the render type
     * @return true if registered, false otherwise
     */
    public static boolean hasRenderType(String name) {
        return RENDER_TYPES.containsKey(name);
    }

    /**
     * Get all registered render types (unmodifiable copy).
     */
    public static Map<String, ParticleRenderType> getAllRenderTypes() {
        return Map.copyOf(RENDER_TYPES);
    }
}
