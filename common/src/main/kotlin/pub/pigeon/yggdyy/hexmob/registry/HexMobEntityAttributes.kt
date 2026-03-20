package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystEntity
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity

object HexMobEntityAttributes {
    fun init() {
        EntityAttributeRegistry.register(HexMobEntities.STIMULATED_PATTERN) { StimulatedPatternEntity.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.CRYING_AMETHYST) { CryingAmethystEntity.registerAttributes() }
    }
}