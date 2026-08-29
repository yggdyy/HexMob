package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.api.casting.actions.UrCircleSpell
import pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent.UrCircleSerpent
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 【促动石长蛇】在指定位置生成长蛇，**方向决定形态**：
 * - 方向接近竖直（|y| > [VERTICAL_THRESHOLD]）：朝该方向放 [VERTICAL_COUNT] 条冲天式长蛇
 *   （如 (0,1,0) 就是从脚下贯到天上的冲天形态）；
 * - 方向接近水平：以该方向为基准扇形放出 [LATERAL_COUNT] 条横向长蛇（不必中，躲开就落空）。
 *
 * 参数：0 = 生成位置（向量 iota），1 = 方向（向量 iota，可非单位长）。
 */
class SerpentSpell : UrCircleSpell() {
    override val argc: Int get() = 2

    override fun cost(args: List<Iota>, env: CastingEnvironment): Long = 45L

    override fun makeEffect(args: List<Iota>, env: CastingEnvironment): RenderedSpell {
        val pos = args.getVec3(0, argc)
        val dir = args.getVec3(1, argc)
        if (!env.isVecInRange(pos)) throw MishapBadLocation(pos)
        return Spell(pos, dir)
    }

    class Spell(private val pos: Vec3, private val dir: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.castingEntity ?: return
            val level = env.world
            if (level.isClientSide) return
            val aim = if (dir.lengthSqr() > 1.0E-6) dir.normalize() else Vec3(0.0, 0.0, 1.0)
            val rng = level.random
            if (abs(aim.y) > VERTICAL_THRESHOLD) {
                // 竖直形态：在生成位置附近放冲天长蛇，朝 dir 上涌
                for (i in 0 until VERTICAL_COUNT) {
                    val serpent = UrCircleSerpent(HexMobEntities.UR_CIRCLE_SERPENT.get(), level)
                    serpent.owner = caster
                    val ox = (rng.nextDouble() - 0.5) * 1.6
                    val oz = (rng.nextDouble() - 0.5) * 1.6
                    serpent.setPos(pos.x + ox, pos.y - 1.0, pos.z + oz)
                    serpent.setAim(aim.x, aim.y, aim.z)
                    level.addFreshEntity(serpent)
                }
            } else {
                // 水平形态：以 dir 为基准扇形放出横向长蛇
                for (i in 0 until LATERAL_COUNT) {
                    val serpent = UrCircleSerpent(HexMobEntities.UR_CIRCLE_SERPENT.get(), level)
                    serpent.owner = caster
                    val ang = rng.nextDouble() * Math.PI * 2.0
                    serpent.setPos(pos.x + cos(ang) * 0.6, pos.y, pos.z + sin(ang) * 0.6)
                    val spread = (i - (LATERAL_COUNT - 1) / 2.0) * FAN_ANGLE
                    val v = rotateY(aim, spread)
                    serpent.setAim(v.x + JITTER, v.y + JITTER, v.z + JITTER)
                    level.addFreshEntity(serpent)
                }
            }
        }

        private fun rotateY(v: Vec3, angle: Double): Vec3 {
            val c = cos(angle)
            val s = sin(angle)
            return Vec3(v.x * c + v.z * s, v.y, -v.x * s + v.z * c)
        }
    }

    companion object {
        /** 判定"竖直形态"的 |y| 阈值。 */
        const val VERTICAL_THRESHOLD = 0.5
        /** 竖直形态条数。 */
        const val VERTICAL_COUNT = 3
        /** 水平形态横向条数。 */
        const val LATERAL_COUNT = 2
        /** 扇形间隔（弧度）。 */
        const val FAN_ANGLE = 0.18
        /** 朝向微抖动。 */
        const val JITTER = 0.05
    }
}