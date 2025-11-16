package team.nextlevelmodding.ar2;



import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;
import team.nextlevelmodding.ar2.gui.GuinuclearengineMenu;


public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ar2.MOD_ID);
    public static final RegistryObject<MenuType<GuinuclearengineMenu>> GUINUCLEARENGINE =
            REGISTRY.register("guinuclearengine",
                    () -> IForgeMenuType.create((containerId, inventory, data) ->
                            new GuinuclearengineMenu(containerId, inventory, data.readBlockPos())
                    ));


}
