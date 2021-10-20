package foundationgames.frogconcept.entity;

import foundationgames.frogconcept.FrogConcept;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FireflyEntity extends Entity {
    private Vec3d movement = null;

    public FireflyEntity(EntityType<?> type, World world) {
        super(type, world);
        this.age = world.random.nextInt(50);
    }

    public FireflyEntity(World world) {
        this(FrogConcept.FIREFLY, world);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (this.movement == null || this.world.getTime() % 3 == 0) {
            changeDirection();
        }

        this.setVelocity(this.getVelocity().add(movement));

        this.move(MovementType.SELF, getVelocity());
    }

    public void changeDirection() {
        this.movement = new Vec3d(
                (this.world.random.nextDouble() * 0.008) - 0.004,
                (this.world.random.nextDouble() * 0.008) - 0.004,
                (this.world.random.nextDouble() * 0.008) - 0.004
        );
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
