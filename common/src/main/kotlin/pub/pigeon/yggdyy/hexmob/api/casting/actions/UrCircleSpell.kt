package pub.pigeon.yggdyy.hexmob.api.casting.actions

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import pub.pigeon.yggdyy.hexmob.content.now.EverythingInNowItem
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleCoreItem

abstract class UrCircleSpell: SpellWithCondition() {
    override fun canUse(env: CastingEnvironment): Boolean {
        val caster = env.castingEntity ?: return false
        val hand = caster.getItemInHand(env.otherHand)
        // 副手持大环核心 或 淬灵媒质立方，均可使用大环法术
        return hand.item is UrCircleCoreItem || hand.item is EverythingInNowItem
    }

    override fun makeMishap(args: List<Iota>, env: CastingEnvironment): Mishap {
        return NoUrCircleMishap()
    }

    /**
     * 解析并校验法术参数里的目标实体（hexcasting 惯例，实体在第 0 个参数）：
     * 非活体参数（非实体/盔甲架/物品等）抛事故，超出施法范围抛 [MishapEntityTooFarAway]。
     * 供子类在 makeEffect 里解析第 0 个参数使用。
     */
    protected fun livingTarget(args: List<Iota>, env: CastingEnvironment): LivingEntity =
        livingTarget(args, env, 0)

    /** 同上，但实体位于指定下标（三参数法术如 ur_beam 的 entity 在下标 1）。 */
    protected fun livingTarget(args: List<Iota>, env: CastingEnvironment, index: Int): LivingEntity {
        val entity = args.getLivingEntityButNotArmorStand(index, argc)
        if (!env.isEntityInRange(entity)) throw MishapEntityTooFarAway(entity)
        return entity
    }

    class NoUrCircleMishap : Mishap() {
        override fun accentColor(
            ctx: CastingEnvironment,
            errorCtx: Context
        ): FrozenPigment = dyeColor(DyeColor.LIGHT_BLUE)

        override fun errorMessage(
            ctx: CastingEnvironment,
            errorCtx: Context
        ): Component? {
            return Component.translatable("mishap.hexmob.circle")
        }

        override fun execute(
            env: CastingEnvironment,
            errorCtx: Context,
            stack: MutableList<Iota>
        ) {

        }

    }
}