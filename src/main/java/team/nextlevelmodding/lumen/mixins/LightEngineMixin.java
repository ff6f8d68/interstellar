package team.nextlevelmodding.lumen.mixins;

import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LightEngine.class)
public class LightEngineMixin {

    /**
     * Replace every use of the MAX_LEVEL constant (15) in LightEngine
     * with Integer.MAX_VALUE, effectively removing the upper cap for light propagation.
     */
    @ModifyConstant(
            method = "*",
            constant = @Constant(intValue = 15)
    )
    private int removeLightCap(int original) {
        return Integer.MAX_VALUE; // true no max for internal light calculations
    }
}
