package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import pub.pigeon.yggdyy.hexmob.registry.HexMobBlocks

class OpTransformStimulatedPatternSpell: SpellAction {
    override val argc: Int
        get() = 3
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val slatePos: BlockPos = args.getBlockPos(0, argc)
        if(!env.isVecInRange(slatePos.center)) throw MishapBadLocation(slatePos.center)
        val entity: StimulatedPatternEntity = args.getEntity(1, argc) as? StimulatedPatternEntity ?: throw MishapBadEntity(args.getEntity(1, argc), Component.translatable("entity.hexmob.stimulated_pattern"))
        val plPat: HexPattern = args.getPattern(2, argc)
        val slateTile: StimulatedSlateBlockEntity = if (env.world.getBlockState(slatePos).`is`(HexMobBlocks.STIMULATED_SLATE_BLOCK.get())) {
            env.world.getBlockEntity(slatePos) as? StimulatedSlateBlockEntity ?: throw MishapBadBlock(slatePos, Component.translatable("block.hexmob.stimulated_slate"))
        } else {
            throw MishapBadBlock(slatePos, Component.translatable("block.hexmob.stimulated_slate"))
        }
        return SpellAction.Result(Spell(entity, slateTile, plPat), HexMobServerConfig.config.opTransformStimulatedPatternCost, listOf())
    }
    data class Spell(val entity: StimulatedPatternEntity, val tile: StimulatedSlateBlockEntity, val plPat: HexPattern): RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if(plPat.sigsEqual(entity.getPattern())) {
                env.world.playSound(null, BlockPos.containing(entity.position()), HexSounds.CAST_SPELL, SoundSource.PLAYERS, 1F, 1F)
                val particle = ParticleSpray(entity.eyePosition, Vec3(0.0, 1.0, 0.0), 0.0, 0.1)
                particle.sprayParticles(env.world, env.pigment)
                entity.discard()
                tile.pattern = plPat
                tile.sync()
            } else {
                val particle = ParticleSpray(entity.eyePosition, Vec3(0.0, 1.0, 0.0), 0.0, 0.1)
                particle.sprayParticles(env.world, FrozenPigment(ItemStack.EMPTY, entity.uuid))
                entity.hurt(env.castingEntity?.damageSources()?.magic() ?: env.world.damageSources().magic(), 5F)
            }
        }
    }
}