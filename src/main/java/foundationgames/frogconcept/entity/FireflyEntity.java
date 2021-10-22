package foundationgames.frogconcept.entity;

import foundationgames.frogconcept.FrogConcept;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
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

        if (!this.world.isNight() && this.world.getTime() % 20 == 0 && world.random.nextInt(50) == 0) {
            this.remove(RemovalReason.DISCARDED);
        }

        this.setVelocity(this.getVelocity().add(movement));

        this.move(MovementType.SELF, getVelocity());
    }

    public void changeDirection() {
        var surfaceY = this.world.getTopY(Heightmap.Type.WORLD_SURFACE, getBlockX(), getBlockZ());
        // Keeps fireflies near the world surface
        var yCoax = MathHelper.clamp(((surfaceY + 1.5) - getY()) * 0.0016, -0.006, 0.006);

        this.movement = new Vec3d(
                (this.world.random.nextDouble() * 0.008) - 0.004,
                ((this.world.random.nextDouble() * 0.008) - 0.004) + yCoax,
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
