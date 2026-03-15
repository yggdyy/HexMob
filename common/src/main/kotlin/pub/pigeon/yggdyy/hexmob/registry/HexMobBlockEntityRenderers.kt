package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateBlockEntityRenderer

object HexMobBlockEntityRenderers {
    fun init() {
        BlockEntityRendererRegistry.register(HexMobBlocks.STIMULATED_SLATE_TILE.get()) { context ->
            if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Register Block Entity Renderer")
            StimulatedSlateBlockEntityRenderer(
                context
            )
        }
    }
}