package pub.pigeon.yggdyy.hexmob.content.ur_circle

import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks

/** Hex Casting（HexMod）的紫水晶/板岩类方块 tag：石板弹不破坏这些方块。 */
private val HEX_AMETHYST_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation("hexcasting", "amethyst_blocks"))
private val HEX_SLATE_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation("hexcasting", "slate_blocks"))

/**
 * 大环/石板弹的"砸地"：在 pos 处破坏 3×3×3 范围（跳过空气、液体与不可破坏方块
 * ——getDestroySpeed < 0，如基岩/屏障/命令方块），并播放法术释放音效（声音偏大）。
 *
 * @param drop 破坏时是否掉落方块（石板弹要求不掉落）。
 * @param skipHexStones 是否跳过 Hex Casting 的紫水晶/板岩类方块（石板弹要求不破坏它们）。
 * @param convertChance 0~1：命中方块时把"一部分"方块直接转化为紫水晶/板岩（替代破坏，无掉落）。
 */
fun craterAround(
    level: Level,
    pos: BlockPos,
    dropper: Entity,
    drop: Boolean = true,
    skipHexStones: Boolean = false,
    convertChance: Float = 0.0F,
) {
    for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
        val p = pos.offset(dx, dy, dz)
        val state = level.getBlockState(p)
        if (state.isAir || !state.fluidState.isEmpty) continue
        if (state.getDestroySpeed(level, p) < 0.0F) continue
        if (skipHexStones && (state.`is`(HEX_AMETHYST_BLOCKS) || state.`is`(HEX_SLATE_BLOCKS))) continue
        if (convertChance > 0.0F && level.random.nextFloat() < convertChance) {
            // 转化为紫水晶块或板岩块（各半），替代破坏、无掉落
            val converted = if (level.random.nextBoolean()) {
                Blocks.AMETHYST_BLOCK.defaultBlockState()
            } else {
                HexBlocks.SLATE_BLOCK.defaultBlockState()
            }
            level.setBlockAndUpdate(p, converted)
        } else {
            level.destroyBlock(p, drop, dropper)
        }
    }
    level.playSound(null, pos, HexSounds.CAST_SPELL, SoundSource.BLOCKS, 2.0F, 1.0F)
}
