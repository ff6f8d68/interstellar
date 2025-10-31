import mods.hexagonal.ar2.ModBlocks;
import mods.hexagonal.ar2.ModCreativeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ar2", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == null) {
            ModBlocks.ITEMS.getEntries().forEach(regObj -> event.accept(regObj.get()));
        }
    }
}
