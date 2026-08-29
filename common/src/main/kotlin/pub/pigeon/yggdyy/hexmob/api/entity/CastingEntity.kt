package pub.pigeon.yggdyy.hexmob.api.entity

/**
 * A living entity that casts hexes on its own, providing its own media pool and
 * casting ambit.
 *
 * Kept as a pure interface so any mob (regardless of its concrete Entity
 * hierarchy) can opt in. Media storage/syncing is the implementing entity's
 * responsibility.
 */
interface CastingEntity {
    /**
     * Attempt to consume [cost] media.
     *
     * Must follow the [CastingEnvironment][] media contract: return the amount
     * of media that could NOT be paid (i.e. the leftover cost), and honor
     * [simulate] by not actually removing anything when it is true.
     *
     * @return leftover (unpaid) media; <= 0 when the cost was fully covered.
     */
    fun consumeMedia(cost: Long, simulate: Boolean): Long

    /** How far (in blocks) this entity can reach with its spells. */
    fun getCastingRange(): Double

    /**
     * Whether this entity counts as enlightened for spellcasting. The vanilla
     * env only grants enlightenment to players with the advancement; an
     * entity-based caster opts in by overriding this to true (unlocks great
     * spells for it).
     */
    fun isEnlightened(): Boolean = false
}
