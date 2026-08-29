package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.Level
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity

/**
 * 【自爆】Explosion——有仇恨目标贴身（≤[CLOSE_RANGE] 格）时，以自身为中心引发爆炸。
 * 大环免疫自身爆炸伤害（hurt() 里 `source.entity === this` 直接免疫）。
 */
class ExplosionSkill : UrCircleSkill("explosion", 40) {

    override val weight: Int = 2

    /** 不可打断：吟唱期间受击只掉血，不中断（避免被近身压制时放不出来）。 */
    override fun channelHurtInterrupts(circle: UrCircleEntity): Boolean = false

    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        return circle.currentHated().any { circle.distanceToSqr(it) < CLOSE_RANGE * CLOSE_RANGE }
    }

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.SMOKE
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 16
    override fun channelPulseSound(circle: UrCircleEntity) = HexSounds.CAST_FAILURE
    override fun channelPulseInterval(circle: UrCircleEntity): Int = 8

    override fun cast(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        val pos = circle.position().add(0.0, circle.bbHeight / 2.0, 0.0)
        level.explode(circle, pos.x, pos.y, pos.z, EXPLOSION_POWER, Level.ExplosionInteraction.MOB)
    }

    companion object {
        /** 触发近身距离（格）：任一仇恨目标进入此距离即自爆。 */
        const val CLOSE_RANGE = 8.0
        /** 爆炸威力。 */
        const val EXPLOSION_POWER = 7.5F
    }
}
