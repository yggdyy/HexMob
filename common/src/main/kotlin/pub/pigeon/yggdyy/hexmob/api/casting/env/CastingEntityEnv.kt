package pub.pigeon.yggdyy.hexmob.api.casting.env

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import pub.pigeon.yggdyy.hexmob.api.entity.CastingEntity

/**
 * A [MobCastingEnv] specialized to a mob that implements [CastingEntity]: the
 * mob supplies its own media pool ([CastingEntity.consumeMedia]) and casting
 * ambit ([CastingEntity.getCastingRange]) instead of holding amethyst in its hands.
 *
 * The caster is expected to also be a [LivingEntity]; that assumption is what
 * lets [MobCastingEnv] run on it.
 */
class CastingEntityEnv(
    caster: CastingEntity,
    castingHand: InteractionHand = InteractionHand.MAIN_HAND,
) : MobCastingEnv(caster as LivingEntity, castingHand) {

    private val castingEntity: CastingEntity = caster

    /**
     * Read live from the entity so a changing [CastingEntity.getCastingRange] is
     * honored. The base property is a `var`, so this stays a `var` with a no-op
     * setter (nobody writes it here).
     */
    override var castRange: Double
        get() = castingEntity.getCastingRange()
        set(_) {}

    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long =
        castingEntity.consumeMedia(cost, simulate)
}
