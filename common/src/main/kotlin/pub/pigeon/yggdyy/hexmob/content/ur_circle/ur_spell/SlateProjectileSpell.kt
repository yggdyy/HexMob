package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.api.casting.actions.UrCircleSpell
import pub.pigeon.yggdyy.hexmob.content.ur_circle.SlateProjectile
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities

/**
 * 【召唤石板弹】在指定位置生成 [SHOTS] 枚石板弹，朝指定方向发射
 * （shoot 内置归一化：向量只需给方向，速度固定 [SPEED]）。
 * 命中实体 8 伤害 + 击退，命中方块炸 3×3×3 坑并转化部分方块（照大环石板弹）。
 *
 * 参数：0 = 生成位置（向量 iota），1 = 方向（向量 iota，可非单位长）。
 */
class SlateProjectileSpell : UrCircleSpell() {
    override val argc: Int get() = 2

    override fun cost(args: List<Iota>, env: CastingEnvironment): Long = MediaConstants.DUST_UNIT*3

    override fun makeEffect(args: List<Iota>, env: CastingEnvironment): RenderedSpell {
        val pos = args.getVec3(0, argc)
        val aim = args.getVec3(1, argc)
        if (!env.isVecInRange(pos)) throw MishapBadLocation(pos)
        return Spell(pos, aim)
    }

    class Spell(private val from: Vec3, private val aim: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.castingEntity ?: return
            val level = env.world
            if (level.isClientSide) return
            for (i in 0 until SHOTS) {
                val projectile = SlateProjectile(HexMobEntities.SLATE_PROJECTILE.get(), level)
                projectile.setPos(from.x, from.y, from.z)
                projectile.owner = caster
                projectile.pattern = PROJECTILE_PATTERN
                projectile.shoot(aim.x, aim.y, aim.z, SPEED, INACCURACY)
                level.addFreshEntity(projectile)
            }
        }
    }

    companion object {
        /** 一次射出的石板弹数量（玩家版只射一枚）。 */
        const val SHOTS = 1
        /** 飞行速度（照大环 SLATE_SPEED）。 */
        const val SPEED = 1.0F
        /** 射击散布（照大环）。 */
        const val INACCURACY = 1.0F
        /** 弹体上携带的展示图案（渲染用）。 */
        val PROJECTILE_PATTERN: HexPattern = HexPattern.fromAngles("aq", HexDir.EAST)
    }
}