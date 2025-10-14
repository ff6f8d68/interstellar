package mods.hexagonal.interstellar.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpaceVoidBlock extends Block {
    private static final VoxelShape EMPTY_SHAPE = Block.box(0, 0, 0, 0, 0, 0);
    private static final ResourceLocation SPACE_VOID_ID = new ResourceLocation("interstellar", "space_void");

    public SpaceVoidBlock() {
        super(BlockBehaviour.Properties.of()
                .air()
                .noCollission()     // allows walking through
                .noOcclusion()      // no shadow / line-of-sight blockage
                .instabreak()   // Behaves like air
        );
    }
    @Override
    public boolean isAir(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return EMPTY_SHAPE;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }




    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }




    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return true;
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            // Use the registered damage type from your datapack
            if (level instanceof ServerLevel serverLevel) {
                var damageTypeHolder = serverLevel.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,
                                new ResourceLocation("minecraft", "out_of_world")));

                DamageSource src = new DamageSource(damageTypeHolder);
                living.hurt(src, 1.0F);
            }
        }
    }
}
