package pub.pigeon.yggdyy.hexmob.content.ur_circle

/**
 * 大环的战斗状态机。
 * CRUISE=悬浮巡航（默认）；WINDUP/CHARGING/STAGGER（冲撞前摇/冲刺/僵直）与 BEAM（核心光线）
 * 由后续招式（第 3、4 步）驱动；CHANNELING=蓄力吟唱（转速逐渐停止 → 完全停下释放技能）；
 * DYING=死亡演出（参考末影龙：加速→减速→部件渐熄→依次消失→核心毁灭→掉落）。
 * 通过 SynchedEntityData 同步到客户端供渲染/粒子使用。
 */
enum class CircleState {
    CRUISE, WINDUP, CHARGING, STAGGER, BEAM, CHANNELING, DYING;

    companion object {
        fun byId(id: Int): CircleState = values()[id.coerceIn(values().indices)]
    }
}
