package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish.AmethystSilverfishEntity
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystEntity
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
    val CRYING_AMETHYST: DeferredSupplier<EntityType<CryingAmethystEntity>> = ENTITIES.register("crying_amethyst") {
        EntityType.Builder.of(
            {type, level -> CryingAmethystEntity(type, level)},
            MobCategory.CREATURE
        ).sized(1F, 1F).build("crying_amethyst")
    }
    val AMETHYST_SILVERFISH: DeferredSupplier<EntityType<AmethystSilverfishEntity>> = ENTITIES.register("amethyst_silverfish") {
        EntityType.Builder.of(
            {type, level -> AmethystSilverfishEntity(type, level)},
            MobCategory.MONSTER
        ).sized(0.375F, 0.25F).build("amethyst_silverfish")
    }
}