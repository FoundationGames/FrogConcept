package foundationgames.frogconcept;

import com.google.common.collect.Lists;
import foundationgames.frogconcept.entity.FireflyEntity;
import foundationgames.frogconcept.entity.FrogEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import software.bernie.geckolib3.GeckoLib;

import java.util.List;

public class FrogConcept implements ModInitializer {
    public static final EntityType<FrogEntity> FROG = Registry.register(
            Registry.ENTITY_TYPE,
            id("frog"),
            FabricEntityTypeBuilder.<FrogEntity>create(SpawnGroup.CREATURE, FrogEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.5f)).build()
    );

    public static final EntityType<FireflyEntity> FIREFLY = Registry.register(
            Registry.ENTITY_TYPE,
            id("firefly"),
            FabricEntityTypeBuilder.<FireflyEntity>create(SpawnGroup.AMBIENT, FireflyEntity::new).dimensions(EntityDimensions.fixed(0.08f, 0.08f)).build()
    );

    public static final Item FROG_SPAWN_EGG = Registry.register(Registry.ITEM, id("frog_spawn_egg"),
            new SpawnEggItem(FROG, 0xa66341, 0x96774c, new Item.Settings().group(ItemGroup.MISC))
    );

    public static final List<Biome.Category> FIREFLY_BIOME_CATEGORIES = Lists.newArrayList(Biome.Category.SWAMP);

    @Override
    public void onInitialize() {
        GeckoLib.initialize();

        FabricDefaultAttributeRegistry.register(FROG, FrogEntity.createAttributes());

        BiomeModifications.addSpawn(ctx ->
                ctx.getBiome().getCategory() == Biome.Category.SWAMP || ctx.getBiome().getCategory() == Biome.Category.JUNGLE || ctx.getBiomeKey() == BiomeKeys.FROZEN_RIVER || ctx.getBiomeKey() == BiomeKeys.DESERT_LAKES,
                SpawnGroup.CREATURE, FROG, 50, 1, 3
        );
        BiomeModifications.addSpawn(ctx ->
                        ctx.getBiome().getPrecipitation() == Biome.Precipitation.SNOW && ctx.getBiomeKey() != BiomeKeys.FROZEN_RIVER,
                SpawnGroup.CREATURE, FROG, 15, 1, 3
        );

        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.isNight()) {
                for (var player : world.getPlayers()) {
                    if (FIREFLY_BIOME_CATEGORIES.contains(world.getBiome(player.getBlockPos()).getCategory())
                            && world.getTime() % 10 == 0 && world.random.nextInt(5) < 3) {
                        var box = player.getBoundingBox().offset(0, world.getTopY(Heightmap.Type.WORLD_SURFACE, player.getBlockX(), player.getBlockZ()) - player.getBlockY(), 0).expand(20, 10, 20);
                        var fireflies = world.getEntitiesByClass(FireflyEntity.class, box, entity -> true);
                        if (fireflies.size() < 32) {
                            for (int i = 0; i < 5; i++) {
                                int x = player.getBlockX() + (int) (world.random.nextInt((int) box.getXLength()) - (box.getXLength() * 0.5));
                                int z = player.getBlockZ() + (int) (world.random.nextInt((int) box.getZLength()) - (box.getZLength() * 0.5));
                                var pos = new BlockPos(x, world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z), z);
                                if (world.getLightLevel(LightType.SKY, pos) > 5) {
                                    for (int j = 0; j < 1 + world.random.nextInt(5); j++) {
                                        var fly = new FireflyEntity(world);
                                        fly.refreshPositionAndAngles(pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(), pos.getZ() + world.random.nextDouble(), 0, 0);
                                        world.spawnEntity(fly);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(id("frog_queue_anim"), (client, handler, buf, responseSender) -> {
            if (client.world == null) return;
            var entity = client.world.getEntityById(buf.readInt());
            int anim = buf.readInt();
            client.execute(() -> {
                if (entity instanceof FrogEntity frog) {
                    frog.pushQueuedAnimIndex(anim);
                }
            });
        });
    }

    public static void queueFrogAnim(ServerPlayerEntity player, FrogEntity frog, int anim) {
        var buf = PacketByteBufs.create();
        buf.writeInt(frog.getId());
        buf.writeInt(anim);
        ServerPlayNetworking.send(player, id("frog_queue_anim"), buf);
    }

    public static Identifier id(String path) {
        return new Identifier("frogconcept", path);
    }
}
