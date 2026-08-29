package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.util.spawnParticle

/**
 * 【核心光线】光炮（Core Beam）——必定命中的激光束。
 * 吟唱 60 tick，有存活目标即可用；释放后进入 BEAM 状态约 24 tick，
 * 从核心到目标铺满 ELECTRIC_SPARK 电火花（信标光束式），每 4 tick 施加一次伤害（直击目标，必定命中）。
 *
 * 防御特性：
 * - 比例减伤：吟唱+光束期间只承受 [BEAM_DAMAGE_MULTIPLIER]（默认 0.3）的伤害；
 * - 只有单次原始伤害 ≥ [INTERRUPT_THRESHOLD]（默认 15）才会打断施放。
 */
class BeamSkill : UrCircleSkill("core_beam", 80) {
    override val weight: Int = 3
    override val channelInterruptible: Boolean = true
    override fun channelMarksTarget(circle: UrCircleEntity): Boolean = true

    override fun canUse(circle: UrCircleEntity): Boolean {
        val t = circle.target
        return t != null && t.isAlive &&circle.health/circle.maxHealth <= 0.8
    }

    override fun releaseSound(circle: UrCircleEntity): SoundEvent {
        return SoundEvents.END_PORTAL_SPAWN
    }

    // 吟唱粒子：电火花 + 符文
    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.ENCHANT
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 24

    // 吟唱期间大环自身发光
    override fun onChannelTick(circle: UrCircleEntity) {
        circle.addEffect(MobEffectInstance(MobEffects.GLOWING, 12, 0, false, false))
    }

    // 受伤控制：比例减伤 + 仅 15+ 打断
    override fun onChannelHurt(circle: UrCircleEntity, source: DamageSource, amount: Float): Boolean {
        val dmg = amount * BEAM_DAMAGE_MULTIPLIER
        if (dmg > 0.0F) {
            circle.applyChannelDamage(source, dmg)
        }
        if (amount >= INTERRUPT_THRESHOLD) {
            circle.interruptChannel()
        }
        return true
    }

    // 释放：进入 BEAM 状态（光束由状态机驱动）
    override fun beamTicks(circle: UrCircleEntity): Int = BEAM_DURATION

    override fun cast(circle: UrCircleEntity) {
        circle.beginBeam(this)
    }

    override fun onBeamTick(circle: UrCircleEntity) {
        val level = circle.level()
        val origin = circle.beamOrigin()
        val t = circle.target
        val rawEnd = if (t != null && t.isAlive) {
            t.position().add(0.0, t.bbHeight / 2.0, 0.0)
        } else {
            origin.add(circle.lookAngle.scale(16.0))
        }
        // 范围更大：光束末端再延伸 BEAM_EXTEND 格
        val delta = rawEnd.subtract(origin)
        val len = delta.length()
        val end = if (len > 0.01) rawEnd.add(delta.normalize().scale(BEAM_EXTEND)) else rawEnd
        // 沿 核心→末端 的连线铺粒子（信标光束式，ENCHANT 符文 + ELECTRIC_SPARK 电火花）
        val full = end.subtract(origin)
        val fullLen = full.length()
        if (fullLen > 0.01) {
            val dir = full.scale(1.0 / fullLen)
            val steps = (fullLen * 2).toInt().coerceIn(6, 48)
            for (i in 0 until steps) {
                val along = origin.add(dir.scale(fullLen * i / steps))
                val jx = (circle.random.nextDouble() - 0.5) * 0.5
                val jy = (circle.random.nextDouble() - 0.5) * 0.5
                val jz = (circle.random.nextDouble() - 0.5) * 0.5
                spawnParticle(level, ParticleTypes.ENCHANT, along.x + jx, along.y + jy, along.z + jz, 0.0, 0.0, 0.0)
                spawnParticle(level, ParticleTypes.ENCHANT, along.x + jx, along.y + jy, along.z + jz, 0.3, 0.0, 0.3)
                spawnParticle(level, ParticleTypes.ELECTRIC_SPARK, along.x + jx, along.y + jy, along.z + jz, 0.0, 0.0, 0.0)
            }
        }
        // 周期伤害（直击目标，必定命中）+ 命中点范围伤害
        if (circle.stateTicks % BEAM_DAMAGE_INTERVAL == 0 && t != null && t.isAlive) {
            t.hurt(level.damageSources().mobAttack(circle), beamDamageFor(t))
            val aoe = AABB(rawEnd, rawEnd).inflate(BEAM_AOE_RADIUS)
            for (other in level.getEntitiesOfClass(LivingEntity::class.java, aoe)) {
                if (other === t || other is Enemy) continue
                other.hurt(level.damageSources().mobAttack(circle), beamDamageFor(other) * 0.5F)
            }
            spawnParticle(level, ParticleTypes.ELECTRIC_SPARK, rawEnd.x, rawEnd.y, rawEnd.z, 0.0, 0.2, 0.0)
            spawnParticle(level, ParticleTypes.ENCHANT, rawEnd.x, rawEnd.y, rawEnd.z, 0.0, 0.2, 0.0)
        }
    }

    /** 光炮单次伤害：目标最大生命 > [PERCENT_HEALTH_THRESHOLD] 时按 [BEAM_PERCENT_DAMAGE]（20%）扣血，否则固定 [BEAM_DAMAGE]。 */
    private fun beamDamageFor(victim: LivingEntity): Float =
        if (victim.getMaxHealth() > PERCENT_HEALTH_THRESHOLD) victim.getMaxHealth() * BEAM_PERCENT_DAMAGE else BEAM_DAMAGE

    companion object {
        /** 光束时长（tick）。 */
        const val BEAM_DURATION = 50
        /** 每次伤害（点/周期）。 */
        const val BEAM_DAMAGE = 8.0F
        /** 最大生命超过此值的实体，光炮改按百分比扣血。 */
        const val PERCENT_HEALTH_THRESHOLD = 100.0F
        /** 光炮对高血量实体（最大生命 >100）的每次伤害：其最大生命的 20%。 */
        const val BEAM_PERCENT_DAMAGE = 0.2F
        /** 伤害周期（tick）。 */
        const val BEAM_DAMAGE_INTERVAL = 2
        /** 命中点范围伤害半径。 */
        const val BEAM_AOE_RADIUS = 2.5
        /** 光束末端超出目标距离（格）。 */
        const val BEAM_EXTEND = 4.0
        /** 比例减伤：吟唱+光束期间只承受 30% 伤害。 */
        const val BEAM_DAMAGE_MULTIPLIER = 0.3F
        /** 打断阈值：单次原始伤害 ≥15 才打断。 */
        const val INTERRUPT_THRESHOLD = 15.0F
    }
}
