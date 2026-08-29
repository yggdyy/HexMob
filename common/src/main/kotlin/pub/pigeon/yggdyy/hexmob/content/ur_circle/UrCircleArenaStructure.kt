package pub.pigeon.yggdyy.hexmob.content.ur_circle

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
import pub.pigeon.yggdyy.hexmob.registry.HexMobStructures
import java.util.Optional

/**
 * crystal_spikes 群系里的 ur_circle 祭坛结构（1.20.1 无 minecraft:nbt 结构类型，自定义）。
 *
 * 放置：在 chunk 中心 + 地表高度，放一个 air 清空区 + 两只沉睡的 ur_circle（见 UrCircleArenaPiece）。
 * 生成率由 structure_set（random_spread, spacing≈60 → 每 50~80 区块一套）控制。
 */
class UrCircleArenaStructure(
    settings: StructureSettings,
    private val nbtId: ResourceLocation,
    private val rotation: Rotation
) : Structure(settings) {

    override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        val cx = context.chunkPos().middleBlockX
        val cz = context.chunkPos().middleBlockZ
        // 避免骑在群系边缘：祭坛占地 11×11 取 3×3 采样点，
        // 任一采样点不在 crystal_spikes 内就不在这里生成（不悬空挂边界）。
        val biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation("hexmob", "crystal_spikes"))
        val sampler = context.randomState().sampler()
        val fullyInside = listOf(-5, 0, 5).all { dx ->
            listOf(-5, 0, 5).all { dz ->
                context.biomeSource().getNoiseBiome((cx + dx) shr 2, 0, (cz + dz) shr 2, sampler).`is`(biomeKey)
            }
        }
        if (!fullyInside) return Optional.empty()
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG) { builder: StructurePiecesBuilder ->
            val ground = getLowestY(context, cx, cz, 7, 7)
            // 离地 18 格悬浮：大环与清空区整块升空
            val cy = ground + FLOAT_HEIGHT
            // 模板按原点往 +x/+z 方向铺 0..10（11×11），把原点往回偏 HALF_SPAN，
            // 祭坛整体与 boss（origin+5,+3,+5）才能正好落在区块中央。
            val origin = BlockPos(cx - HALF_SPAN, cy, cz - HALF_SPAN)
            builder.addPiece(UrCircleArenaPiece(context.structureTemplateManager(), nbtId, origin, rotation))
        }
    }

    override fun type(): StructureType<*> = HexMobStructures.UR_CIRCLE_ARENA.get()

    companion object {
        /** 祭坛离地悬浮高度（格）。 */
        const val FLOAT_HEIGHT = 18
        /** 模板半宽：11×11 布局的原点偏移量（让 boss 精确落于区块中心）。 */
        const val HALF_SPAN = 5
        val CODEC: Codec<UrCircleArenaStructure> = RecordCodecBuilder.create { b ->
            b.group(
                settingsCodec(b),
                ResourceLocation.CODEC.fieldOf("nbt").forGetter { it.nbtId },
                Codec.STRING.xmap({ s: String -> Rotation.valueOf(s) }, { r: Rotation -> r.name })
                    .fieldOf("rotation").forGetter { it.rotation }
            ).apply(b, ::UrCircleArenaStructure)
        }
    }
}
