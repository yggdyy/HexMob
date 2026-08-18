package pub.pigeon.yggdyy.hexmob.api.casting.env

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.otherHand
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.api.entity.extractMediaFromHands
import java.util.function.Predicate

/**
 * A [CastingEnvironment] driven by an arbitrary [LivingEntity] (a mob), not a player.
 *
 * Designed for mobs that cast hexes on their own:
 * - media is paid from amethyst/media-holding items held in the mob's hands,
 * - the "ambit" is a sphere around the mob of [castRange] blocks,
 * - mobs always have edit permissions (they are not gated by gamemode),
 * - miscasts manifest as damage to the mob (via [MobMishapEnv]).
 *
 * Only sane on the server — the caster's level is cast to [ServerLevel].
 */
open class MobCastingEnv(
    open val caster: LivingEntity,
    castingHand: InteractionHand = InteractionHand.MAIN_HAND,
) : CastingEnvironment(caster.level() as ServerLevel) {

    // Stored under a different name than the Java getter `getCastingHand()` so the
    // Kotlin property getter does not clash with the overridden method above.
    private val heldCastingHand: InteractionHand = castingHand

    /** How far the mob can reach with spells, in blocks. Tweak freely. */
    open var castRange: Double = 16.0

    override fun getCastingEntity(): LivingEntity = caster

    override fun getMishapEnvironment(): MishapEnvironment = MobMishapEnv(world, caster)

    override fun mishapSprayPos(): Vec3 = caster.position()

    /**
     * Pay media from amethyst (or any media-holding item) held in the mob's hands.
     * Returns the amount of media that could NOT be paid.
     */
    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long =
        caster.extractMediaFromHands(cost, simulate)

    /** A simple spherical ambit around the mob. */
    override fun isVecInRangeEnvironment(vec: Vec3?): Boolean {
        return vec != null && vec.distanceToSqr(caster.position()) <= castRange * castRange
    }

    /** Mobs are not gated by gamemode/adventure rules. */
    override fun hasEditPermissionsAtEnvironment(pos: BlockPos?): Boolean = true

    override fun getCastingHand(): InteractionHand = heldCastingHand

    /** Both hands are discoverable stacks (mobs have no inventory). */
    override fun getUsableStacks(mode: StackDiscoveryMode?): List<ItemStack?> {
        return listOf(
            caster.getItemInHand(InteractionHand.MAIN_HAND),
            caster.getItemInHand(InteractionHand.OFF_HAND),
        )
    }

    override fun getPrimaryStacks(): List<HeldItemInfo?> {
        return listOf(
            HeldItemInfo(caster.getItemInHand(heldCastingHand), heldCastingHand),
            HeldItemInfo(caster.getItemInHand(otherHand(heldCastingHand)), otherHand(heldCastingHand)),
        )
    }

    /** Replace the first matching hand-held stack. */
    override fun replaceItem(
        stackOk: Predicate<ItemStack?>?,
        replaceWith: ItemStack?,
        hand: InteractionHand?,
    ): Boolean {
        val hands: Array<InteractionHand> =
            if (hand != null) arrayOf(hand)
            else arrayOf(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)
        for (h in hands) {
            val stack = caster.getItemInHand(h)
            if (stackOk?.test(stack) == true) {
                caster.setItemInHand(h, replaceWith ?: ItemStack.EMPTY)
                return true
            }
        }
        return false
    }

    override fun getPigment(): FrozenPigment = FrozenPigment.DEFAULT.get()

    /** Mobs cannot carry a player "staff" pigment; nothing is stored. */
    override fun setPigment(pigment: FrozenPigment?): FrozenPigment? = null

    override fun produceParticles(particles: ParticleSpray?, colorizer: FrozenPigment?) {
        particles?.sprayParticles(world, colorizer ?: FrozenPigment.DEFAULT.get())
    }

    /** Mobs have no chat to print to; drop the message. */
    override fun printMessage(message: Component?) {
        /* no-op */
    }

    /**
     * Mishap handling for a mob: cosmetic drop/yeet of held items, and real damage.
     * Player-only mishaps (drown/xp/blind) are deliberately no-ops.
     */
    private class MobMishapEnv(
        world: ServerLevel,
        private val mob: LivingEntity,
    ) : MishapEnvironment(world, null) {

        override fun yeetHeldItemsTowards(targetPos: Vec3?) {
            val srcPos = mob.position()
            val dir = (targetPos?.subtract(srcPos)?.normalize() ?: Vec3(1.0, 0.0, 0.0))
            for (hand in arrayOf(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
                val stack = mob.getItemInHand(hand)
                if (!stack.isEmpty) {
                    yeetItem(stack.copy(), srcPos, dir.scale(0.4))
                }
            }
        }

        override fun dropHeldItems() {
            val srcPos = mob.position()
            for (hand in arrayOf(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
                val stack = mob.getItemInHand(hand)
                if (!stack.isEmpty) {
                    yeetItem(stack.copy(), srcPos, Vec3(0.0, 0.15, 0.0))
                    mob.setItemInHand(hand, ItemStack.EMPTY)
                }
            }
        }

        override fun drown() {
            /* mobs manage their own air; no-op */
        }

        override fun damage(healthProportion: Float) {
            mob.hurt(world.damageSources().magic(), healthProportion * mob.maxHealth)
        }

        override fun removeXp(amount: Int) {
            /* mobs have no experience; no-op */
        }

        override fun blind(ticks: Int) {
            /* mobs are not blinded by the player mechanic; no-op */
        }
    }
}
