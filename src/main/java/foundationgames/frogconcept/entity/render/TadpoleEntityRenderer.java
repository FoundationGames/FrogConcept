package foundationgames.frogconcept.entity.render;

import foundationgames.frogconcept.entity.TadpoleEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TadpoleEntityRenderer extends GeoEntityRenderer<TadpoleEntity> {
    public TadpoleEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new TadpoleEntityModel());
        this.shadowRadius = 3f / 16;
    }
}
