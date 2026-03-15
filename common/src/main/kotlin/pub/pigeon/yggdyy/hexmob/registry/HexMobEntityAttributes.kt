package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity

object HexMobEntityAttributes {
    fun init() {
        EntityAttributeRegistry.register(HexMobEntities.STIMULATED_PATTERN) {
            if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Register Entity Attribute")
            StimulatedPatternEntity.registerAttributes()
        }
    }
}