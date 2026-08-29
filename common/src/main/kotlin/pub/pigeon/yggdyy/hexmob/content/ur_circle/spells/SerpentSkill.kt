package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.common.lib.HexBlocks
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent.UrCircleSerpent
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities
import kotlin.math.cos
import kotlin.math.sin

/**
 * 【促动石长蛇】Serpent——独立的临时蛇实体，朝目标方向蛇形游出。
 * 吟唱 80 tick，有存活目标即可用；释放时在核心生成一条由石板构成的长蛇，
 * 蛇身每节都是石板块（BLOCK 粒子 + 方块渲染），命中目标前是实体碰撞（不锁定，**不必中**）。
 *
 * 防御特性：**不能打断**——吟唱/期间受伤不打断施放（也无减伤）。
 */
class SerpentSkill : UrCircleSkill("serpent", 80) {
    override val weight: Int = 3
    override fun channelMarksTarget(circle: UrCircleEntity): Boolean = true

    override fun canUse(circle: UrCircleEntity): Boolean {
        val t = circle.target
        return t != null && t.isAlive && circle.health/circle.maxHealth > 0.5
    }

    // 吟唱粒子：石板块 BLOCK 粒子
    override fun channelParticleType(circle: UrCircleEntity) =
        BlockParticleOption(ParticleTypes.BLOCK, HexBlocks.SLATE.defaultBlockState())
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 16

    // 不能打断：受击不打断施放
    override fun onChannelHurt(circle: UrCircleEntity, source: DamageSource, amount: Float): Boolean {
        if (amount > 0.0F) {
            circle.applyChannelDamage(source, amount)
        }
        return true
    }

    // 释放：一次放出多条长蛇（扇形散布）
    override fun cast(circle: UrCircleEntity) {
        val level = circle.level()
        if (level.isClientSide) return
        val origin = circle.position().add(0.0, circle.bbHeight / 2.0, 0.0)
        val t = circle.target
        val base = if (t != null && t.isAlive) {
            t.position().add(0.0, t.bbHeight / 2.0, 0.0).subtract(origin).normalize()
        } else {
            Vec3(0.0, 0.0, 1.0)
        }
        for (i in 0 until SERPENT_COUNT) {
            val serpent = UrCircleSerpent(HexMobEntities.UR_CIRCLE_SERPENT.get(), level)
            serpent.owner = circle
            val ang = circle.random.nextDouble() * Math.PI * 2.0
            serpent.setPos(origin.x + cos(ang) * 0.6, origin.y, origin.z + sin(ang) * 0.6)
            // 扇形散布（绕 Y 轴偏转），每条朝向略有差异 → 不必中
            val spread = (i - (SERPENT_COUNT - 1) / 2.0) * FAN_ANGLE
            val aim = rotateY(base, spread)
            val jitter = 0.05
            serpent.setAim(
                aim.x + (circle.random.nextDouble() - 0.5) * jitter,
                aim.y + (circle.random.nextDouble() - 0.5) * jitter,
                aim.z + (circle.random.nextDouble() - 0.5) * jitter
            )
            level.addFreshEntity(serpent)
        }
    }

    private fun rotateY(v: Vec3, angle: Double): Vec3 {
        val c = cos(angle)
        val s = sin(angle)
        return Vec3(v.x * c + v.z * s, v.y, -v.x * s + v.z * c)
    }

    companion object {
        /** 一次放出几条。 */
        const val SERPENT_COUNT = 3
        /** 扇形间隔（弧度）。 */
        const val FAN_ANGLE = 0.18
    }
}
