package team.nextlevelmodding.ar2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

// Use Forge event bus

public class EventUtils {
    
    /**
     * Posts a MasterCallEvent to the Forge event bus.
     * @param targetPos The target block position for the event
     * @param data The data to pass with the event (should be an instance of a known data class like TestData or ThrustData)
     * @param level The world/level where the event is occurring
     */
    public static void postMasterCallEvent(BlockPos targetPos, Object data, Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null when posting MasterCallEvent");
        }
        
        MasterCallEvent event = new MasterCallEvent(targetPos, data, level);
        boolean cancelled = MinecraftForge.EVENT_BUS.post(event);

        if (cancelled) {
            // Handle event cancellation if needed
            ar2.LOGGER.debug("MasterCallEvent was cancelled for position: {}", targetPos);
        }
    }
}
