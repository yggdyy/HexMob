package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.utils.getOrCreateCompound
import at.petrak.hexcasting.api.utils.remove
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate
import at.petrak.hexcasting.common.items.storage.ItemSlate
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import pub.pigeon.yggdyy.hexmob.HexMob

class StimulatedSlateItem(pBlock: Block?, pProperties: Properties?) : ItemSlate(pBlock, pProperties) {
    override fun getName(pStack: ItemStack): Component {
        val key = "block." + HexMob.MODID + ".stimulated_slate." + if (hasPattern(pStack)) "written" else "blank"
        return Component.translatable(key)
    }
    override fun writeable(stack: ItemStack?) = false
    override fun canWrite(stack: ItemStack?, datum: Iota?): Boolean {
        return false
    }
    override fun writeDatum(stack: ItemStack, datum: Iota?) {
        if (datum == null) {
            val beTag = stack.getOrCreateCompound("BlockEntityTag")
            beTag.remove(BlockEntitySlate.TAG_PATTERN)
            if (beTag.isEmpty) {
                stack.remove("BlockEntityTag")
            }
        } else if (datum is PatternIota) {
            val beTag = stack.getOrCreateCompound("BlockEntityTag")
            beTag.put(BlockEntitySlate.TAG_PATTERN, datum.pattern.serializeToNBT())
        }
    }
}