package team.nextlevelmodding.ar2.client;

import team.nextlevelmodding.ar2.Registry;
import team.nextlevelmodding.ar2.ar2;
import team.nextlevelmodding.ar2.particles.RocketFlameParticleProvider;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ar2.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Minecraft.getInstance().particleEngine.register(
                Registry.ROCKET_FLAME.get(),
                RocketFlameParticleProvider::new
        );
    }
}

