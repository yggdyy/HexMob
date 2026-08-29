package pub.pigeon.yggdyy.hexmob.content.quench_allay.action

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.InteractionHand
import pub.pigeon.yggdyy.hexmob.api.casting.env.CastingEntityEnv
import pub.pigeon.yggdyy.hexmob.content.quench_allay.QuenchAllay

object OpAllayTarget: SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val entity = args.getLivingEntityButNotArmorStand(0,argc)
        val vec = args.getVec3(1,argc)
        if(entity is QuenchAllay && env.isEntityInRange(entity)) {
            return SpellAction.Result(
                effect = object : RenderedSpell {
                    override fun cast(env: CastingEnvironment) {
                     entity.setAllayTarget(vec)
                    }
                },
                cost = MediaConstants.DUST_UNIT,
                particles = listOf()
            )
        }
        else if(!env.isEntityInRange(entity)) {
            throw MishapEntityTooFarAway(entity)
        }
        else throw MishapBadEntity.of(entity,"quench_allay")
    }
}