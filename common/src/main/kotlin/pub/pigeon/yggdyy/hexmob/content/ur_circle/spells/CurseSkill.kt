package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.phys.AABB
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.util.spawnParticle

/**
 * 【咒祸】Curse——从状态表抽一个 debuff 施加给目标及其周围（小范围 AoE）。
 * 血量 >40% 时可用（战斗前半段施压；进入危急区后让位给事故与光炮）。
 */
class CurseSkill : UrCircleSkill("curse", 60) {

    override val weight: Int = 3

    /** 不可打断：吟唱不中断（受击只掉血）。 */
    override fun channelHurtInterrupts(circle: UrCircleEntity): Boolean = false

    /** 吟唱标记目标发光。 */
    override fun channelMarksTarget(circle: UrCircleEntity): Boolean = true

    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        val t = circle.target
        return t != null && t.isAlive && circle.health / circle.maxHealth > 0.4F
    }

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.WITCH
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 16
    override fun channelPulseSound(circle: UrCircleEntity) = HexSounds.CAST_NORMAL
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 10

    // 吟唱期间：在目标周围持续生成咒祸粒子（WITCH 星火）
    override fun onChannelTick(circle: UrCircleEntity) {
        val t = circle.target
        if (t == null || !t.isAlive) return
        for (k in 0 until 3) {
            spawnParticle(
                circle.level(), ParticleTypes.WITCH,
                t.x + (circle.random.nextDouble() - 0.5) * 1.6,
                t.y + circle.random.nextDouble() * t.bbHeight,
                t.z + (circle.random.nextDouble() - 0.5) * 1.6,
                (circle.random.nextDouble() - 0.5) * 0.1, 0.12, (circle.random.nextDouble() - 0.5) * 0.1
            )
        }
    }

    override fun cast(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        val t = circle.target ?: return
        if (!t.isAlive) return
        val debuff = UrCircleStatusTable.randomDebuff(level.random)
        // 目标 + 周围 3 格 AoE 一起吃咒
        for (victim in level.getEntitiesOfClass(LivingEntity::class.java, AABB(t.blockPosition()).inflate(3.0))) {
            if (victim is Enemy) continue
            victim.addEffect(MobEffectInstance(debuff.effect, debuff.duration, debuff.amplifier))
        }
        // 目标头顶炸开一团诅咒星火
        val top = t.position().add(0.0, t.bbHeight.toDouble(), 0.0)
        for (k in 0 until 28) {
            spawnParticle(
                level, ParticleTypes.WITCH,
                top.x, top.y, top.z,
                (circle.random.nextDouble() - 0.5) * 0.2,
                circle.random.nextDouble() * 0.25,
                (circle.random.nextDouble() - 0.5) * 0.2
            )
        }
    }
}
