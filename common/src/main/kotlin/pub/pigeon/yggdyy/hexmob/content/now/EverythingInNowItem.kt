package pub.pigeon.yggdyy.hexmob.content.now

import net.minecraft.world.item.Item

/**
 * 淬灵媒质立方（everything_in_now）：与"当下"相关的主题物品。
 *
 * 变材质完全照抄 hexcasting 淬灵晶碎片：
 * - 模型 = 纯 overrides（无父模型/无顶层贴图，见 everything_in_now.json，与 quenched_allay_shard.json 同构）；
 * - 谓词 = HexMobGaslightingTracker.getGaslightingAmount() % 3（煤气灯：盯着看不动，不看 40 tick 换一档）。
 *
 * 备用扩展位：接入 IMediaHolder 存媒质、材质随储量变化；作大环战利品等。
 */
class EverythingInNowItem(properties: Properties) : Item(properties)