package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish.AmethystSilverfishRenderer
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystRenderer
import pub.pigeon.yggdyy.hexmob.content.iota_sheep.IotaSheepRenderer
import pub.pigeon.yggdyy.hexmob.content.quench_allay.QuenchAllayRenderer
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntityRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.renderers.SlateProjectileRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent.UrCircleSerpentRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.servant.UrCircleServantRenderer
import pub.pigeon.yggdyy.hexmob.content.guard.GuardArcherRenderer
import pub.pigeon.yggdyy.hexmob.content.guard.GuardBruteRenderer
import pub.pigeon.yggdyy.hexmob.content.guard.GuardGolemRenderer

object HexMobEntityRenderers {
    fun init() {
        if(HexMob.LOGGER.isDebugEnabled) HexMob.LOGGER.warn("Entity Renderers Init")
        EntityRendererRegistry.register(HexMobEntities.STIMULATED_PATTERN) { context -> StimulatedPatternEntityRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.CRYING_AMETHYST) { context -> CryingAmethystRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.AMETHYST_SILVERFISH) { context -> AmethystSilverfishRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.UR_CIRCLE) { context -> UrCircleRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.IOTA_SHEEP) { context -> IotaSheepRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.QUENCH_ALLAY) { context -> QuenchAllayRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.SLATE_PROJECTILE) { context -> SlateProjectileRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.UR_CIRCLE_SERPENT) { context -> UrCircleSerpentRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.UR_CIRCLE_SERVANT) { context -> UrCircleServantRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.GUARD_ARCHER) { context -> GuardArcherRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.GUARD_BRUTE) { context -> GuardBruteRenderer(context) }
        EntityRendererRegistry.register(HexMobEntities.GUARD_GOLEM) { context -> GuardGolemRenderer(context) }
    }
}