package pub.pigeon.yggdyy.hexmob.content.quench_allay.action

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway
import net.minecraft.world.InteractionHand
import pub.pigeon.yggdyy.hexmob.api.casting.env.CastingEntityEnv
import pub.pigeon.yggdyy.hexmob.content.quench_allay.QuenchAllay

object OpAllayCasting: SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val entity = args.getLivingEntityButNotArmorStand(0,argc)
        val spell = args.getList(1, argc)
        if(entity is QuenchAllay && env.isEntityInRange(entity)) {
            return SpellAction.Result(
                effect = object : RenderedSpell{
                    override fun cast(env: CastingEnvironment) {
                        val newenv = CastingEntityEnv(entity, InteractionHand.MAIN_HAND)
                        val vm = CastingVM.empty(newenv)
                        vm.queueExecuteAndWrapIotas(spell.toList(),newenv.world)
                    }
                },
                cost = 0,
                particles = listOf()
            )
        }
        else if(!env.isEntityInRange(entity)) {
            throw MishapEntityTooFarAway(entity)
        }
        else throw MishapBadEntity.of(entity,"quench_allay")
    }
}