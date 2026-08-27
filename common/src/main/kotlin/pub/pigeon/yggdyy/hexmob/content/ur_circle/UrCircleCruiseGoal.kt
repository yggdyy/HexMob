package pub.pigeon.yggdyy.hexmob.content.ur_circle

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.Vec3
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 悬浮巡航（大环默认行为，凋灵式飞行）：
 * - 有目标：太远就逼近、太近就拉远、中距环绕，悬在目标斜上方（+3 格）做正弦浮动；
 * - 无目标：回到出生点（homePos）附近悬停浮动。
 * 通过 canUse 的状态闸门，后续招式（冲撞/光束）把状态切走时本 Goal 自动让位。
 */
class UrCircleCruiseGoal(private val circle: UrCircleEntity) : Goal() {

    override fun canUse(): Boolean =
        circle.isAwake() && circle.circleState == CircleState.CRUISE && !circle.isProtected()

    override fun canContinueToUse(): Boolean = canUse()

    override fun tick() {
        val origin = circle.position()
        val target = circle.target
        val wanted = if (target != null && target.isAlive) {
            cruiseAroundTarget(target, origin)
        } else {
            hoverAtHome(origin)
        }
        val mc = circle.moveControl
        if (mc is UrCircleMoveControl) {
            mc.setWantedPosition(wanted.x, clampHeight(wanted.y), wanted.z, 1.0)
        }
    }

    /** 不倾向飞太高：把目标高度限制在出生点上方 [FLY_CEILING] 之内。 */
    private fun clampHeight(wantedY: Double): Double {
        val home = circle.homePos ?: return wantedY
        val cap = home.y + FLY_CEILING
        return if (wantedY > cap) cap else wantedY
    }

    /** 凋灵式环绕：太远逼近、太近拉远、中距环绕；悬在目标斜上方 + 垂直浮动。 */
    private fun cruiseAroundTarget(target: LivingEntity, origin: Vec3): Vec3 {
        val t = circle.tickCount * 0.02
        val tc = target.position().add(0.0, target.bbHeight / 2.0, 0.0)
        // 目标 -> 大环 的水平方向（归一化）
        var dx = origin.x - tc.x
        var dz = origin.z - tc.z
        val h = sqrt(dx * dx + dz * dz)
        if (h < 0.001) {
            dx = 1.0; dz = 0.0 // 正上方/正下方时随便给个方向
        } else {
            dx /= h; dz /= h
        }
        // 距离控制：太远逼近（收向理想半径），太近拉远，中距环绕微调
        val range = when {
            h > APPROACH_RANGE -> PREFERRED_RANGE + 2.0
            h < BACK_OFF_RANGE -> PREFERRED_RANGE + 5.0
            else -> PREFERRED_RANGE + sin(t) * 2.0
        }
        // 理想点：目标水平面外 range + 随时间旋转的切向偏移（环绕 ±3 格）
        val orbit = sin(t) * 3.0
        val px = tc.x + dx * range - dz * orbit
        val pz = tc.z + dz * range + dx * orbit
        // 垂直：悬在目标斜上方 +3 格，正弦浮动 ±1.5
        val py = tc.y + 3.0 + sin(t * 1.3) * 1.5
        return Vec3(px, py, pz)
    }

    /** 无目标：回出生点悬停，只做垂直浮动。 */
    private fun hoverAtHome(origin: Vec3): Vec3 {
        val home = circle.homePos ?: origin
        val t = circle.tickCount * 0.02
        return Vec3(home.x, home.y + 2.0 + sin(t) * 1.5, home.z)
    }

    companion object {
        /** 与目标保持的理想水平距离（格）。 */
        const val PREFERRED_RANGE = 12.0
        /** 超过此水平距离就逼近目标。 */
        const val APPROACH_RANGE = 16.0
        /** 小于此水平距离就拉远。 */
        const val BACK_OFF_RANGE = 6.0
        /** 巡航最高飞行高度：出生点上方（格）——大环不倾向于飞得太高。 */
        const val FLY_CEILING = 16.0
    }
}
