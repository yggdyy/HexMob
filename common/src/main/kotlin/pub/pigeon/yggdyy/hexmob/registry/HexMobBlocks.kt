package pub.pigeon.yggdyy.hexmob.registry

import at.petrak.hexcasting.common.lib.HexBlocks
import com.mojang.datafixers.DSL
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.DeferredSupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.akashic_library.AmethystInfestedBlock
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateBlock
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateBlockEntity
import pub.pigeon.yggdyy.hexmob.content.stimulated_pattern.StimulatedSlateItem

object HexMobBlocks {
    fun init() {
        BLOCKS.register()
        TYPES.register()
        ITEMS.register()
    }
    private val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(HexMob.MODID, Registries.BLOCK)
    private val TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(HexMob.MODID, Registries.BLOCK_ENTITY_TYPE)
    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(HexMob.MODID, Registries.ITEM)
    val STIMULATED_SLATE_BLOCK: DeferredSupplier<StimulatedSlateBlock> = BLOCKS.register("stimulated_slate") { StimulatedSlateBlock(BlockBehaviour.Properties.copy(HexBlocks.SLATE_BLOCK)) }
    val STIMULATED_SLATE_TILE: DeferredSupplier<BlockEntityType<StimulatedSlateBlockEntity>> = TYPES.register("stimulated_slate") { BlockEntityType.Builder.of({pos, state -> StimulatedSlateBlockEntity(pos, state)}, STIMULATED_SLATE_BLOCK.get()).build(DSL.remainderType()) }
    val STIMULATED_SLATE_ITEM: DeferredSupplier<StimulatedSlateItem> = ITEMS.register("stimulated_slate") {StimulatedSlateItem(STIMULATED_SLATE_BLOCK.get(), Item.Properties())}
    val INFESTED_EDIFIED_PLANKS_BLOCK: DeferredSupplier<AmethystInfestedBlock> = BLOCKS.register("infested_edified_planks") { AmethystInfestedBlock(BlockBehaviour.Properties.copy(HexBlocks.EDIFIED_PLANKS)) }
    val INFESTED_EDIFIED_PLANKS_ITEM: DeferredSupplier<BlockItem> = ITEMS.register("infested_edified_planks") { BlockItem(INFESTED_EDIFIED_PLANKS_BLOCK.get(), Item.Properties()) }
    val INFESTED_EDIFIED_PANEL_BLOCK: DeferredSupplier<AmethystInfestedBlock> = BLOCKS.register("infested_edified_panel") { AmethystInfestedBlock(BlockBehaviour.Properties.copy(HexBlocks.EDIFIED_PANEL)) }
    val INFESTED_EDIFIED_PANEL_ITEM: DeferredSupplier<BlockItem> = ITEMS.register("infested_edified_panel") { BlockItem(INFESTED_EDIFIED_PANEL_BLOCK.get(), Item.Properties()) }
    val INFESTED_EDIFIED_TILE_BLOCK: DeferredSupplier<AmethystInfestedBlock> = BLOCKS.register("infested_edified_tile") { AmethystInfestedBlock(BlockBehaviour.Properties.copy(HexBlocks.EDIFIED_TILE)) }
    val INFESTED_EDIFIED_TILE_ITEM: DeferredSupplier<BlockItem> = ITEMS.register("infested_edified_tile") { BlockItem(INFESTED_EDIFIED_TILE_BLOCK.get(), Item.Properties()) }
}