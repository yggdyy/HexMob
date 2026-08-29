package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.util.spawnParticle

/**
 * 【事故投掷】Mishap——吟唱后朝目标"丢"一个真·Hex 事故（新建 VM 触发，
 * 红色文案 + 红/主色事故粒子 + 惩罚效果）。
 * 血量 ≤75% 时可用（大环被重创后开始玩事故，配合反向过度施法）。
 * 目标非玩家时退回"直接上随机 debuff"。
 */
class MishapSkill : UrCircleSkill("mishap", 80) {

    override val weight: Int = 3

    /** 不可打断：吟唱不中断（受击只掉血）。 */
    override fun channelHurtInterrupts(circle: UrCircleEntity): Boolean = false

    /** 吟唱标记目标发光。 */
    override fun channelMarksTarget(circle: UrCircleEntity): Boolean = true

    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        val t = circle.target
        return t != null && t.isAlive && circle.health / circle.maxHealth <= 0.75F
    }

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.PORTAL
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 18
    override fun channelPulseSound(circle: UrCircleEntity) = HexSounds.CAST_FAILURE
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 12

    // 吟唱期间：在目标周围持续生成事故前兆粒子（PORTAL 传送紫烟）
    override fun onChannelTick(circle: UrCircleEntity) {
        val t = circle.target
        if (t == null || !t.isAlive) return
        for (k in 0 until 3) {
            spawnParticle(
                circle.level(), ParticleTypes.PORTAL,
                t.x + (circle.random.nextDouble() - 0.5) * 2.0,
                t.y + circle.random.nextDouble() * t.bbHeight,
                t.z + (circle.random.nextDouble() - 0.5) * 2.0,
                (circle.random.nextDouble() - 0.5) * 0.15, 0.1, (circle.random.nextDouble() - 0.5) * 0.15
            )
        }
    }

    override fun cast(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        val t = circle.target ?: return
        if (!t.isAlive) return
        val mishap = UrCircleStatusTable.randomMishap(circle, level.random)
        if (t is ServerPlayer) {
            UrCircleMishap.throwAt(t, mishap)
        } else {
            // 非玩家目标：直接把随机 debuff 压上去
            val debuff = UrCircleStatusTable.randomDebuff(level.random)
            t.addEffect(MobEffectInstance(debuff.effect, debuff.duration, debuff.amplifier))
        }
    }
}
