package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity

/**
 * 大环的蓄力技能抽象——大招的统一入口（核心光线、促动石长蛇等后续技能都继承它）。
 *
 * 由大环的 CHANNELING 状态机驱动：
 * - 满足 [canUse] 时进入吟唱，期间大环**不移动、旋转逐渐停止**；
 * - 吟唱期间按 [channelPulseInterval] 周期性播放 [channelPulseSound] 并聚集
 *   [channelParticleType] 粒子（越接近完成越密集）；
 * - 吟唱 [channelTicks] 后**完全停下**，播放 [releaseSound] 并执行 [cast]。
 *
 * 每个技能只需定义：吟唱时长、使用条件、释放效果；
 * 粒子和音效都提供了**默认实现**，可按需覆盖（核心光线想要光束粒子、长蛇想要
 * 自己的声音时，覆写对应 open 方法即可）。
 */
abstract class UrCircleSkill(val name: String, val channelTicks: Int) {
    /** 使用条件：满足才进入吟唱（通常要求 circle.target 存活等）。 */
    abstract fun canUse(circle: UrCircleEntity): Boolean

    /** 效果：吟唱完成、完全停下的瞬间执行。 */
    abstract fun cast(circle: UrCircleEntity)

    /**
     * 技能权重：大环挑选技能时在"所有满足 canUse 的技能"里按权重随机抽（默认 1）。
     * 数值越高被抽中的概率越大。
     */
    open val weight: Int = 1

    /**
     * 是否"可打断"技能：可打断技能吟唱时，赤道面/黄道面各自绕自己半径轴旋转的动画，
     * 且受击超过各自打断阈值（见 onChannelHurt 实现）会中断吟唱。
     * 不可打断技能：吟唱不被打断（受击只掉血）。
     */
    open val channelInterruptible: Boolean = false

    /**
     * 吟唱期间是否给目标挂发光（GLOWING）：用于标记"即将被打"的目标。
     * 需要在吟唱分支每 tick 给 circle.target 施加发光（见 UrCircleEntity.combatBrain）。
     */
    open fun channelMarksTarget(circle: UrCircleEntity): Boolean = false

    // ---- 粒子（可调，默认：亮色星光向圆心汇聚） ----

    /** 蓄力粒子类型（默认 WITCH——女巫施法的亮绿色星光，非常醒目；可按技能换 END_ROD 等）。 */
    open fun channelParticleType(circle: UrCircleEntity): ParticleOptions = ParticleTypes.WITCH

    /** 蓄力粒子基础数量（默认 20，随吟唱进度增强）。 */
    open fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 20

    /** 粒子生成位置（默认：以圆心为中心、半径 4~8 格的球壳上随机一点——覆盖整个大环）。 */
    open fun channelParticlePos(circle: UrCircleEntity): Vec3 {
        val origin = circle.position().add(0.0, circle.bbHeight / 2.0, 0.0)
        val r = 4.0 + circle.random.nextDouble() * 4.0
        val theta = circle.random.nextDouble() * Math.PI * 2.0
        val phi = Math.acos(2.0 * circle.random.nextDouble() - 1.0)
        return origin.add(
            r * Math.sin(phi) * Math.cos(theta),
            r * Math.sin(phi) * Math.sin(theta),
            r * Math.cos(phi)
        )
    }

    /** 粒子运动速度（默认：朝圆心靠拢，形成"向中心汇聚"的形态）。 */
    open fun channelParticleVelocity(circle: UrCircleEntity, pos: Vec3): Vec3 {
        val origin = circle.position().add(0.0, circle.bbHeight / 2.0, 0.0)
        val dir = origin.subtract(pos)
        if (dir.lengthSqr() < 1.0E-4) return Vec3.ZERO
        return dir.normalize().scale(0.12 + circle.random.nextDouble() * 0.08)
    }

    // ---- 音效（可调，默认 开始/脉冲=CAST_NORMAL，释放=CAST_THOTH） ----

    /** 吟唱开始音效（默认 CAST_NORMAL）。 */
    open fun channelStartSound(circle: UrCircleEntity): SoundEvent = HexSounds.CAST_NORMAL
    open fun channelStartVolume(circle: UrCircleEntity): Float = 1.5F

    /** 蓄力脉冲音效（默认 CAST_NORMAL）。 */
    open fun channelPulseSound(circle: UrCircleEntity): SoundEvent = HexSounds.CAST_NORMAL
    open fun channelPulseVolume(circle: UrCircleEntity): Float = 1.2F

    /** 蓄力脉冲间隔（tick，默认 12）。 */
    open fun channelPulseInterval(circle: UrCircleEntity): Int = 12

    /** 释放音效（默认 CAST_THOTH，音量大）。 */
    open fun releaseSound(circle: UrCircleEntity): SoundEvent = HexSounds.CAST_THOTH
    open fun releaseVolume(circle: UrCircleEntity): Float = 2.0F

    // ---- 吟唱受伤控制 ----

    /**
     * 吟唱期间受伤的控制入口（大环 hurt() 在 CHANNELING 状态会委托到这里）。
     * 返回是否"承认"这次伤害（决定攻击者是否获得打击反馈/击退）。
     *
     * 默认：按 [channelDamageMultiplier] 减免后施加伤害，并按 [channelHurtInterrupts] 决定是否打断吟唱。
     * 覆写可自定义：免疫、转移、反弹、只加仇恨不受伤等。
     * 注意：施加伤害请用 circle.applyChannelDamage(...)（直接走父类 hurt，避免死循环）。
     */
    open fun onChannelHurt(circle: UrCircleEntity, source: DamageSource, amount: Float): Boolean {
        val dmg = amount * channelDamageMultiplier(circle)
        var handled = true
        if (dmg > 0.0F) {
            handled = circle.applyChannelDamage(source, dmg)
        }
        if (handled && channelHurtInterrupts(circle)) circle.interruptChannel()
        return handled
    }

    /** 吟唱期间的伤害减免倍率（默认 1.0 = 无减免；0 = 吟唱期间完全免疫）。 */
    open fun channelDamageMultiplier(circle: UrCircleEntity): Float = 1.0F

    /** 吟唱受伤是否打断吟唱（默认 true）。 */
    open fun channelHurtInterrupts(circle: UrCircleEntity): Boolean = true

    /** 吟唱期间每 tick 调用（服务端）：可用于给大环挂发光、附加音效等持续效果。 */
    open fun onChannelTick(circle: UrCircleEntity) {}

    // ---- 光束持续状态（cast 后进入 BEAM） ----

    /**
     * 光束持续时间（tick）。>0 时 cast() 结束后大环进入 BEAM 状态，
     * 期间每 tick 调 [onBeamTick]，持续 [beamTicks] 后调 [onBeamEnd] 回巡航。
     * 默认 0 = cast 一次性，不进入 BEAM。
     */
    open fun beamTicks(circle: UrCircleEntity): Int = 0

    /** 光束持续期间每 tick 调用（服务端，粒子用 util.spawnParticle 广播）。 */
    open fun onBeamTick(circle: UrCircleEntity) {}

    /** 光束结束。 */
    open fun onBeamEnd(circle: UrCircleEntity) {}
}
