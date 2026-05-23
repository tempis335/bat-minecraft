package io.github.tempis335.batminecraft.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(BatEntity.class)
public abstract class BatEntityMixin extends AmbientEntity {
    @Unique
    private static final double WAKE_RANGE = 4.0D;

    @Unique
    private static final double FORGET_RANGE = 24.0D;

    @Unique
    private static final double ATTACK_RANGE = 1.3D;

    @Unique
    private static final double FLIGHT_SPEED = 0.12D;

    @Unique
    private static final float ATTACK_DAMAGE = 2.0F;

    @Unique
    private static final int ATTACK_COOLDOWN = 20;

    @Unique
    private int batMinecraft$attackCooldown;

    protected BatEntityMixin(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isRoosting();

    @Shadow
    public abstract void setRoosting(boolean roosting);

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void batMinecraft$wakeIfPlayerApproaches(ServerWorld world, CallbackInfo callbackInfo) {
        if (!isRoosting()) {
            return;
        }

        PlayerEntity player = batMinecraft$findPlayerThatWokeBat();

        if (player == null) {
            return;
        }

        setRoosting(false);
        setTarget(player);
        playSound(SoundEvents.ENTITY_BAT_TAKEOFF, 1.0F, 1.0F);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void batMinecraft$tick(CallbackInfo callbackInfo) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        if (batMinecraft$attackCooldown > 0) {
            batMinecraft$attackCooldown--;
        }

        LivingEntity target = getTarget();

        if (!batMinecraft$isValidTarget(target)) {
            setTarget(null);
            return;
        }

        batMinecraft$flyToward(target);
        batMinecraft$tryAttack(serverWorld, target);
    }

    @Unique
    private PlayerEntity batMinecraft$findPlayerThatWokeBat() {
        Box searchBox = getBoundingBox().expand(WAKE_RANGE);
        List<PlayerEntity> players = getWorld().getEntitiesByClass(PlayerEntity.class, searchBox, this::batMinecraft$canAttack);

        return players.stream()
            .min(Comparator.comparingDouble(this::squaredDistanceTo))
            .orElse(null);
    }

    @Unique
    private boolean batMinecraft$isValidTarget(LivingEntity target) {
        return target instanceof PlayerEntity player
            && batMinecraft$canAttack(player)
            && squaredDistanceTo(player) <= FORGET_RANGE * FORGET_RANGE;
    }

    @Unique
    private boolean batMinecraft$canAttack(PlayerEntity player) {
        return player.isAlive()
            && !player.isSpectator()
            && !player.isCreative();
    }

    @Unique
    private void batMinecraft$flyToward(LivingEntity target) {
        Vec3d direction = target.getEyePos().subtract(getPos());

        if (direction.lengthSquared() < 0.001D) {
            return;
        }

        Vec3d steering = direction.normalize().multiply(FLIGHT_SPEED);
        setVelocity(getVelocity().multiply(0.85D).add(steering));
        getLookControl().lookAt(target, 30.0F, 30.0F);
        velocityDirty = true;
    }

    @Unique
    private void batMinecraft$tryAttack(ServerWorld world, LivingEntity target) {
        if (batMinecraft$attackCooldown > 0 || squaredDistanceTo(target) > ATTACK_RANGE * ATTACK_RANGE) {
            return;
        }

        target.damage(world, getDamageSources().mobAttack(this), ATTACK_DAMAGE);
        batMinecraft$attackCooldown = ATTACK_COOLDOWN;
    }
}
