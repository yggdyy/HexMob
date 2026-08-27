package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.util.spawnParticle
import java.util.UUID

/**
 * 【环刃风暴】Ring Spin——石板与促动石体积膨胀、环半径扩展、转速飙升，
 * 像绞肉机一样对所有碰到的实体造成大量伤害并击退。
 *
 * 不可打断（近身压制也放得出来）。膨胀/放大由 RING_SPINNING 同步标志 + stateTicks
 * 在两端确定性推导（见 UrCircleEntity.ringSpinScale），渲染端与命中结算用同一套倍率。
 */
class RingSpinSkill : UrCircleSkill("ring_spin", CHANNEL_TICKS) {

    override val weight: Int = 4

    /** 不可打断：吟唱不中断（受击只掉血）。 */
    override fun channelHurtInterrupts(circle: UrCircleEntity): Boolean = true

    override fun channelPulseVolume(circle: UrCircleEntity): Float = 35F
    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        val t = circle.target
        return t != null && t.isAlive && circle.health/circle.maxHealth > 0.5
    }

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.END_ROD
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 18
    override fun channelPulseSound(circle: UrCircleEntity) = HexSounds.CAST_NORMAL
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 8

    /** 每个受害者被命中后的冷却（tick）：不会每 tick 连打，但贴着环就持续掉血。 */
    private val hitCooldowns: MutableMap<UUID, Int> = HashMap()

    override fun onChannelTick(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        // 冷却衰减
        val it = hitCooldowns.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val next = e.value - 1
            if (next <= 0) it.remove() else e.setValue(next)
        }
        val s = circle.totalScale()
        val hitR = HIT_RADIUS * s // 命中判定半径跟随膨胀
        val origin = circle.position().add(0.0, circle.bbHeight / 2.0, 0.0)
        for (part in circle.getAllParts()) {
            val p = part.posNow
            if (p == Vec3.ZERO) continue // 首 tick 部件还没就位
            val box = AABB(p, p).inflate(hitR)
            for (victim in level.getEntitiesOfClass(LivingEntity::class.java, box)) {
                if (victim === circle) continue // 只排除自身：伤几乎所有实体（含敌对怪、大环下属）
                if (hitCooldowns.containsKey(victim.uuid)) continue
                if (victim.hurt(level.damageSources().mobAttack(circle), SPIN_DAMAGE)) {
                    val kb = victim.position().subtract(origin)
                    victim.knockback(KNOCKBACK.toDouble(), kb.x, kb.z)
                    hitCooldowns[victim.uuid] = HIT_COOLDOWN
                }
            }
        }
        // 吸引：把几乎所有实体往核心拉（"变大技能同时吸引附近的生物"），半径随膨胀放大，越近吸力越强；
        // 不排除敌对生物（含大环自己的下属），只有大环自身不被拉
        val pullR = PULL_RADIUS * s
        for (victim in level.getEntitiesOfClass(LivingEntity::class.java, AABB(origin, origin).inflate(pullR))) {
            if (victim === circle) continue
            val toCenter = origin.subtract(victim.position())
            val dist = toCenter.length()
            if (dist < 1.5) continue // 已贴脸，交给伤害判定
            val dir = toCenter.normalize()
            val strength = PULL_ACCEL * (1.5 - dist / pullR) // 边缘 0.5× → 中心 1.5×
            victim.setDeltaMovement(
                victim.deltaMovement.scale(PULL_DAMP)
                    .add(dir.x * strength, dir.y * strength * 0.35, dir.z * strength)
            )
        }
        // 旋转碎屑：随机部件位置炸石板块粒子（视觉：环在转 + 掉渣）
        if (circle.tickCount % 3 == 0) {
            val part = circle.getAllParts().random()
            val p = part.posNow
            if (p != Vec3.ZERO) {
                spawnParticle(level, SLATE_PART, p.x, p.y, p.z, 0.0, 0.15, 0.0)
            }
        }
    }

    override fun cast(circle: UrCircleEntity) {
        // 伤害全部在吟唱期间（onChannelTick）结算，释放瞬间无需额外效果
    }

    override fun channelDamageMultiplier(circle: UrCircleEntity): Float = 0.9F

    companion object {
        /** 吟唱时长（tick），UrCircleEntity.ringSpinProgress() 引用同值。 */
        const val CHANNEL_TICKS = 80
        /** 每次命中的伤害。 */
        const val SPIN_DAMAGE = 14.0F
        /** 命中冷却（tick）。 */
        const val HIT_COOLDOWN = 1
        /** 命中判定半径（格，未膨胀时）。 */
        const val HIT_RADIUS = 1.1
        /** 击退强度。 */
        const val KNOCKBACK = 5
        /** 吸引半径（格，未膨胀时）。 */
        const val PULL_RADIUS = 14.0
        /** 吸引加速度（速度/tick，越近越大）。 */
        const val PULL_ACCEL = 0.08
        /** 吸引时保留原速度的比例。 */
        const val PULL_DAMP = 0.95
        /** 石板块粒子（旋转碎屑）。 */
        val SLATE_PART = BlockParticleOption(ParticleTypes.BLOCK, HexBlocks.SLATE.defaultBlockState())
    }
}
