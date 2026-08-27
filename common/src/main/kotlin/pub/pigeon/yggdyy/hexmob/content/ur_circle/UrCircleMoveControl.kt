package pub.pigeon.yggdyy.hexmob.content.ur_circle

import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.phys.Vec3

/**
 * 大环的飞行控制器——借鉴恶魂 GhastMoveControl：朝 wantedPosition 加速式飞行。
 * 大环 noPhysics，本体会穿透方块，但**外圈部件不能穿墙**：飞行方向先经
 * [UrCircleEntity.safeFlightDir] 部件回避（爬升/绕行），避免任何部件卡进方块。
 * 目标点由各 Goal/状态（巡航、冲撞、光束等）通过 setWantedPosition 设定。
 * 飞行速度随大环"怒意"（moveSpeedFactor：阻尼放大，索敌/低血微增）变化。
 */
class UrCircleMoveControl(private val circle: UrCircleEntity) : MoveControl(circle) {

    override fun tick() {
        if (!hasWanted()) {
            // 无目标点：惯性减速，原地悬停
            circle.setDeltaMovement(circle.deltaMovement.scale(0.8))
            return
        }
        val dir = Vec3(wantedX - circle.x, wantedY - circle.y, wantedZ - circle.z)
        val dist = dir.length()
        if (dist < 0.5) {
            // 已到达：刹住
            circle.setDeltaMovement(circle.deltaMovement.scale(0.8))
            return
        }
        // 移速：怒意阻尼放大（基本保持稳定，不会飞太疯）
        val f = circle.moveSpeedFactor().toDouble()
        // 部件回避：挑一个"环整体不穿墙"的方向；探测距离够刹得住（上限 2 倍巡航速）
        val lookAhead = (circle.deltaMovement.length() + 1.0).coerceAtMost(MAX_SPEED * 2.0)
        val safe = circle.safeFlightDir(dir.normalize(), lookAhead)
        if (safe == null) {
            // 所有方向都堵（贴着地形/卡墙角）：减速悬停，等脱困机会，不硬撞穿模
            circle.setDeltaMovement(circle.deltaMovement.scale(0.85))
            return
        }
        var vel = circle.deltaMovement.add(safe.scale(ACCELERATION * f))
        val max = (MAX_SPEED * f).coerceAtMost(MAX_CRUISE_SPEED)
        if (vel.length() > max) {
            vel = vel.normalize().scale(max)
        }
        circle.setDeltaMovement(vel)
    }

    companion object {
        /** 巡航基础最大飞行速度（格/tick）。 */
        const val MAX_SPEED = 0.45
        /** 巡航飞行速度上限（防止低血时飞太快失控）。 */
        const val MAX_CRUISE_SPEED = 1.0
        /** 巡航加速度（格/tick²），越大飞得越利落。 */
        const val ACCELERATION = 0.08
    }
}
