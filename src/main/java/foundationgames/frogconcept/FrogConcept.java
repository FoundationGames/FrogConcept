package foundationgames.frogconcept;

import foundationgames.frogconcept.entity.FireflyEntity;
import foundationgames.frogconcept.entity.FrogEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import software.bernie.geckolib3.GeckoLib;

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
