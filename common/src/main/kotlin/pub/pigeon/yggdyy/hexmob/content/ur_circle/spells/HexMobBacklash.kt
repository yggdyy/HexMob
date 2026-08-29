package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.AABB
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity

/**
 * 反向过度施法（Backlash）：把"过度施法"的惩罚反向丢回施法者——
 * 玩家在大环附近每施一次法，就给大环积累一点"反噬值"；
 * 反噬值达到阈值，大环**立刻报复**（免吟唱）：丢事故 / 上 debuff 给施法者，然后清空重算。
 *
 * 通过 [CastingEnvironment.addCreateEventListener] 挂到 HexMob.init()：
 * 玩家每次施法创建 CastingEnvironment 时触发一次 [onCast]。
 */
object HexMobBacklash {
    /** 大环自身丢事故/报复期间置真，屏蔽 create 事件的误触发（避免递归积累）。 */
    var suppress = false

    /** 玩家施法环境创建时触发（服务端）。 */
    fun onCast(env: CastingEnvironment) {
        if (suppress) return
        val player: ServerPlayer? = env.caster
        if (player == null || !player.isAlive) return
        val level = player.level()
        if (level.isClientSide) return
        val circle = level.getEntitiesOfClass(
            UrCircleEntity::class.java,
            AABB(player.blockPosition()).inflate(UrCircleEntity.BACKLASH_RANGE)
        ) { it.isAlive }.minByOrNull { it.distanceToSqr(player) } ?: return
        circle.accumulateBacklash(player)
    }
}
