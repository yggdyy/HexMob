package pub.pigeon.yggdyy.hexmob.content.ur_circle

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.player.Player
import pub.pigeon.yggdyy.hexmob.registry.HexMobTags

/**
 * 大环的索敌：周期扫描附近 [SCAN_RANGE] 格内"有智慧"的生物（hexmob:wise tag——
 * 村民、流浪商人、猪灵、玩家、末影龙、灾厄村民），锁定最近的一个。
 *
 * 关键点：
 * - 每 10 tick 重扫一次（canUse/canContinueToUse/tick 都会触发），
 *   解决"锁死第一个目标永不换"的问题；
 * - 已有存活目标时不抢换，保证"被打还击"（hurt() 设的目标）能一直钉在玩家身上；
 * - **不索敌创造模式玩家**（大环不会主动招惹旁观者）；
 * - 锁定的目标会加入大环的仇恨列表（多目标攻击）。
 *
 * 大环是 Mob 而非 PathfinderMob，用不了 vanilla 的 NearestAttackableTargetGoal，
 * 所以自写一个（TargetGoal 只需 Mob）。
 */
class UrCircleTargetGoal(private val circle: UrCircleEntity) : TargetGoal(circle, true) {

    override fun canUse(): Boolean {
        if (!circle.isAwake()) return false // 沉睡：不索敌
        rescanIfDue()
        val t = circle.target ?: return false
        return t.isAlive
    }

    override fun canContinueToUse(): Boolean {
        if (!circle.isAwake()) return false
        rescanIfDue()
        val t = circle.target ?: return false
        return t.isAlive && circle.distanceToSqr(t) <= FOLLOW_RANGE * FOLLOW_RANGE
    }

    override fun tick() {
        rescanIfDue()
    }

    /** 每 10 tick 重扫：仅当没有存活目标时才重新锁定（跳过创造模式玩家）。
     *  多目标时按优先级选：**最优先玩家、其次剩余生命值最高**（见 UrCircleEntity.preferredTargets）。 */
    private fun rescanIfDue() {
        if (circle.tickCount % 10 != 0) return
        val cur = circle.target
        if (cur != null && cur.isAlive) return
        val target = circle.preferredTargets(
            circle.level()
                .getEntitiesOfClass(LivingEntity::class.java, circle.boundingBox.inflate(SCAN_RANGE))
                .filter {
                    it.isAlive && it !== circle &&
                        !(it is Player && it.isCreative) &&
                        it.type.`is`(HexMobTags.EntityTypeTags.WISE)
                }
        ).firstOrNull()
        if (target != null) {
            circle.target = target
            circle.addToHated(target)
        }
    }

    companion object {
        /** 索敌扫描范围（格）：每 10 tick 扫此半径内 wise 生物。 */
        const val SCAN_RANGE = 48.0
        /** 跟随范围（格）：超过后放弃当前目标（须大于扫描范围，避免追到一半掉锁）。 */
        const val FOLLOW_RANGE = 56.0
    }
}
