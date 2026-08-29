package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleTypes
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent.UrCircleSerpent
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities
import pub.pigeon.yggdyy.hexmob.util.spawnParticle

/**
 * 【冲天长蛇】Erupting Serpent——在目标脚底生成促动石长蛇，从下到上贯穿造成伤害。
 * 释放时在目标脚下放出 [ERUPT_COUNT] 条竖直上升的长蛇（实体碰撞、不必中——目标跑开就落空）。
 * 不可打断；吟唱期间目标脚底冒"即将上涌"的预兆粒子。
 */
class EruptSerpentSkill : UrCircleSkill("erupt_serpent", 60) {

    override val weight: Int = 3

    /** 不可打断：吟唱不中断。 */
    override fun channelHurtInterrupts(circle: UrCircleEntity): Boolean = false

    /** 吟唱标记目标发光。 */
    override fun channelMarksTarget(circle: UrCircleEntity): Boolean = true

    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        val t = circle.target
        return t != null && t.isAlive && circle.health / circle.maxHealth > 0.4F
    }

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.END_ROD
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 14
    override fun channelPulseSound(circle: UrCircleEntity) = HexSounds.CAST_NORMAL
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 10

    // 吟唱期间：目标脚底持续冒"上涌"的预兆粒子
    override fun onChannelTick(circle: UrCircleEntity) {
        val t = circle.target ?: return
        if (!t.isAlive) return
        for (k in 0 until 3) {
            spawnParticle(
                circle.level(), ParticleTypes.END_ROD,
                t.x + (circle.random.nextDouble() - 0.5) * 1.4,
                t.y - 0.4,
                t.z + (circle.random.nextDouble() - 0.5) * 1.4,
                (circle.random.nextDouble() - 0.5) * 0.1, 0.35, (circle.random.nextDouble() - 0.5) * 0.1
            )
        }
    }

    override fun cast(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        val t = circle.target ?: return
        if (!t.isAlive) return
        for (i in 0 until ERUPT_COUNT) {
            val serpent = UrCircleSerpent(HexMobEntities.UR_CIRCLE_SERPENT.get(), level)
            serpent.owner = circle
            // 错位小幅散布，贴目标脚下（从地下冒出）
            val ox = (circle.random.nextDouble() - 0.5) * 1.6
            val oz = (circle.random.nextDouble() - 0.5) * 1.6
            serpent.setPos(t.x + ox, t.y - 1.0, t.z + oz)
            serpent.setAim(0.0, 1.0, 0.0) // 竖直向上：从脚下贯到头顶
            level.addFreshEntity(serpent)
        }
    }

    companion object {
        /** 一次放出几条冲天长蛇。 */
        const val ERUPT_COUNT = 10
    }
}
