package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish.AmethystSilverfishRenderer
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystRenderer
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntityRenderer

object HexMobEntityRenderers {
    fun init() {
        if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Entity Renderers Init")
        EntityRendererRegistry.register(HexMobEntities.STIMULATED_PATTERN) { context -> StimulatedPatternEntityRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.CRYING_AMETHYST) { context -> CryingAmethystRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.AMETHYST_SILVERFISH) { context -> AmethystSilverfishRenderer(context) }
    }
}