package team.nextlevelmodding.ar2;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class RenderEvents {

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<LivingEntity, ?> event) {
        // Safe cast to Player
        if (!(event.getEntity() instanceof Player player)) return;

        // Optional: skip rendering for the local player
        if (player == Minecraft.getInstance().player) return;

        PoseStack matrix = event.getPoseStack();

        // Get the player's name
        String name = player.getName().getString();

        // Replace name if it matches "atom20003113"
        if (name.equals("atom20003113")) {
            name = "hexagon";
        }

        // Convert to Component for rendering
        Component text = Component.literal(name);
        float yOffset = player.getBbHeight() + 0.5f;

        matrix.pushPose();
        matrix.translate(0.0D, yOffset, 0.0D);
        matrix.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        matrix.scale(-0.025f, -0.025f, 0.025f);

        Minecraft.getInstance().font.drawInBatch(
                text,
                -Minecraft.getInstance().font.width(text) / 2f,
                0f,
                0xFFFFFF,
                false,
                matrix.last().pose(),
                event.getMultiBufferSource(),
                Font.DisplayMode.NORMAL,
                0,      // packed light
                0       // packed overlay
        );

        matrix.popPose();
    }
}
