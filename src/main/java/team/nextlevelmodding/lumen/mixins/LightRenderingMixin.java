package team.nextlevelmodding.lumen.mixins;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class LightRenderingMixin {
    // Remove clampColor clamping
    @Redirect(
            method = "clampColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
            )
    )
    private static float removeClamp(float value, float min, float max) {
        return value; // allow >1.0 RGB
    }

    // Optional: remove normalization in getBrightness
    @ModifyConstant(
            method = "getBrightness",
            constant = @Constant(floatValue = 15.0F)
    )
    private float removeDivision(float original) {
        return 1.0F; // prevent dividing by 15
    }
}
