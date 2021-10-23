package foundationgames.frogconcept.entity;

import foundationgames.frogconcept.FrogConcept;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TadpoleEntity extends FishEntity implements IAnimatable, Bucketable {
    public static final String ANIM_SWIM = "animation.tadpole.swim";
    public static final String ANIM_FLOP = "animation.tadpole.flop";

    private final AnimationFactory animations = new AnimationFactory(this);
    private boolean inWater = true;
    private boolean wasInWater = false;

    private int growth = 0;

    public TadpoleEntity(EntityType<? extends FishEntity> type, World world) {
        super(type, world);
    }

    public TadpoleEntity(World world) {
        this(FrogConcept.TADPOLE, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 7.0D);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (world.isClient()) {
            wasInWater = inWater;
            inWater = this.isTouchingWater();
        } else {
            if (growth >= 0) {
                growth++;
                if (growth > 20000) {
                    becomeFrog();
                }
            }
        }
    }

    public void becomeFrog() {
        var frog = new FrogEntity(world);
        frog.refreshPositionAndAngles(getX(), getY(), getZ(), getYaw(), getPitch());
        frog.setTypeForSpawn();
        if (this.hasCustomName()) {
            frog.setCustomName(this.getCustomName());
        }
        for (var effect : this.getStatusEffects()) {
            frog.addStatusEffect(effect);
        }

        this.remove(RemovalReason.DISCARDED);
        world.spawnEntity(frog);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt("Growth", this.growth);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        this.growth = nbt.getInt("Growth");
    }

    private PlayState animationRefresh(AnimationEvent<TadpoleEntity> event) {
        if (this.inWater != this.wasInWater) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(this.inWater ? ANIM_SWIM : ANIM_FLOP));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData anims) {
        anims.addAnimationController(new AnimationController<>(this, "controller", 2f, this::animationRefresh));
    }

    @Override
    public AnimationFactory getFactory() {
        return animations;
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(FrogConcept.TADPOLE_BUCKET);
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
    }
}
