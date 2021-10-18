package foundationgames.frogconcept;

import foundationgames.frogconcept.entity.FrogEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import software.bernie.geckolib3.GeckoLib;

public class FrogConcept implements ModInitializer {
    public static final EntityType<FrogEntity> FROG = Registry.register(
            Registry.ENTITY_TYPE,
            id("frog"),
            FabricEntityTypeBuilder.<FrogEntity>create(SpawnGroup.CREATURE, FrogEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.5f)).build()
    );

    @Override
    public void onInitialize() {
        GeckoLib.initialize();

        FabricDefaultAttributeRegistry.register(FROG, FrogEntity.createAttributes());
    }

    public static Identifier id(String path) {
        return new Identifier("frogconcept", path);
    }
}
