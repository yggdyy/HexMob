package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.api.casting.PatternShapeMatch
import at.petrak.hexcasting.api.casting.circles.ICircleComponent
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow.Continue
import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow.Stop
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent.ExtractMedia
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent.PostExecution
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.blocks.circles.BlockSlate
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.config.HexMobServerConfig
import kotlin.math.pow
import kotlin.math.roundToLong

class StimulatedSlateBlock(p: Properties?) : BlockSlate(p) {
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return StimulatedSlateBlockEntity(pPos, pState)
    }
    override fun acceptControlFlow(
        imageIn: CastingImage,
        env: CircleCastEnv,
        enterDir: Direction,
        pos: BlockPos,
        bs: BlockState,
        world: ServerLevel
    ): ICircleComponent.ControlFlow {
        val pattern: HexPattern = (pos.let { world.getBlockEntity(it) } as? StimulatedSlateBlockEntity)?.pattern ?: return Stop()
        val exitDirsSet = possibleExitDirections(pos, bs, world)
        exitDirsSet.remove(enterDir.opposite)
        val exitDirs = exitDirsSet.stream().map { dir: Direction? ->
            exitPositionFromDirection(
                pos,
                dir
            )
        }
        val data: CompoundTag = imageIn.userData
        val stimulatedPats: MutableList<HexPattern> = if(data.contains(KEY_STIMULATED_PATTERN)) {
            CODEC_STIMULATED_PATTERNS.decode(NbtOps.INSTANCE, data.get(KEY_STIMULATED_PATTERN)).get().map(
                {it.first.toMutableList()},
                { mutableListOf() }
            )
        } else {
            mutableListOf()
        }
        if(stimulatedPats.any { pattern.sigsEqual(it) }) {
            env.printMessage(Component.translatable("mishap.hexmob.duplicate_stimulated_slate"))
            return Stop()
        }
        stimulatedPats.add(pattern)
        data.put(KEY_STIMULATED_PATTERN, CODEC_STIMULATED_PATTERNS.encodeStart(NbtOps.INSTANCE, stimulatedPats).get().left().orElseGet{CompoundTag()})
        data.putInt(KEY_STIMULATED_PATTERN_COUNT, stimulatedPats.size)
        return Continue(imageIn.copy(userData = data), exitDirs.toList())
    }
    companion object {
        val KEY_STIMULATED_PATTERN_COUNT: String = HexMob.id("stimulated_pattern_count").toString()
        val KEY_STIMULATED_PATTERN: String = HexMob.id("stimulated_pattern").toString()
        val CODEC_STIMULATED_PATTERNS: Codec<List<HexPattern>> = Codec.list(HexPattern.CODEC)
        fun applyMediaDiscount(pEnv: CastingEnvironment, pData: CompoundTag) {
            if(pData.contains(KEY_STIMULATED_PATTERN_COUNT, Tag.TAG_INT.toInt())) {
                val p: Int = pData.getInt(KEY_STIMULATED_PATTERN_COUNT)
                if(p > 0) {
                    val factor: Double = (1.0 - HexMobServerConfig.config.opStimulatedSlateMediaDiscount).pow(p)
                    pEnv.addExtension(MediaDiscountExtension(factor))
                    pEnv.addExtension(PatternWhitelistExtension(factor, pEnv))
                }
            }
        }
        class MediaDiscountExtension(val factor: Double): ExtractMedia.Pre {
            override fun getKey(): CastingEnvironmentComponent.Key<*> = KEY
            override fun onExtractMedia(cost: Long, simulate: Boolean): Long = (cost * factor).toLong()
            companion object {
                class Key: CastingEnvironmentComponent.Key<MediaDiscountExtension>
                val KEY: Key = Key()
            }
        }
        class PatternWhitelistExtension(val factor: Double, val env: CastingEnvironment): PostExecution {
            override fun getKey(): CastingEnvironmentComponent.Key<*> = KEY
            override fun onPostExecution(result: CastResult) {
                var cost = 0L
                for(effect in result.sideEffects) {
                    val costMedia: OperatorSideEffect.ConsumeMedia = effect as? OperatorSideEffect.ConsumeMedia ?: continue
                    cost += costMedia.amount
                }
                if(cost > 0 && result.cast.type == HexIotaTypes.PATTERN) {
                    val pat: HexPattern = (result.cast as? PatternIota)?.pattern ?: return
                    val id: String = actionKey(PatternRegistryManifest.matchPattern(pat, env, false))?.toString() ?: return
                    if(HexMobServerConfig.config.stimulatedSlateBlacklist.contains(id)) {
                        val all: Long = (cost.toDouble() / factor).roundToLong()
                        val ext: Long = (cost.toDouble() / factor * (1.0 - factor)).roundToLong()
                        val rest: Long = env.extractMedia(all, true)
                        if(rest > 0) {
                            env.printMessage(MishapNotEnoughMedia(rest).errorMessageWithName(env, Mishap.Context(pat, Component.translatable("hexcasting.action.$id"))))
                            (env as? CircleCastEnv)?.apply {
                                world?.destroyBlock(circleState().currentPos, true)
                            }
                            (env as? PlayerBasedCastEnv)?.apply {
                                castingEntity?.hurt(world.damageSources().magic(), rest.toFloat() / MediaConstants.DUST_UNIT)
                            }
                        } else {
                            env.extractMedia(ext, false)
                        }
                    }
                }
            }
            companion object {
                class Key: CastingEnvironmentComponent.Key<PatternWhitelistExtension>
                val KEY: Key = Key()
                fun actionKey(match: PatternShapeMatch): ResourceLocation? {
                    return (match as? PatternShapeMatch.Normal)?.key?.location()
                        ?: ((match as? PatternShapeMatch.PerWorld)?.key?.location()
                            ?: (match as? PatternShapeMatch.Special)?.key?.location())
                }
            }
        }
    }
}