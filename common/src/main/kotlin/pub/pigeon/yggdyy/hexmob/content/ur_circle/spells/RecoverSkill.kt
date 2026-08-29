package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.util.spawnParticle

/**
 * 【恢复自身状态】Recover——大环低血量时吟唱，回复自身生命（[HEAL_AMOUNT]）。
 *
 * 可打断技能：打断阈值 [INTERRUPT_THRESHOLD]（30），吟唱期间只承受 [RECOVER_DAMAGE_MULTIPLIER] 伤害。
 * 吟唱期间大环自发光 + 心形粒子示警。
 */
class RecoverSkill : UrCircleSkill("recover", 60) {
    override fun canUse(circle: UrCircleEntity): Boolean =
        !circle.level().isClientSide && circle.health / circle.maxHealth < 0.5F

    override val weight: Int = 3
    override val channelInterruptible: Boolean = true

    // 吟唱粒子：心形（"恢复"提示）
    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.HAPPY_VILLAGER
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 16
    override fun channelPulseSound(circle: UrCircleEntity) = SoundEvents.BEACON_ACTIVATE
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 10

    // 吟唱期间大环自身发光
    override fun onChannelTick(circle: UrCircleEntity) {
        circle.addEffect(MobEffectInstance(MobEffects.GLOWING, 12, 0, false, false))
    }

    // 受伤控制：减半 + 仅 30+ 打断
    override fun onChannelHurt(circle: UrCircleEntity, source: DamageSource, amount: Float): Boolean {
        val dmg = amount * RECOVER_DAMAGE_MULTIPLIER
        if (dmg > 0.0F) {
            circle.applyChannelDamage(source, dmg)
        }
        if (amount >= INTERRUPT_THRESHOLD) {
            circle.interruptChannel()
        }
        return true
    }

    override fun cast(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        circle.healSelf(HEAL_AMOUNT)
        // 治愈粒子 + 音效
        val origin = circle.position().add(0.0, circle.bbHeight / 2.0, 0.0)
        for (k in 0 until 28) {
            val dx = (circle.random.nextDouble() - 0.5) * 6.0
            val dy = (circle.random.nextDouble() - 0.5) * 6.0
            val dz = (circle.random.nextDouble() - 0.5) * 6.0
            spawnParticle(level, ParticleTypes.HAPPY_VILLAGER, origin.x + dx, origin.y + dy, origin.z + dz, 0.0, 0.35, 0.0)
        }
        level.playSound(null, circle.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.HOSTILE, 1.0F, 1.4F)
    }

    companion object {
        /** 回复量（点）。 */
        const val HEAL_AMOUNT = 200.0F
        /** 打断阈值：单次原始伤害 ≥30 才打断。 */
        const val INTERRUPT_THRESHOLD = 30.0F
        /** 吟唱期间伤害减免倍率。 */
        const val RECOVER_DAMAGE_MULTIPLIER = 0.5F
    }
}
