package foundationgames.frogconcept.entity.render;

import foundationgames.frogconcept.entity.FrogEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class FrogEntityRenderer extends GeoEntityRenderer<FrogEntity> {
    public FrogEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new FrogEntityModel());
        this.shadowRadius = 0.3347f;
    }
}
