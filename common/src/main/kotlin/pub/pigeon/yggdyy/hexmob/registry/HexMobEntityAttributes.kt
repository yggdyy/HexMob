package pub.pigeon.yggdyy.hexmob.registry

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import at.petrak.hexcasting.common.lib.HexAttributes
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.entity.animal.allay.Allay
import pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish.AmethystSilverfishEntity
import pub.pigeon.yggdyy.hexmob.content.crying_amethyst.CryingAmethystEntity
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedPatternEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.servant.UrCircleServant
import pub.pigeon.yggdyy.hexmob.content.guard.GuardArcher
import pub.pigeon.yggdyy.hexmob.content.guard.GuardBrute
import pub.pigeon.yggdyy.hexmob.content.guard.GuardGolem

object HexMobEntityAttributes {
    fun init() {
        EntityAttributeRegistry.register(HexMobEntities.STIMULATED_PATTERN) { StimulatedPatternEntity.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.CRYING_AMETHYST) { CryingAmethystEntity.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.AMETHYST_SILVERFISH) {AmethystSilverfishEntity.registerAttributes()}
        EntityAttributeRegistry.register(HexMobEntities.UR_CIRCLE) {UrCircleEntity.registerAttributes()}
        EntityAttributeRegistry.register(HexMobEntities.UR_CIRCLE_SERVANT) {UrCircleServant.registerAttributes()}
        EntityAttributeRegistry.register(HexMobEntities.IOTA_SHEEP) { hexCastingAttributes(Sheep.createAttributes()) }
        EntityAttributeRegistry.register(HexMobEntities.QUENCH_ALLAY) { hexCastingAttributes(Allay.createAttributes()) }
        EntityAttributeRegistry.register(HexMobEntities.GUARD_ARCHER) { GuardArcher.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.GUARD_BRUTE) { GuardBrute.registerAttributes() }
        EntityAttributeRegistry.register(HexMobEntities.GUARD_GOLEM) { GuardGolem.registerAttributes() }
    }

    /**
     * A casting entity must expose Hex's casting attributes or the env NPEs —
     * CastingEnvironment.extractMedia reads MEDIA_CONSUMPTION_MODIFIER off the
     * caster, and the range checks read AMBIT/SENTINEL_RADIUS.
     *
     * Sane defaults, matching the allay's 32-block casting range:
     * media modifier 1.0 (no change), ambit 32.0, sentinel 32.0.
     */
    private fun hexCastingAttributes(builder: AttributeSupplier.Builder): AttributeSupplier.Builder =
        builder
            .add(HexAttributes.MEDIA_CONSUMPTION_MODIFIER, 1.0)
            .add(HexAttributes.AMBIT_RADIUS, 32.0)
            .add(HexAttributes.SENTINEL_RADIUS, 32.0)
}