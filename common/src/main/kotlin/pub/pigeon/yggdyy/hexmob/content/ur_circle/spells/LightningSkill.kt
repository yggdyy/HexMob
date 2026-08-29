package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.util.spawnParticle
import kotlin.math.cos
import kotlin.math.sin

/**
 * 【天罚】Lightning——对每一个仇恨目标召唤一道闪电（可多目标同时劈）。
 *
 * **可打断**（阈值 [INTERRUPT_THRESHOLD]=30）：吟唱期间只承 [LIGHTNING_DAMAGE_MULTIPLIER] 伤害。
 * 吟唱期间给所有目标挂发光 + 电火花前兆粒子。
 */
class LightningSkill : UrCircleSkill("lightning", 60) {

    override val weight: Int = 3
    override val channelInterruptible: Boolean = true
    override fun channelMarksTarget(circle: UrCircleEntity): Boolean = true

    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        return circle.currentHated().isNotEmpty() && circle.health/circle.maxHealth <= 0.5
    }

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.ELECTRIC_SPARK
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 16
    override fun channelPulseSound(circle: UrCircleEntity) = SoundEvents.LIGHTNING_BOLT_THUNDER
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 8

    // 吟唱期间：目标周围持续冒电火花（警告：要被劈了）
    override fun onChannelTick(circle: UrCircleEntity) {
        val level = circle.level()
        for (t in circle.currentHated().take(MAX_TARGETS)) {
            for (k in 0 until 3) {
                spawnParticle(
                    level, ParticleTypes.ELECTRIC_SPARK,
                    t.x + (circle.random.nextDouble() - 0.5) * 1.6,
                    t.y + circle.random.nextDouble() * t.bbHeight,
                    t.z + (circle.random.nextDouble() - 0.5) * 1.6,
                    (circle.random.nextDouble() - 0.5) * 0.2, 0.15, (circle.random.nextDouble() - 0.5) * 0.2
                )
            }
        }
    }

    // 受伤控制：减半 + 仅 30+ 打断
    override fun onChannelHurt(circle: UrCircleEntity, source: DamageSource, amount: Float): Boolean {
        val dmg = amount * LIGHTNING_DAMAGE_MULTIPLIER
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
        // 逐个仇恨目标劈下
        for (t in circle.currentHated().take(MAX_TARGETS)) {
            spawnBolt(level, t.x, t.y, t.z)
        }
        // 以自己为中心环状再劈一圈（数目增多），半径随体积放大
        val center = circle.position().add(0.0, 1.0, 0.0)
        val radius = RING_BOLT_RADIUS * circle.totalScale()
        for (k in 0 until RING_BOLTS) {
            val ang = Math.PI * 2.0 * k / RING_BOLTS
            spawnBolt(level, center.x + cos(ang) * radius, center.y, center.z + sin(ang) * radius)
        }
    }

    private fun spawnBolt(level: Level, x: Double, y: Double, z: Double) {
        val bolt = EntityType.LIGHTNING_BOLT.create(level) ?: return
        bolt.moveTo(x, y, z, 0.0F, 0.0F)
        level.addFreshEntity(bolt)
    }

    companion object {
        /** 最多同时劈几个仇恨目标。 */
        const val MAX_TARGETS = 12
        /** 以自己为中心环状劈的闪电数。 */
        const val RING_BOLTS = 10
        /** 环状闪电半径（格，随体积放大）。 */
        const val RING_BOLT_RADIUS = 8.0
        /** 打断阈值：单次原始伤害 ≥30 才打断。 */
        const val INTERRUPT_THRESHOLD = 30.0F
        /** 吟唱期间伤害减免倍率。 */
        const val LIGHTNING_DAMAGE_MULTIPLIER = 0.8F
    }
}
