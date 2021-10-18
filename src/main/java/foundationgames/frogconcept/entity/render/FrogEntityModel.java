package foundationgames.frogconcept.entity.render;

import foundationgames.frogconcept.FrogConcept;
import foundationgames.frogconcept.entity.FrogEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class FrogEntityModel extends AnimatedGeoModel<FrogEntity> {
    public static final Identifier GEO = FrogConcept.id("geo/frog.geo.json");
    public static final Identifier ANIM = FrogConcept.id("animations/frog.animation.json");

    @Override
    public Identifier getModelLocation(FrogEntity entity) {
        return GEO;
    }

    @Override
    public Identifier getTextureLocation(FrogEntity entity) {
        return entity.getFrogType().getTexture();
    }

    @Override
    public Identifier getAnimationFileLocation(FrogEntity entity) {
        return ANIM;
    }
}
