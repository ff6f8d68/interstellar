package team.nextlevelmodding.ar2.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.nextlevelmodding.ar2.ar2;

public class modentitys {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ar2.MOD_ID);
    public static final RegistryObject<EntityType<seatentity>> seat =
            ENTITY_TYPES.register("seatentity", () -> EntityType.Builder.of(seatentity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).build("seatentity"));
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
