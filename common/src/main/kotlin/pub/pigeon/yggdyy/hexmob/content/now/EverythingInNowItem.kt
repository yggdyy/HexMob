package pub.pigeon.yggdyy.hexmob.content.now

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import at.petrak.hexcasting.common.lib.HexAttributes
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * 淬灵媒质立方（everything_in_now）：与"当下"相关的主题物品。
 *
 * 玩法（击败大环后与核心一同掉落的战利品）：
 * - **媒质容器**
 *   施法时可供澄媒质（可被法术消耗）。
 * - **食用**：恢复大量饱食度且不消耗（[finishUsingItem] 返回副本）。
 * - **Shift 右键**：提取自身所存媒质，转化为副手上的结晶态媒质物品
 *   （紫水晶粉/碎片/充能紫水晶/淬灵晶碎片/淬灵晶块），媒质不足时聊天提示。
 * - **手持范围**：主/副手持有自动获得 +[AMBIT_BONUS] 施法范围、+[SENTINEL_BONUS] 哨卫范围
 *   （hexcasting 的 [HexAttributes.AMBIT_RADIUS]/[HexAttributes.SENTINEL_RADIUS] attribute）。
 * - **右键玩家**：授予其 `hexcasting:enlightenment`（启迪）成就。
 *
 * 变材质照抄 hexcasting 淬灵晶碎片（煤气灯相位），见 everything_in_now.json。
 */
class EverythingInNowItem(properties: Properties) : ItemMediaHolder(properties) {

    // 媒质存取/容量条显示/描述由 ItemMediaHolder 提供（getMedia/setMedia/isBarVisible/getBarColor/getBarWidth）
    // 固定容量：64 淬灵晶块（默认配置下 = 7680 粉）
    override fun getMaxMedia(stack: ItemStack): Long = MAX_MEDIA

    override fun canProvideMedia(stack: ItemStack): Boolean = getMedia(stack) > 0

    override fun canRecharge(stack: ItemStack): Boolean = getMedia(stack) < getMaxMedia(stack)


    // ---- 食用：恢复大量饱食度，不消耗 ----
    override fun getUseDuration(stack: ItemStack): Int = 32

    override fun getUseAnimation(stack: ItemStack): UseAnim = UseAnim.EAT

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (player.isShiftKeyDown) {
            val offhand = player.offhandItem
            val targetEntry = CRYSTALLINE_MEDIA.entries.firstOrNull { (id, _) ->
                BuiltInRegistries.ITEM.getKey(offhand.item) == id
            }
            if (targetEntry == null) {
                return InteractionResultHolder.fail(stack)
            }
            val (_, mediaPerUnit) = targetEntry
            if (getMedia(stack) < mediaPerUnit) {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                        Component.translatable("message.hexmob.everything_in_now.insufficient_media"), true
                    )
                }
                return InteractionResultHolder.fail(stack)
            }
            // 提取媒质，副手结晶态媒质物品 +1
            setMedia(stack, getMedia(stack) - mediaPerUnit)
            if (!level.isClientSide) {
                offhand.grow(1)
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        }
        if (player.foodData.needsFood() || player.isCreative) {
            player.startUsingItem(hand)
            return InteractionResultHolder.consume(stack)
        }
        return InteractionResultHolder.fail(stack)
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, entity: LivingEntity): ItemStack {
        if (entity is Player && !level.isClientSide) {
            // 直接恢复饱食度与饱和度（不依赖 food properties），再吃回少量
            entity.foodData.eat(FOOD_NUTRITION, FOOD_SATURATION)
            level.playSound(
                null,
                entity.x,
                entity.y,
                entity.z,
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            )
        }
        // 不消耗：返回副本，不动原堆叠
        return stack.copy()
    }

    // ---- Shift 右键提取及使用被打断的兜底已经在上方 use 处理，无需额外逻辑 ----

    // ---- 手持范围：主/副手 attribute ----
    override fun getDefaultAttributeModifiers(slot: EquipmentSlot): Multimap<Attribute, AttributeModifier> {
        val out = HashMultimap.create<Attribute, AttributeModifier>()
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            out.put(HexAttributes.AMBIT_RADIUS, AMBIT_MOD)
            out.put(HexAttributes.SENTINEL_RADIUS, SENTINEL_MOD)
        }
        return out
    }

    // ---- 右键玩家：授予启迪成就 ----
    override fun interactLivingEntity(
        stack: ItemStack,
        player: Player,
        target: LivingEntity,
        hand: InteractionHand,
    ): InteractionResult {
        if (target is ServerPlayer && !target.level().isClientSide) {
            val advancement = target.server.advancements.getAdvancement(HexAPI.modLoc("enlightenment"))
            if (advancement != null) {
                val progress = target.advancements.getOrStartProgress(advancement)
                for (criteria in progress.remainingCriteria) {
                    target.advancements.award(advancement, criteria)
                }
                player.displayClientMessage(
                    Component.translatable("message.hexmob.everything_in_now.enlightened")
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true
                )
            }
        }
        return InteractionResult.sidedSuccess(target.level().isClientSide)
    }


    companion object {
        /** 最大媒质容量：64 个淬灵晶块的媒质（=64×QUENCHED_BLOCK_UNIT=7680 万，默认配置下等于 7680 份紫水晶粉）。 */
        const val MAX_MEDIA = MediaConstants.QUENCHED_BLOCK_UNIT * 64L

        /** 手持时施法范围加成（格）。 */
        const val AMBIT_BONUS = 16.0

        /** 手持时哨卫范围加成（格）。 */
        const val SENTINEL_BONUS = 32.0

        /** 食用恢复的饱食度点数。 */
        private const val FOOD_NUTRITION = 20

        /** 食用恢复的饱和度点数。 */
        private const val FOOD_SATURATION = 20.0F

        private val AMBIT_MOD = AttributeModifier(
            UUID.fromString("6f7f2c38-9a02-4b3e-9f1e-e0c1a35d2b12"),
            "Everything in Now ambit range", AMBIT_BONUS,
            AttributeModifier.Operation.ADDITION,
        )
        private val SENTINEL_MOD = AttributeModifier(
            UUID.fromString("a3d7a81f-6b24-4f2d-b097-86c4e02c47ae"),
            "Everything in Now sentinel range", SENTINEL_BONUS,
            AttributeModifier.Operation.ADDITION,
        )

        /** 结晶态媒质物品换算表：物品引用 -> 单份媒质值。
         *  粉/碎片/充能紫水晶的值读 hexmod 服务端配置（[at.petrak.hexcasting.api.mod.HexConfig.common]，
         *  记录在 hexcasting 的 common 配置里），淬灵晶碎片/块为固定单位。
         */
        val CRYSTALLINE_MEDIA: Map<ResourceLocation, Long> = run {
            val common = at.petrak.hexcasting.api.mod.HexConfig.common()
            mapOf(
                BuiltInRegistries.ITEM.getKey(HexItems.AMETHYST_DUST) to common.dustMediaAmount(),
                BuiltInRegistries.ITEM.getKey(Items.AMETHYST_SHARD) to common.shardMediaAmount(),
                BuiltInRegistries.ITEM.getKey(HexItems.CHARGED_AMETHYST) to common.chargedCrystalMediaAmount(),
                BuiltInRegistries.ITEM.getKey(HexItems.QUENCHED_SHARD) to MediaConstants.QUENCHED_SHARD_UNIT,
                BuiltInRegistries.ITEM.getKey(HexBlocks.QUENCHED_ALLAY.asItem()) to MediaConstants.QUENCHED_BLOCK_UNIT,
            )
        }
    }
}