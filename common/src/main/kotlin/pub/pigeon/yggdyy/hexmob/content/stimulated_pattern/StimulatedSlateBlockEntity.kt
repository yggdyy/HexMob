package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import pub.pigeon.yggdyy.hexmob.registry.HexMobBlocks

class StimulatedSlateBlockEntity(pType: BlockEntityType<*>?, pWorldPosition: BlockPos?, pBlockState: BlockState?) : HexBlockEntity(pType,
    pWorldPosition, pBlockState
) {
    constructor(pos: BlockPos, state: BlockState): this(HexMobBlocks.STIMULATED_SLATE_TILE.get(), pos, state)
    var pattern: HexPattern? = null
    override fun saveModData(tag: CompoundTag) {
        if (pattern != null) {
            tag.put(TAG_PATTERN, pattern!!.serializeToNBT())
        } else {
            tag.put(TAG_PATTERN, CompoundTag())
        }
    }
    override fun loadModData(tag: CompoundTag) {
        pattern = if (tag.contains(TAG_PATTERN, Tag.TAG_COMPOUND.toInt())) {
            val patternTag = tag.getCompound(TAG_PATTERN)
            if (HexPattern.isPattern(patternTag)) {
                HexPattern.fromNBT(patternTag)
            } else {
                null
            }
        } else {
            null
        }
    }
    companion object {
        val TAG_PATTERN = "pattern"
    }
}