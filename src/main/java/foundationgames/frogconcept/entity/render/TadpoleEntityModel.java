package foundationgames.frogconcept.entity.render;

import foundationgames.frogconcept.FrogConcept;
import foundationgames.frogconcept.entity.TadpoleEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TadpoleEntityModel extends AnimatedGeoModel<TadpoleEntity> {
    public static final Identifier GEO = FrogConcept.id("geo/tadpole.geo.json");
    public static final Identifier TEXTURE = FrogConcept.id("textures/entity/tadpole.png");
    public static final Identifier ANIM = FrogConcept.id("animations/tadpole.animation.json");

    @Override
    public Identifier getModelLocation(TadpoleEntity entity) {
        return GEO;
    }

    @Override
    public Identifier getTextureLocation(TadpoleEntity entity) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationFileLocation(TadpoleEntity entity) {
        return ANIM;
    }
}
