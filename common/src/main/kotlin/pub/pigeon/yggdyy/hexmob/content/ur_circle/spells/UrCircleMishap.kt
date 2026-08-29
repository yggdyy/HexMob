package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.effect.MobEffectInstance
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * 丢事故工具：**新建一个 VM** 对目标玩家执行 HexCasting 真·事故（原版 Hex 事故，
 * 走 [OperatorSideEffect.DoMishap] 管线）——在玩家身上喷 红+主色 事故粒子、
 * 执行事故本身的效果、并打印红色事故文案，观感与 Hex 施法失败完全一致。
 *
 * 丢的全是 HexCasting 自带的原版事故（见 [UrCircleStatusTable.mishaps]）。
 *
 * 期间屏蔽反向过度施法钩子（[HexMobBacklash.suppress]），避免 VM 创建触发的
 * create 事件造成递归积累。失败时退回"直接上随机 debuff"，保证惩罚不落空。
 */
object UrCircleMishap {
    fun throwAt(player: ServerPlayer, mishap: Mishap) {
        if (player.level().isClientSide) return
        HexMobBacklash.suppress = true
        try {
            val env = StaffCastEnv(player, InteractionHand.MAIN_HAND)
            val ctx = Mishap.Context(null, null)
            CastingVM.empty(env).performSideEffects(listOf(OperatorSideEffect.DoMishap(mishap, ctx)))
            mishap.errorMessageWithName(env, ctx)?.let { env.printMessage(it) }
        } catch (e: Exception) {
            HexMob.LOGGER.error("[hexmob] 丢事故失败，退回直接 debuff", e)
            val debuff = UrCircleStatusTable.randomDebuff(player.level().random)
            player.addEffect(MobEffectInstance(debuff.effect, debuff.duration, debuff.amplifier))
        } finally {
            HexMobBacklash.suppress = false
        }
    }
}
