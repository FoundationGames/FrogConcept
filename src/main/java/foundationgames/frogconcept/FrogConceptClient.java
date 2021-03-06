package foundationgames.frogconcept;

import foundationgames.frogconcept.entity.FireflyEntity;
import foundationgames.frogconcept.entity.FrogEntity;
import foundationgames.frogconcept.entity.render.FireflyEntityRenderer;
import foundationgames.frogconcept.entity.render.FrogEntityRenderer;
import foundationgames.frogconcept.entity.render.TadpoleEntityRenderer;
import foundationgames.frogconcept.mixin.WorldRendererAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;

public class FrogConceptClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FrogConcept.FROG, FrogEntityRenderer::new);
        EntityRendererRegistry.register(FrogConcept.TADPOLE, TadpoleEntityRenderer::new);
        EntityRendererRegistry.register(FrogConcept.FIREFLY, FireflyEntityRenderer::new);

        WorldRenderEvents.END.register(context -> {
            var access = (WorldRendererAccess)context.worldRenderer();
            var immediate = access.getBufferBuilders().getEntityVertexConsumers();
            var buffer = immediate.getBuffer(RenderLayer.getEntityCutout(FireflyEntityRenderer.TEXTURE));
            var matrices = context.matrixStack();

            matrices.push();

            var cPos = context.camera().getPos();
            matrices.translate(-cPos.x, -cPos.y, -cPos.z);
            for (var entity : context.world().getEntities()) {
                if (entity instanceof FireflyEntity firefly) {
                    matrices.push();

                    var pos = firefly.getLerpedPos(context.tickDelta());
                    matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                    FireflyEntityRenderer.render(context.camera(), firefly, matrices, buffer, context.tickDelta());

                    matrices.pop();
                }
            }
            immediate.draw();

            matrices.pop();
        });

        ClientPlayNetworking.registerGlobalReceiver(FrogConcept.id("frog_queue_anim"), (client, handler, buf, responseSender) -> {
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
}
