package foundationgames.frogconcept.entity;

import foundationgames.frogconcept.FrogConcept;
import net.minecraft.entity.EntityType;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
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
    public static final Type TEMPERATE = Type.register(FrogConcept.id("temperate"), biome -> true);

    public static final TrackedData<Type> FROG_TYPE = DataTracker.registerData(FrogEntity.class, Type.DATA_HANDLER);
    public static final TrackedData<Integer> LOOPING_ANIMATION = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.INTEGER);

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
        this.goalSelector.add(1, new CroakGoal(this));
        this.goalSelector.add(2, new WanderAroundGoal(this, 1, 50));
        this.goalSelector.add(3, new LookAroundGoal(this));
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
        return 1;
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
            return frog.idle && frog.world.random.nextInt(69) == 0;
        }

        @Override
        public void start() {
            frog.pushQueuedAnimIndex(ANIM_CROAK);
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
