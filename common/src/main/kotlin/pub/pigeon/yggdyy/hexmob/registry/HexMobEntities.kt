package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish.AmethystSilverfishEntity
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystEntity
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepEntity
import pub.pigeon.yggdyy.hexmob.content.quench_allay.QuenchAllay
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.SlateProjectile
import pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent.UrCircleSerpent
import pub.pigeon.yggdyy.hexmob.content.ur_circle.servant.UrCircleServant
import pub.pigeon.yggdyy.hexmob.content.guard.GuardArcher
import pub.pigeon.yggdyy.hexmob.content.guard.GuardBrute
import pub.pigeon.yggdyy.hexmob.content.guard.GuardGolem

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
    val UR_CIRCLE: DeferredSupplier<EntityType<UrCircleEntity>> = ENTITIES.register("ur_circle") {
        EntityType.Builder.of(
            {type, level -> UrCircleEntity(type, level)},
            MobCategory.MISC
        ).sized(10F, 6F).build("ur_circle")
    }
    val IOTA_SHEEP: DeferredSupplier<EntityType<IotaSheepEntity>> = ENTITIES.register("iota_sheep") {
        EntityType.Builder.of(
            { type, level -> IotaSheepEntity(type, level) },
            MobCategory.CREATURE
        ).sized(0.9F, 1.3F).build("iota_sheep")
    }
    val QUENCH_ALLAY: DeferredSupplier<EntityType<QuenchAllay>> = ENTITIES.register("quench_allay") {
        EntityType.Builder.of(
            { type, level -> QuenchAllay(type, level) },
            MobCategory.CREATURE
        ).sized(0.35F, 0.6F).build("quench_allay")
    }
    val SLATE_PROJECTILE: DeferredSupplier<EntityType<SlateProjectile>> = ENTITIES.register("slate_projectile") {
        EntityType.Builder.of(
            { type, level -> SlateProjectile(type, level) },
            MobCategory.MISC
        ).sized(0.5F, 0.5F).build("slate_projectile")
    }
    val UR_CIRCLE_SERPENT: DeferredSupplier<EntityType<UrCircleSerpent>> = ENTITIES.register("ur_circle_serpent") {
        EntityType.Builder.of(
            { type, level -> UrCircleSerpent(type, level) },
            MobCategory.MISC
        ).sized(0.9F, 0.9F).build("ur_circle_serpent")
    }
    val UR_CIRCLE_SERVANT: DeferredSupplier<EntityType<UrCircleServant>> = ENTITIES.register("ur_circle_servant") {
        EntityType.Builder.of(
            { type, level -> UrCircleServant(type, level) },
            MobCategory.MONSTER
        ).sized(0.6F, 1.2F).build("ur_circle_servant")
    }
    val GUARD_ARCHER: DeferredSupplier<EntityType<GuardArcher>> = ENTITIES.register("guard_archer") {
        EntityType.Builder.of(
            { type, level -> GuardArcher(type, level) },
            MobCategory.MONSTER
        ).sized(0.6F, 1.95F).build("guard_archer")
    }
    val GUARD_BRUTE: DeferredSupplier<EntityType<GuardBrute>> = ENTITIES.register("guard_brute") {
        EntityType.Builder.of(
            { type, level -> GuardBrute(type, level) },
            MobCategory.MONSTER
        ).sized(0.6F, 1.95F).build("guard_brute")
    }
    val GUARD_GOLEM: DeferredSupplier<EntityType<GuardGolem>> = ENTITIES.register("guard_golem") {
        EntityType.Builder.of(
            { type, level -> GuardGolem(type, level) },
            MobCategory.MONSTER
        ).sized(1.4F, 2.7F).build("guard_golem")
    }
}