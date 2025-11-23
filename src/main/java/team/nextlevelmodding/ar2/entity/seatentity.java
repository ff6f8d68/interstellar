package team.nextlevelmodding.ar2.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class seatentity extends Entity {
    public seatentity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    protected void defineSynchedData() {

    }
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        // Seat height + player's eye offset
        double seatHeight = this.getBbHeight(); // 0.25
        double playerHeight = passenger.getBbHeight(); // usually 1.8 for players
        double offset = Math.max(seatHeight, 0.25) + 0.1; // small extra buffer

        // Place the player just above the seat
        return this.position().add(0, offset, 0);
    }



    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
    @Override
    public void removePassenger(Entity pPassenger) {
        super.removePassenger(pPassenger);
        this.kill();
    }
}
