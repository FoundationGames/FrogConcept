package foundationgames.frogconcept.entity;

import foundationgames.frogconcept.FrogConcept;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.*;
import java.util.function.Predicate;

public class FrogEntity extends AnimalEntity implements IAnimatable {
    public static final Type TEMPERATE = Type.register(FrogConcept.id("temperate"), biome ->
            biome.getPrecipitation() != Biome.Precipitation.SNOW &&
            !(biome.getCategory() == Biome.Category.JUNGLE || biome.getCategory() == Biome.Category.DESERT)
    );
    public static final Type SNOWY = Type.register(FrogConcept.id("snowy"), biome -> biome.getPrecipitation() == Biome.Precipitation.SNOW);
    public static final Type TROPICAL = Type.register(FrogConcept.id("tropical"), biome -> biome.getCategory() == Biome.Category.JUNGLE || biome.getCategory() == Biome.Category.DESERT);

    public static final TrackedData<Type> FROG_TYPE = DataTracker.registerData(FrogEntity.class, Type.DATA_HANDLER);
    public static final TrackedData<Integer> LOOPING_ANIMATION = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> EAT_TIME = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final String[] ANIMS = {
            "animation.frog.idle",
            "animation.frog.croak",
            "animation.frog.walk",
            "animation.frog.swim",
            "animation.frog.ascend",
            "animation.frog.descend",
            "animation.frog.eat",
            "animation.frog.chew"
    };
    public static final int ANIM_IDLE = 0;
    public static final int ANIM_CROAK = 1;
    public static final int ANIM_WALK = 2;
    public static final int ANIM_SWIM = 3;
    public static final int ANIM_ASCEND = 4;
    public static final int ANIM_DESCEND = 5;
    public static final int ANIM_EAT = 6;
    public static final int ANIM_CHEW = 7;

    public static final int MAX_EAT_TIME = 7;

    private final AnimationFactory animations = new AnimationFactory(this);

    private boolean updateAnimation = false;
    private Deque<Integer> animationQueue = new ArrayDeque<>();

    private boolean idle = true;

    public FrogEntity(World world) {
        this(FrogConcept.FROG, world);
    }

