package pub.pigeon.yggdyy.hexmob.client

/**
 * 煤气灯换皮 tracker——照抄 hexcasting 淬灵晶碎片的 GaslightingTracker：
 *
 * - 模型谓词每次读取本 tracker（=物品正在被渲染）把"看着"冷却刷新到 40 tick；
 * - 客户端每 tick（未暂停）推进：连续 40 tick 没人看它，相位才 +1。
 *
 * 效果与淬灵晶碎片完全一致。
 */
object HexMobGaslightingTracker {
    private var amount = 0
    private const val LOOKING_COOLDOWN_MAX = 40
    private var lookCooldown = LOOKING_COOLDOWN_MAX

    /** 每次被模型谓词读取（=正在渲染）都刷新"看着"冷却。 */
    fun getGaslightingAmount(): Int {
        lookCooldown = LOOKING_COOLDOWN_MAX
        return amount
    }

    /** 客户端每 tick 调用：连续 40 tick 没人看才推进一档。 */
    fun postFrameCheckRendered() {
        if (lookCooldown > 0) {
            lookCooldown -= 1
        } else {
            amount += 1
        }
    }
}