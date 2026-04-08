package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish.AmethystSilverfishEntity
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystEntity
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity

object HexMobEntityAttributes {
    fun init() {
        EntityAttributeRegistry.register(HexMobEntities.STIMULATED_PATTERN) { StimulatedPatternEntity.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.CRYING_AMETHYST) { CryingAmethystEntity.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.AMETHYST_SILVERFISH) {AmethystSilverfishEntity.registerAttributes()}
        EntityAttributeRegistry.register(HexMobEntities.UR_CIRCLE) {UrCircleEntity.registerAttributes()}
    }
}