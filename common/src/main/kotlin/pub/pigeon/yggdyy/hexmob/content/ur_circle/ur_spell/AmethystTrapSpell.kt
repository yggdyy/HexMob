package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Blocks
import pub.pigeon.yggdyy.hexmob.api.casting.actions.UrCircleSpell

/**
 * 【紫水晶陷阱】在目标实体脚下铺 3×3×3 紫水晶块，困住并使其窒息。
 * 大环技能 Amethyst Trap 的玩家施法版：只埋目标脚下，不带大环的自环柱。
 */
class AmethystTrapSpell : UrCircleSpell() {
    override val argc: Int get() = 1

    override fun cost(args: List<Iota>, env: CastingEnvironment): Long = 30L

    override fun makeEffect(args: List<Iota>, env: CastingEnvironment): RenderedSpell =
        Spell(livingTarget(args, env))

    class Spell(private val target: LivingEntity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val level = env.world
            if (level.isClientSide) return
            val base = target.blockPosition()
            for (dx in -1..1) {
                for (dy in -1..1) {
                    for (dz in -1..1) {
                        val pos = base.offset(dx, dy, dz)
                        if (level.getBlockState(pos).isAir) {
                            level.setBlockAndUpdate(pos, Blocks.AMETHYST_BLOCK.defaultBlockState())
                        }
                    }
                }
            }
        }
    }
}