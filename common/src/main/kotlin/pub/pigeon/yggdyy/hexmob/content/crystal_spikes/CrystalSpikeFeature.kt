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
 * 【晶簇尖刺】Crystal Spike——冰刺之地的"晶簇版"，1:1 镜像原版 IceSpikeFeature 的写法：
 * 锥形高尖柱（高 7~10、基半径 1~3、罕见超高柱 1/60）+ **底部根**（基座下 3×3 列向下填、随机跳过、到 y≤50）。
 * 另加原版 ice_patch 的对应物：地表**板岩台地斑块**（圆盘，半径 3~5）。
 *
 * 方块替换：
 * - 尖柱 + 根部（原 packed_ice）→ **紫水晶块们**（hexcasting:amethyst_blocks tag，柱身多种随机）
 * - 台地斑块 + 矮丘（原 ice_patch / 冰）→ **板岩块们**（hexcasting:slate_blocks tag）
 * - 地面（整片群系）→ **板岩块**：由 NoiseBasedChunkGeneratorMixin 的 surface rule 铺。
 * - 地面点缀 → 紫水晶 + 板岩嵌紫水晶方块（slate_amethyst_tiles 等）嵌进地表。
 */
class CrystalSpikeFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        var origin = context.origin()
        // 找地表：往下滑到非空方块（与 IceSpikeFeature 一致）
        while (level.isEmptyBlock(origin) && origin.y > level.minBuildHeight + 2) {
            origin = origin.below()
        }
        val ground = level.getBlockState(origin)
        if (ground.isAir || !ground.fluidState.isEmpty) return false

        val slates = slateBlocks(level)
        val amethysts = amethystBlocks(level)
        val decos = decorBlocks(level)

        // ⚠ 顺序关键：必须先铺地面内容（台地/矮丘/点缀），再立尖柱——
        // getHeight(地表) 会看到已放置的尖柱并返回柱顶高度，把板岩圆盘摆到柱子顶上 = 顶端变板岩。
        // 1) 板岩台地斑块（镜像原版 ice_patch）：2~4 片圆盘铺在地表上
        val pads = 2 + random.nextInt(3)
        for (i in 0 until pads) {
            placeSlatePad(level, origin, random, slates)
        }
        // 2) 板岩矮丘：0~2 个小圆包
        for (i in 0 until random.nextInt(3)) {
            placeSlateMound(level, origin, random, slates)
        }
        // 3) 地面点缀：紫水晶 / 板岩嵌紫水晶方块"嵌"进地表
        placeGroundDecor(level, origin, random, decos)

        // 4) 主尖柱（紫水晶）：原版参数 + 根部；柱身用多种紫水晶随机拼接
        var top = origin.above(random.nextInt(4))
        val height = random.nextInt(8) + 12 // 细长：12~19
        val baseR = height / 5 + random.nextInt(2) // 更细：基半径按 1/5 高算（2~4）
        if (baseR > 1 && random.nextInt(60) == 0) {
            top = top.above(10 + random.nextInt(30)) // 罕见超高尖柱
        }
        placeSpike(level, top, height, baseR, random, amethysts, slates)
        placeSpikeRoot(level, top, baseR, random, slates)
        return true
    }

    /** 锥形尖柱：凸曲线收窄 + 高度渐变——越往上板岩越少、紫水晶越多（底部板岩柱体，顶部紫晶冠）。 */
    private fun placeSpike(level: WorldGenLevel, top: BlockPos, height: Int, baseR: Int, random: RandomSource, amethysts: List<BlockState>, slates: List<BlockState>) {
        val plainAmethyst = Blocks.AMETHYST_BLOCK.defaultBlockState()
        // 变体池 = tag 里去掉纯紫水晶块后的 hexcasting 变体（bricks/tiles/pillar）
        val amethystVariants = amethysts.filterNot { it.`is`(Blocks.AMETHYST_BLOCK) }.ifEmpty { amethysts }
        for (k in 0 until height) {
            // 线性收窄（原版冰刺写法）：f = 实际半径浮点 (1-k/h)·baseR，r=ceil(f)。
            // ⚠ 角裁切必须用这个实际 f 判（原版写法 f1²+f2²>f²），误用归一化 f² 会让各层裁空成细杆
            val hRatio = k.toFloat() / height.toFloat()
            val f = (1.0F - hRatio) * baseR.toFloat()
            val r = Mth.ceil(f)
            // 尽量少板岩：底部 85% 紫晶、顶部近 100%；板岩只在基座零星点缀
            val t = 0.85F + 0.15F * hRatio
            val pick = {
                if (random.nextFloat() < t) {
                    // 紫水晶里 80% 用纯"紫水晶块"，20% 用 hexcasting 变体
                    if (random.nextFloat() < 0.8F) plainAmethyst else amethystVariants[random.nextInt(amethystVariants.size)]
                } else {
                    slates[random.nextInt(slates.size)]
                }
            }
            for (i1 in -r..r) {
                val f1 = Mth.abs(i1).toFloat() - 0.25F
                for (k1 in -r..r) {
                    val f2 = Mth.abs(k1).toFloat() - 0.25F
                    if (i1 != 0 || k1 != 0) {
                        if (f1 * f1 + f2 * f2 > f * f) continue
                    }
                    val edge = i1 == -r || i1 == r || k1 == -r || k1 == r
                    if (edge && random.nextFloat() > 0.75F) continue
                    placeSpikeBlock(level, top.offset(i1, k, k1), pick())
                    if (k != 0 && r > 1) {
                        placeSpikeBlock(level, top.offset(i1, -k, k1), pick())
                    }
                }
            }
        }
    }

    /** 底部根（原版 IceSpikeFeature 根部循环）：基座下 3×3 列向下填板岩，随机跳过，直到 y≤50 或撞到不可替换方块。 */
    private fun placeSpikeRoot(level: WorldGenLevel, top: BlockPos, baseR: Int, random: RandomSource, slates: List<BlockState>) {
        var i1 = baseR - 1
        if (i1 < 0) i1 = 0
        if (i1 > 1) i1 = 1
        for (i2 in -i1..i1) {
            for (i3 in -i1..i1) {
                var pos = top.offset(i2, -1, i3)
                var j = 50
                if (Math.abs(i2) == 1 && Math.abs(i3) == 1) {
                    j = random.nextInt(5) // 角柱：短根
                }
                while (pos.y > 50) {
                    val st = level.getBlockState(pos)
                    if (st.isAir || isGroundDirt(st) || st.`is`(Blocks.SNOW_BLOCK) || st.`is`(Blocks.ICE)
                        || st.`is`(Blocks.DEEPSLATE) || st.`is`(Blocks.STONE)
                    ) {
                        setBlock(level, pos, slates[random.nextInt(slates.size)])
                        pos = pos.below()
                        j--
                        if (j <= 0) {
                            pos = pos.below(1 + random.nextInt(5)) // 随机向下跳段（产生"解离"悬浮块）
                            j = random.nextInt(5)
                        }
                    } else {
                        break
                    }
                }
            }
        }
    }

    /** 只在"可替换"位置放尖柱块（空气/泥土类/雪/冰/深板岩/石头），不盖掉突兀的东西。 */
    private fun placeSpikeBlock(level: WorldGenLevel, pos: BlockPos, state: BlockState) {
        val st = level.getBlockState(pos)
        if (st.isAir || isGroundDirt(st) || st.`is`(Blocks.SNOW_BLOCK) || st.`is`(Blocks.ICE)
            || st.`is`(Blocks.DEEPSLATE) || st.`is`(Blocks.STONE)
        ) {
            setBlock(level, pos, state)
        }
    }

    /** 板岩台地斑块：地表圆盘（半径 3~5、厚 1~2），把深板岩/泥土表层换成板岩块们。 */
    private fun placeSlatePad(level: WorldGenLevel, origin: BlockPos, random: RandomSource, slates: List<BlockState>) {
        val radius = 3 + random.nextInt(3)
        val depth = 1 + random.nextInt(2)
        val cx = origin.x + random.nextInt(9) - 4
        val cz = origin.z + random.nextInt(9) - 4
        for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                if (dx * dx + dz * dz > radius * radius) continue
                val surfY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx + dx, cz + dz)
                for (dy in 0 until depth) {
                    val pos = BlockPos(cx + dx, surfY - dy, cz + dz)
                    val st = level.getBlockState(pos)
                    if (st.isAir || isGroundDirt(st) || st.`is`(Blocks.SNOW_BLOCK) || st.`is`(Blocks.ICE)
                        || st.`is`(Blocks.DEEPSLATE) || st.`is`(Blocks.STONE)
                    ) {
                        setBlock(level, pos, slates[random.nextInt(slates.size)])
                    }
                }
            }
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

    /** 地面点缀方块：紫水晶块们 + 板岩嵌紫水晶方块们（slate_amethyst_tiles 等）。 */
    private fun decorBlocks(level: WorldGenLevel): List<BlockState> {
        val result = ArrayList<BlockState>()
        result.addAll(amethystBlocks(level))
        for (b in listOf(
            HexBlocks.SLATE_AMETHYST_TILES,
            HexBlocks.SLATE_AMETHYST_BRICKS,
            HexBlocks.SLATE_AMETHYST_BRICKS_SMALL,
            HexBlocks.SLATE_AMETHYST_PILLAR,
        )) {
            result.add(b.defaultBlockState())
        }
        return result
    }

    /** 地面点缀：在尖柱周围把紫水晶/板岩嵌紫水晶方块嵌进地表（只替换地表方块，不凸起）。 */
    private fun placeGroundDecor(level: WorldGenLevel, origin: BlockPos, random: RandomSource, decos: List<BlockState>) {
        val count = 4 + random.nextInt(7) // 4~10
        repeat(count) {
            val x = origin.x + random.nextInt(17) - 8
            val z = origin.z + random.nextInt(17) - 8
            val y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
            val pos = BlockPos(x, y, z)
            val st = level.getBlockState(pos)
            if (st.isAir || isGroundDirt(st) || st.`is`(Blocks.DEEPSLATE) || st.`is`(Blocks.STONE)
                || st.`is`(HexBlocks.SLATE_BLOCK)
            ) {
                setBlock(level, pos, decos[random.nextInt(decos.size)])
            }
        }
    }

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
