package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDoubleBetween
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import pub.pigeon.yggdyy.hexmob.api.casting.actions.UrCircleSpell

/**
 * 【核心光线】对目标打出一条持续光束（施法者→目标，跟随目标移动）：
 * 持续时间由第 1 个参数（数字 iota，tick）决定，**媒质消耗按持续时间累计**（超出
 * [MIN_TICKS]~[MAX_TICKS] 范围直接抛事故）。
 * 光束每 [BeamTicker.DAMAGE_INTERVAL] tick 对目标直击（必中）并对其周围 AoE 半伤
 * （照大环核心光线：高血量实体按 20% 最大生命扣血）。
 */
class BeamSpell : UrCircleSpell() {
    override val argc: Int get() = 2

    private fun clampDuration(args: List<Iota>): Int =
        args.getDoubleBetween(1, MIN_TICKS.toDouble(), MAX_TICKS.toDouble(), argc).toInt()

    override fun cost(args: List<Iota>, env: CastingEnvironment): Long =
        COST_BASE + clampDuration(args) * COST_PER_TICK

    override fun makeEffect(args: List<Iota>, env: CastingEnvironment): RenderedSpell =
        Spell(livingTarget(args, env), clampDuration(args))

    class Spell(private val target: LivingEntity, private val durationTicks: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.castingEntity ?: return
            val level = env.world as? ServerLevel ?: return
            if (level.isClientSide) return
            BeamTicker.launch(level, caster, target, durationTicks)
        }
    }

    companion object {
        /** 持续时间下限（tick）。 */
        const val MIN_TICKS = 5
        /** 持续时间上限（tick）。 */
        const val MAX_TICKS = 200
        /** 基础消耗（媒质）。 */
        const val COST_BASE = 10L
        /** 每持续 1 tick 的消耗（媒质）。 */
        const val COST_PER_TICK = MediaConstants.SHARD_UNIT
    }
}