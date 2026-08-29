package pub.pigeon.yggdyy.hexmob.api.entity

import at.petrak.hexcasting.api.casting.mishaps.Mishap
import net.minecraft.world.entity.Entity
import pub.pigeon.yggdyy.hexmob.api.mishap.MishapFlickeringEntity

/**
 * 标记：实体的 EntityIota 引用是"飘忽"的。
 *
 * 指向 [FlickeringEntity] 的 `EntityIota` 会周期性解析失败（`getEntity` 返回
 * null），因此依赖稳定实体引用的法术（追踪它、驱动它、传送它……）会时灵时不灵。
 * 这是 HexMob 保护 Boss 不被实体 iota 玩弄的方式：不是免疫，而是"拒绝引用"。
 *
 * 具体节奏由每个实现者通过 [flickerPeriodTicks] / [flickerInvalidTicks] /
 * [flickerOffsetTicks] 定制（默认：每 6 秒失效 1 秒，从第 3 秒开始）。
 */
interface FlickeringEntity {
    /** 一个完整失效周期的长度（tick）。 */
    val flickerPeriodTicks: Int
        get() = 20

    /** 每个周期内引用保持失效的时长（tick）。 */
    val flickerInvalidTicks: Int
        get() = 20

    /** 失效窗口在周期内的起始偏移（tick）。 */
    val flickerOffsetTicks: Int
        get() = 0

    /** 在世界时间 [gameTime] 时，指向本实体的引用是否应当失效。 */
    fun isReferenceFlickering(gameTime: Long): Boolean {
        val t = (gameTime + flickerOffsetTicks) % flickerPeriodTicks
        return t < flickerInvalidTicks
    }

    /**
     * 引用失效时抛出的事故（[entity] 为被引用的实体）。默认是通用
     * [MishapFlickeringEntity]；需要专属文案/效果（如大环的失明 + "自然本身"）时覆写。
     */
    fun createFlickeringMishap(entity: Entity): Mishap = MishapFlickeringEntity(entity)
}
