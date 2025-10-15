package mods.hexagonal.interstellar.client.celestial;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;

/**
 * Custom render types for celestial bodies.
 * Provides proper render type registration for planets and suns.
 */
public class CelestialRenderTypes extends RenderType {

    // Planet render type for celestial bodies
    private static final RenderType PLANET_RENDER_TYPE = createPlanetRenderType();

    // Sun render type for celestial bodies
    private static final RenderType SUN_RENDER_TYPE = createSunRenderType();

    /**
     * Private constructor to prevent instantiation.
     */
    private CelestialRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    /**
     * Gets the planet render type for celestial body rendering.
     * This render type is optimized for planet rendering with proper lighting and blending.
     *
     * @return The planet render type
     */
    public static RenderType getPlanetRenderType() {
        return PLANET_RENDER_TYPE;
    }

    /**
     * Gets the sun render type for celestial body rendering.
     * This render type is optimized for sun rendering with proper lighting and blending.
     *
     * @return The sun render type
     */
    public static RenderType getSunRenderType() {
        return SUN_RENDER_TYPE;
    }

    /**
      * Creates the planet render type with proper configuration.
      */
     private static RenderType createPlanetRenderType() {
         RenderType.CompositeState renderState = RenderType.CompositeState.builder()
             .setShaderState(RENDERTYPE_SOLID_SHADER)
             .setTextureState(BLOCK_SHEET_MIPPED)  // Enable texture support for planet textures
             .setTransparencyState(NO_TRANSPARENCY)
             .setDepthTestState(LEQUAL_DEPTH_TEST)
             .setCullState(NO_CULL)
             .setLightmapState(NO_LIGHTMAP)  // Disable lightmap since we're handling lighting manually
             .setOverlayState(NO_OVERLAY)
             .setLayeringState(VIEW_OFFSET_Z_LAYERING)
             .setOutputState(MAIN_TARGET)
             .setTexturingState(DEFAULT_TEXTURING)
             .setWriteMaskState(COLOR_DEPTH_WRITE)
             .setLineState(DEFAULT_LINE)
             .createCompositeState(false);

         return RenderType.create(
             "interstellar_planet",
             DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,  // Use POSITION_TEX_COLOR_NORMAL to support UV texture coordinates and normals
             VertexFormat.Mode.QUADS,
             256,
             false,
             false,
             renderState
         );
     }

    /**
     * Creates the sun render type with proper configuration.
     */
    private static RenderType createSunRenderType() {
        RenderType.CompositeState renderState = RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_SOLID_SHADER)
            .setTextureState(BLOCK_SHEET_MIPPED)  // Enable texture support for sun textures
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setLightmapState(LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setLineState(DEFAULT_LINE)
            .createCompositeState(false);

        return RenderType.create(
            "interstellar_sun",
            DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,  // Use texture format for sun textures with normal support
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            renderState
        );
    }
}