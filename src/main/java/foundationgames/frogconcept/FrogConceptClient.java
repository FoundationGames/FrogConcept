package foundationgames.frogconcept;

import foundationgames.frogconcept.entity.render.FrogEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FrogConceptClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FrogConcept.FROG, FrogEntityRenderer::new);
    }
}
