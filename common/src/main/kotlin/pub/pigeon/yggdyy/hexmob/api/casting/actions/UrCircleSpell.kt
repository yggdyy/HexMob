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
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleCoreItem

abstract class UrCircleSpell: SpellWithCondition() {
    override fun canUse(env: CastingEnvironment): Boolean {
        val caster = env.castingEntity ?: return false
        val hand = caster.getItemInHand(env.otherHand)
        return hand.item is UrCircleCoreItem
    }

    override fun makeMishap(args: List<Iota>, env: CastingEnvironment): Mishap {
        return NoUrCircleMishap()
    }

    /**
     * 解析并校验法术参数里的目标实体（hexcasting 惯例）：
     * 非活体参数（非实体/盔甲架/物品等）抛事故，超出施法范围抛 [MishapEntityTooFarAway]。
     * 供子类在 makeEffect 里解析第 0 个参数使用。
     */
    protected fun livingTarget(args: List<Iota>, env: CastingEnvironment): LivingEntity {
        val entity = args.getLivingEntityButNotArmorStand(0, argc)
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