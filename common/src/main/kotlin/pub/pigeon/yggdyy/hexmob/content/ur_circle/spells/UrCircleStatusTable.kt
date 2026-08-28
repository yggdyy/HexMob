package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.casting.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidPattern
import net.minecraft.network.chat.Component
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffects
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity

/** 一条 debuff：效果 + 时长（tick）+ 等级 + 抽取权重。 */
data class UrCircleDebuff(
    val effect: MobEffect,
    val duration: Int,
    val amplifier: Int,
    val weight: Int,
)

/**
 * 大环的状态表（第 5 步）：负面效果池 + **原版 Hex 事故池**。
 * - [debuffs]：诅咒/咒祸技能与反噬报复随机抽取的 debuff 池；
 * - [mishaps]：事故投掷与反噬报复随机抽取的"丢事故"池——全部是 HexCasting
 *   自带原版事故（媒质不足/实体失效/太远/除零/括号过多），保证事故观感原生。
 */
object UrCircleStatusTable {

    // ---- debuff 池（权重越高越常抽到） ----
    val debuffs: List<UrCircleDebuff> = listOf(
        UrCircleDebuff(MobEffects.MOVEMENT_SLOWDOWN, 200, 2, 3), // 缓慢 III
        UrCircleDebuff(MobEffects.WEAKNESS, 300, 1, 3),           // 虚弱 II
        UrCircleDebuff(MobEffects.CONFUSION, 200, 1, 2),          // 反胃 II
        UrCircleDebuff(MobEffects.BLINDNESS, 200, 0, 2),          // 失明
        UrCircleDebuff(MobEffects.POISON, 200, 1, 2),             // 中毒 II
        UrCircleDebuff(MobEffects.WITHER, 160, 1, 2),             // 凋零 II
        UrCircleDebuff(MobEffects.DIG_SLOWDOWN, 300, 1, 2),       // 挖掘疲劳 II
        UrCircleDebuff(MobEffects.HUNGER, 200, 1, 2),             // 饥饿 II
        UrCircleDebuff(MobEffects.GLOWING, 300, 0, 1),            // 发光（标记猎物）
    )

    // ---- 原版 Hex 事故池（丢事故）：每条工厂返回新事故实例 ----
    val mishaps: List<(UrCircleEntity) -> Mishap> = listOf(
        // 媒质不足：抽走施法者身上 500 媒质，红色"你不能过度施法"——最贴反向过度施法
        { MishapNotEnoughMedia(500L) },
        // 实体免疫：把施法者手持物甩向大环
        { circle -> MishapImmuneEntity(circle) },
        // 目标太远：把施法者手持物甩向大环
        { circle -> MishapEntityTooFarAway(circle) },
        // 除零：纯红色报错文案
        { MishapDivideByZero(Component.literal("1"), Component.literal("0")) },
        // 格式错误（占位事故：0.11.3 的 MishapTooManyCloseParens 在 0.11.4 改名 MishapNeedsParens，
        // 两边版本各缺一个类名 → 用两版都有的 MishapInvalidPattern 无参版代替）：纯红色报错文案
        { MishapInvalidPattern() },
    )

    fun randomDebuff(random: RandomSource): UrCircleDebuff = weightedRandom(debuffs, random)

    fun randomMishap(circle: UrCircleEntity, random: RandomSource): Mishap =
        mishaps[random.nextInt(mishaps.size)].invoke(circle)

    private fun weightedRandom(entries: List<UrCircleDebuff>, random: RandomSource): UrCircleDebuff {
        val total = entries.sumOf { it.weight }
        var r = random.nextInt(total)
        for (e in entries) {
            r -= e.weight
            if (r < 0) return e
        }
        return entries.last()
    }
}
