package foundationgames.frogconcept.entity.render;

import foundationgames.frogconcept.FrogConcept;
import foundationgames.frogconcept.entity.FrogEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class FrogEntityRenderer extends GeoEntityRenderer<FrogEntity> {
    public static final Identifier TEXTURE = FrogConcept.id("textures/entity/frog_tongue.png");

    public FrogEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new FrogEntityModel());
        this.shadowRadius = 0.3347f;
    }

    @Override
    public RenderLayer getRenderType(FrogEntity animatable, float partialTicks, MatrixStack stack, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityCutoutNoCull(getTextureLocation(animatable));
    }

    @Override
    public void renderEarly(FrogEntity entity, MatrixStack matrices, float ticks, VertexConsumerProvider vertexConsumers, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float partialTicks) {
        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        float ext = entity.getTongueExtension(tickDelta);
        if (ext > 0) {
            matrices.push();

            var tongueBuffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));

            matrices.translate(0, 0.25, 0);
            matrices.scale(0.0625f, 0.0625f, 0.0625f);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
            matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(entity.getTongueAngle()));

            buildTongueQuads(16 * entity.getTongueLength() * ext, tongueBuffer, matrices.peek().getModel(), matrices.peek().getNormal(), light, overlay);

            matrices.pop();
        }
    }

    private void buildTongueQuads(float lengthPx, VertexConsumer buffer, Matrix4f model, Matrix3f normal, int light, int overlay) {
        float endColor = 0.65f + (lengthPx * 0.00546875f); // 0.65 + ((length / 64) * 0.35);

        buffer.vertex(model, -1.5f, 0, 0).color(0.65f, 0.65f, 0.65f, 1).texture(0, 0).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
        buffer.vertex(model, 1.5f, 0, 0).color(0.65f, 0.65f, 0.65f, 1).texture(1, 0).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
        buffer.vertex(model, 1.5f, 0, lengthPx).color(endColor, endColor, endColor, 1).texture(1, lengthPx * 0.0625f).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
        buffer.vertex(model, -1.5f, 0, lengthPx).color(endColor, endColor, endColor, 1).texture(0, lengthPx * 0.0625f).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();

        buffer.vertex(model, -1.5f, 0, lengthPx).color(endColor, endColor, endColor, 1).texture(0, lengthPx * 0.0625f).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
        buffer.vertex(model, 1.5f, 0, lengthPx).color(endColor, endColor, endColor, 1).texture(1, lengthPx * 0.0625f).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
        buffer.vertex(model, 1.5f, 0, 0).color(0.65f, 0.65f, 0.65f, 1).texture(1, 0).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
        buffer.vertex(model, -1.5f, 0, 0).color(0.65f, 0.65f, 0.65f, 1).texture(0, 0).overlay(overlay).light(light).normal(normal, 0, 1, 0).next();
    }
}