    public FrogEntity(EntityType<FrogEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!world.isClient()) {
            this.animationTick();

            if (getEatTime() > 0) {
                setEatTime(getEatTime() - 1);
                if (getEatTime() == 0 && idle) {
                    pushQueuedAnimIndex(ANIM_CHEW);
                }
            }
        }
    }

    protected void animationTick() {
        var vel = getVelocity();
        int anim = -1;
        this.idle = true;
        if (!this.isTouchingWater() && !this.isOnGround()) {
            if (vel.getY() > 0) {
                anim = ANIM_ASCEND;
            } else if (vel.getY() < 0) {
                anim = ANIM_DESCEND;
            }
            this.idle = false;
        }
        if (vel.getX() != 0 || vel.getZ() != 0 || this.moveControl.isMoving()) {
            if (this.isOnGround()) {
                anim = ANIM_WALK;
            } else if (this.isTouchingWater()) {
                anim = ANIM_SWIM;
            }
            this.idle = false;
        } else {
            anim = ANIM_IDLE;
        }
        if (anim >= 0) {
            setLoopAnimIndex(anim);
        }
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SwimAroundGoal(this, 4.20, 69));
        this.goalSelector.add(2, new EatFireflyGoal(this));
        this.goalSelector.add(3, new CroakGoal(this));
        this.goalSelector.add(4, new JumpRandomlyGoal(this));
        this.goalSelector.add(5, new WanderAroundGoal(this, 1, 80));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("FrogType", getFrogType().id.toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setFrogType(Type.fromString(nbt.getString("FrogType")));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        setTypeForSpawn();
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return super.computeFallDamage(Math.max(0, fallDistance - 3), damageMultiplier);
    }

    @Override
    public double getSwimHeight() {
        return 0.15;
    }

    public void setTypeForSpawn() {
        setFrogType(Type.forSpawn(world.getBiome(getBlockPos()), world.random));
    }

    public Type getFrogType() {
        return this.dataTracker.get(FROG_TYPE);
    }

    public void setFrogType(Type type) {
        this.dataTracker.set(FROG_TYPE, type == null ? Type.defaultType() : type);
    }

    public int getLoopAnimIndex() {
        return this.dataTracker.get(LOOPING_ANIMATION);
    }

    public void setLoopAnimIndex(int index) {
        this.dataTracker.set(LOOPING_ANIMATION, index);
    }

    public int getEatTime() {
        return this.dataTracker.get(EAT_TIME);
    }

    public void setEatTime(int time) {
        this.dataTracker.set(EAT_TIME, time);
    }

    public void pushQueuedAnimIndex(int index) {
        if (world.isClient()) {
            this.animationQueue.push(index);
            return;
        }

        world.getPlayers().forEach(player -> {
            if (player.getPos().distanceTo(getPos()) < 96 && player instanceof ServerPlayerEntity sPlayer) {
                FrogConcept.queueFrogAnim(sPlayer, this, index);
            }
        });
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FROG_TYPE, Type.defaultType());
        this.dataTracker.startTracking(LOOPING_ANIMATION, ANIM_IDLE);
        this.dataTracker.startTracking(EAT_TIME, 0);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (data.equals(LOOPING_ANIMATION) && world.isClient()) {
            updateAnimation = true;
        }
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 7.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.075);
    }

    @Override
    public int getBodyYawSpeed() {
        return 0;
    }

    private PlayState animationRefresh(AnimationEvent<FrogEntity> event) {
        if (animationQueue.size() > 0) {
            var anims = new AnimationBuilder();
            for (int i : animationQueue) {
                i = Math.max(0, i);
                if (i > ANIMS.length) i = 0;
                anims.addAnimation(ANIMS[i]);
            }
            animationQueue.clear();
            event.getController().setAnimation(anims);
            event.getController().markNeedsReload();
        } else if (updateAnimation) {
            int index = Math.max(0, getLoopAnimIndex());
            if (index > ANIMS.length) index = 0;
            updateAnimation = false;
            event.getController().setAnimation(new AnimationBuilder().addAnimation(ANIMS[index]));
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

    public static class CroakGoal extends Goal {
        private final FrogEntity frog;

        public CroakGoal(FrogEntity frog) {
            this.frog = frog;
        }

        @Override
        public boolean canStart() {
            return frog.idle && frog.world.random.nextInt(60) == 0;
        }

        @Override
        public void start() {
            frog.pushQueuedAnimIndex(ANIM_CROAK);
        }
    }

    public static class JumpRandomlyGoal extends Goal {
        private final FrogEntity frog;

        public JumpRandomlyGoal(FrogEntity frog) {
            this.frog = frog;
        }

        @Override
        public boolean canStart() {
            return frog.idle && frog.world.random.nextInt(40) == 0;
        }

        @Override
        public void start() {
            var targetPos = NoPenaltyTargeting.find(frog, 1, 4);
            if (targetPos == null) return;

            frog.lookControl.lookAt(targetPos.x, targetPos.y, targetPos.z);
            frog.setJumping(true);

            var difference = targetPos.subtract(frog.getPos());
            double minHeight = 0.7 + (frog.world.random.nextFloat() * 0.4);
            frog.setVelocity(difference.x * 0.32, (Math.max(minHeight, difference.y * 0.8) * 0.6) * frog.getJumpVelocityMultiplier(), difference.z * 0.32);
        }
    }

    public static class EatFireflyGoal extends Goal {
        private final FrogEntity frog;

        public EatFireflyGoal(FrogEntity frog) {
            this.frog = frog;
        }

        @Override
        public boolean canStart() {
            return frog.idle && frog.world.random.nextInt(30) == 0;
        }

        @Override
        public void start() {
            var flies = frog.world.getEntitiesByClass(FireflyEntity.class, frog.getBoundingBox().expand(2, 3, 2), e -> true);
            if (flies.size() > 0) {
                var fly = flies.get(0);
                frog.lookControl.lookAt(fly.getX(), fly.getY(), fly.getZ());
                frog.pushQueuedAnimIndex(ANIM_EAT);
                frog.setEatTime(MAX_EAT_TIME);
                fly.remove(RemovalReason.KILLED);
            }
        }
    }

    public static class Type {
        private static final Map<Identifier, Type> ENTRIES = new HashMap<>();

        public final Identifier id;

        private final Identifier texture;
        private final Predicate<Biome> spawnCondition;

        private Type(Identifier id, Predicate<Biome> spawnCondition) {
            this.id = id;
            this.texture = new Identifier(id.getNamespace(), "textures/entity/frog/"+id.getPath()+".png");
            this.spawnCondition = spawnCondition;
        }

        public Identifier getTexture() {
            return texture;
        }

        public static Type fromString(String id) {
            return ENTRIES.getOrDefault(Identifier.tryParse(id), defaultType());
        }

        public static Type forSpawn(Biome biome, Random random) {
            var candidates = new ArrayList<Type>();

            for (var type : ENTRIES.values()) {
                if (type.spawnCondition.test(biome)) {
                    candidates.add(type);
                }
            }

            if (candidates.size() <= 0) {
                return defaultType();
            }

            return candidates.get(random.nextInt(candidates.size()));
        }

        public static Type register(Identifier id, Predicate<Biome> spawnCondition) {
            var type = new Type(id, spawnCondition);
            ENTRIES.put(id, type);
            return type;
        }

        private static Type defaultType() {
            return TEMPERATE;
        }

        public static final TrackedDataHandler<Type> DATA_HANDLER = new TrackedDataHandler<>() {
            @Override
            public void write(PacketByteBuf buf, Type value) {
                buf.writeString(value.id.toString());
            }

            @Override
            public Type read(PacketByteBuf buf) {
                return Type.fromString(buf.readString());
            }

            @Override
            public Type copy(Type value) {
                return value;
            }
        };

        static {
            TrackedDataHandlerRegistry.register(DATA_HANDLER);
        }
    }
}
