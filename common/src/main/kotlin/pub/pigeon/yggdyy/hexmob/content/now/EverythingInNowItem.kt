package pub.pigeon.yggdyy.hexmob.content.now

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceLocation

/**
 * 淬灵媒质立方（everything_in_now）：与"当下"相关的主题物品。
 *
 * 现在：装饰性物品，材质随世界时间循环换 3 帧（见 HexMobItemProperties 的 hexmob:phase 谓词）。
 *
 * 备用扩展位（后续按需启用，无需改注册）：
 * - 右击（use）切换 NBT 相位"hexmob:Phase"（0/1/2）——相位谓词可改成读 NBT；
 * - 接入 hexcasting 的 IMediaHolder 让它像媒质瓶一样存媒质、材质随储量变化；
 * - 配方/掉落（如作为大环的进阶战利品）。
 */
class EverythingInNowItem(properties: Properties) : Item(properties) {

    /** 相位 NBT 键（0/1/2，对应三帧材质）。 */
    companion object {
        val PHASE_TAG: ResourceLocation = ResourceLocation("hexmob", "phase")

        /** 当前相位（无 NBT 时返回 0）。 */
        fun getPhase(stack: ItemStack): Int =
            stack.tag?.getInt(PHASE_TAG.toString()).takeIf { it in 0..2 } ?: 0

        /** 设定 NBT 相位（0~2）。 */
        fun setPhase(stack: ItemStack, phase: Int) {
            stack.tag?.putInt(PHASE_TAG.toString(), phase.coerceIn(0, 2))
                ?: stack.setTag(net.minecraft.nbt.CompoundTag().apply { putInt(PHASE_TAG.toString(), phase.coerceIn(0, 2)) })
        }
    }
}