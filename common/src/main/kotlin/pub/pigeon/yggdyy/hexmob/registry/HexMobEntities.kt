package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity

object HexMobEntities {
    fun init() {
        ENTITIES.register()
    }
    private val ENTITIES: DeferredRegister<EntityType<*>> = DeferredRegister.create(HexMob.MODID, Registries.ENTITY_TYPE)
    val STIMULATED_PATTERN: DeferredSupplier<EntityType<StimulatedPatternEntity>> = ENTITIES.register("stimulated_pattern") {
        if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Register Entity")
        EntityType.Builder.of(
            { type, level -> StimulatedPatternEntity(type, level) },
            MobCategory.CREATURE
        ).sized(1F, 1F).build("stimulated_pattern")
    }
}