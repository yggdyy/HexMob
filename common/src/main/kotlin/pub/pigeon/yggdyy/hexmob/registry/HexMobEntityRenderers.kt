package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntityRenderer

object HexMobEntityRenderers {
    fun init() {
        if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Entity Renderers Init")
        EntityRendererRegistry.register(HexMobEntities.STIMULATED_PATTERN) { context ->
            if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Register Entity Renderer")
            StimulatedPatternEntityRenderer(
                context
            )
        }
    }
}