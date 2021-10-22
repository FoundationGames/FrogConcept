package foundationgames.frogconcept.entity.render;

import foundationgames.frogconcept.FrogConcept;
import foundationgames.frogconcept.entity.FireflyEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FireflyEntityRenderer extends EntityRenderer<FireflyEntity> {
    public static final Identifier TEXTURE = FrogConcept.id("textures/entity/firefly.png");

    public FireflyEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(FireflyEntity entity) {
        return null;
    }

    /**
     * Rendering is handled by a custom world rendering event, in order to be more performant (render more like particles)
     */
    @Override
    public boolean shouldRender(FireflyEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    public static void render(Camera camera, FireflyEntity entity, MatrixStack matrices, VertexConsumer buffer, float tickDelta) {
        matrices.multiply(camera.getRotation());

        float glow = (((float) Math.sin(((float) entity.age + tickDelta) * 0.2)) * 0.5f) + 0.5f;
        var model = matrices.peek().getModel();
        int overlay = OverlayTexture.DEFAULT_UV;
        int light = LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE;

        matrices.scale(0.07f, 0.07f, 0.07f);
        // Glowing part
        buffer.vertex(model, 0, -0.5f, 0).color(glow, glow, glow, 1).texture(0, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
        buffer.vertex(model, 0, 0.5f, 0).color(glow, glow, glow, 1).texture(0, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
        buffer.vertex(model, 1, 0.5f, 0).color(glow, glow, glow, 1).texture(0.5f, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
        buffer.vertex(model, 1, -0.5f, 0).color(glow, glow, glow, 1).texture(0.5f, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
        // Non glowing part
        buffer.vertex(model, -1, -0.5f, 0).color(1f, 1f, 1f, 1f).texture(0.5f, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
        buffer.vertex(model, -1, 0.5f, 0).color(1f, 1f, 1f, 1f).texture(0.5f, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
        buffer.vertex(model, 0, 0.5f, 0).color(1f, 1f, 1f, 1f).texture(1, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
        buffer.vertex(model, 0, -0.5f, 0).color(1f, 1f, 1f, 1f).texture(1, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
    }
}
