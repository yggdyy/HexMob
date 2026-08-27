package pub.pigeon.yggdyy.hexmob.content.crystal_spikes

import at.petrak.hexcasting.common.lib.HexBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * 【晶簇尖刺】Crystal Spike——冰刺之地的"晶簇版"。
 *
 * 形态抄 IceSpikeFeature（1.20.1 原版）：地表找点 → 锥形高尖柱 + 向下延伸 + 罕见超高柱。
 * 方块替换：
 * - 高尖柱（原浮冰 packed_ice）→ **紫水晶块们**（hexcasting:amethyst_blocks tag 随机）；
 * - 矮丘（原冰 ice）→ **板岩块们**（hexcasting:slate_blocks tag 随机）；
 * - 地面（整片群系）→ **深板岩**：由覆盖主世界的 surface rule 铺（data/minecraft/.../overworld.json）。
 */
class CrystalSpikeFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        var origin = context.origin()
        // 找地表：往下滑到非空方块
        while (level.isEmptyBlock(origin) && origin.y > level.minBuildHeight + 2) {
            origin = origin.below()
        }
        val ground = level.getBlockState(origin)
        if (ground.isAir || !ground.fluidState.isEmpty) return false

        val slates = slateBlocks(level)
        val amethysts = amethystBlocks(level)

        // 地面已是深板岩（surface rule 覆盖，整个群系地表），这里只生成尖柱和矮丘
        // 1) 主尖柱（紫水晶）：抄 IceSpikeFeature 锥形逻辑
        var top = origin.above(random.nextInt(4))
        val height = random.nextInt(4) + 7
        val baseR = height / 4 + random.nextInt(2)
        if (baseR > 1 && random.nextInt(60) == 0) {
            top = top.above(10 + random.nextInt(30)) // 罕见超高尖柱
        }
        placeSpike(level, top, height, baseR, random, amethysts)

        // 2) 板岩矮丘（原"冰"的小圆丘）
        for (i in 0 until random.nextInt(3)) {
            placeSlateMound(level, origin, random, slates)
        }
        return true
    }

    /** 锥形尖柱（紫水晶），逐层收窄；k>0 时同时向下填一段，保证尖柱扎进地面。 */
    private fun placeSpike(level: WorldGenLevel, top: BlockPos, height: Int, baseR: Int, random: RandomSource, amethysts: List<BlockState>) {
        for (k in 0 until height) {
            val f = 1.0F - k.toFloat() / height.toFloat()
            val r = Mth.ceil(f * baseR.toFloat())
            for (i1 in -r..r) {
                val f1 = Mth.abs(i1).toFloat() - 0.25F
                for (k1 in -r..r) {
                    val f2 = Mth.abs(k1).toFloat() - 0.25F
                    // 角部裁切（与 IceSpike 一致）：非中心列且平方和超出半径的跳过
                    if (i1 != 0 || k1 != 0) {
                        if (f1 * f1 + f2 * f2 > f * f) continue
                    }
                    // 外缘 25% 概率缺块，让尖柱边缘更"啃咬"
                    val edge = i1 == -r || i1 == r || k1 == -r || k1 == r
                    if (edge && random.nextFloat() > 0.75F) continue
                    placeSpikeBlock(level, top.offset(i1, k, k1), amethysts[random.nextInt(amethysts.size)])
                    if (k != 0 && r > 1) {
                        placeSpikeBlock(level, top.offset(i1, -k, k1), amethysts[random.nextInt(amethysts.size)])
                    }
                }
            }
        }
    }

    /** 只在"可替换"位置放尖柱块（空气/泥土类/雪/冰），不盖掉板岩地面与石头。 */
    private fun placeSpikeBlock(level: WorldGenLevel, pos: BlockPos, state: BlockState) {
        val st = level.getBlockState(pos)
        if (st.isAir || isGroundDirt(st) || st.`is`(Blocks.SNOW_BLOCK) || st.`is`(Blocks.ICE)) {
            setBlock(level, pos, state)
        }
    }

    /** 板岩矮丘：地表上 1~2 格高的小圆包。 */
    private fun placeSlateMound(level: WorldGenLevel, origin: BlockPos, random: RandomSource, slates: List<BlockState>) {
        val r = 1 + random.nextInt(2)
        val h = 1 + random.nextInt(2)
        val cx = origin.x + random.nextInt(7) - 3
        val cz = origin.z + random.nextInt(7) - 3
        for (dx in -r..r) {
            for (dz in -r..r) {
                if (dx * dx + dz * dz > r * r) continue
                val surfY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx + dx, cz + dz)
                for (dy in 0 until h) {
                    setBlock(level, BlockPos(cx + dx, surfY + dy + 1, cz + dz), slates[random.nextInt(slates.size)])
                }
            }
        }
    }

    private fun amethystBlocks(level: WorldGenLevel): List<BlockState> =
        tagBlocks(level, AMETHYST_TAG, Blocks.AMETHYST_BLOCK)

    private fun slateBlocks(level: WorldGenLevel): List<BlockState> =
        tagBlocks(level, SLATE_TAG, HexBlocks.SLATE_BLOCK)

    /** 从 hexcasting tag 里取"方块们"；tag 为空时退回单个基础方块。 */
    private fun tagBlocks(level: WorldGenLevel, tag: TagKey<Block>, fallback: Block): List<BlockState> {
        val result = ArrayList<BlockState>()
        for (h in level.registryAccess().registryOrThrow(Registries.BLOCK).getTagOrEmpty(tag)) {
            result.add(h.value().defaultBlockState())
        }
        return result.ifEmpty { listOf(fallback.defaultBlockState()) }
    }

    private fun isGroundDirt(state: BlockState): Boolean =
        state.`is`(Blocks.DIRT) || state.`is`(Blocks.COARSE_DIRT) || state.`is`(Blocks.PODZOL)

    companion object {
        private val AMETHYST_TAG = TagKey.create(Registries.BLOCK, ResourceLocation("hexcasting", "amethyst_blocks"))
        private val SLATE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation("hexcasting", "slate_blocks"))
    }
}

/** 世界生成 feature 注册：必须在 datapack 加载前把 feature 类注册进 BuiltInRegistries.FEATURE。 */
object HexMobFeatures {
    fun init() {
        Registry.register(BuiltInRegistries.FEATURE, HexMob.id("crystal_spike"), CrystalSpikeFeature())
    }
}
