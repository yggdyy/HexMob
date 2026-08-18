package pub.pigeon.yggdyy.hexmob.api.entity

import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity

/**
 * Helpers for implementing [CastingEntity] when the entity pays media from
 * amethyst / media-holding items held in its hands. A typical [CastingEntity]
 * implementation becomes:
 *
 * ```
 * override fun consumeMedia(cost: Long, simulate: Boolean) =
 *     extractMediaFromHands(cost, simulate)
 * ```
 */

/** Total media currently available in media-holding items held in both hands. */
fun LivingEntity.mediaInHands(): Long {
    var total = 0L
    for (hand in arrayOf(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
        total += IXplatAbstractions.INSTANCE.findMediaHolder(this.getItemInHand(hand))?.getMedia() ?: 0L
    }
    return total
}

/**
 * Extract up to [cost] media from media-holding items held in both hands.
 *
 * Follows [CastingEntity.consumeMedia]'s contract: returns the amount of media
 * that could NOT be paid (leftover cost), and honors [simulate] (nothing is
 * actually removed when it is true).
 */
fun LivingEntity.extractMediaFromHands(cost: Long, simulate: Boolean): Long {
    var costLeft = cost
    for (hand in arrayOf(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
        if (costLeft <= 0) break
        val holder = IXplatAbstractions.INSTANCE.findMediaHolder(this.getItemInHand(hand)) ?: continue
        costLeft -= holder.withdrawMedia(costLeft, simulate)
    }
    return costLeft
}
