package pub.pigeon.yggdyy.hexmob.content.akashic_library

import at.petrak.hexcasting.common.lib.HexBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.mixin.`StrongholdPieces$LibraryAccessor`
import pub.pigeon.yggdyy.hexmob.registry.HexMobStructurePieceTypes
import pub.pigeon.yggdyy.hexmob.registry.HexMobTags

class AkashicLibraryPiece: StrongholdPieces.Library {
    constructor(genDepth: Int, random: RandomSource, box: BoundingBox, orientation: Direction): super(genDepth, random, box, orientation)
    constructor(tag: CompoundTag): super(tag)
    override fun postProcess(level: WorldGenLevel, structureManager: StructureManager, generator: ChunkGenerator, random: RandomSource, box: BoundingBox, chunkPos: ChunkPos, pos: BlockPos) {
        if(HexMob.LOGGER.isDebugEnabled) {
            HexMob.LOGGER.info("placing a stronghold akashic library at $pos")
        }
        val self: `StrongholdPieces$LibraryAccessor` = (this as `StrongholdPieces$LibraryAccessor`)
        var i = 11
        if (!self.isIsTall) {
            i = 6
        }
        this.generateBox(level, box, 0, 0, 0, 13, i - 1, 14, true, random, SMOOTH_STONE_SELECTOR)
        generateSmallDoor(level, random, box, entryDoor, 4, 1, 0)
        generateMaybeBox(level, box, random, 0.07f, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false)
        placeLower(level, random, box)
        if(self.isIsTall) {
            placeUpper(level, structureManager, generator, random, box, chunkPos, pos)
        }
    }
    private fun placeLower(level: WorldGenLevel, random: RandomSource, box: BoundingBox) {
        placeLowerPillars(level, random, box)
        placeLowerLigatures(level, box)
        placeLowerDecorations(level, box)
        placeLowerBookshelves(level)
        placeLowerChest(level, random, box)
    }
    private fun placeLowerPillars(level: WorldGenLevel, random: RandomSource, box: BoundingBox) {
        for(z in 1..13 step 4) {
            generateBox(level, box, 1, 1, z, 1, 4, z, false, random, AKASHIC_PLANKS_SELECTOR)
            generateBox(level, box, 12, 1, z, 12, 4, z, false, random, AKASHIC_PLANKS_SELECTOR)
        }
    }
    private fun placeLowerLigatures(level: WorldGenLevel, box: BoundingBox) {
        val ligature: BlockState = HexBlocks.AKASHIC_LIGATURE.defaultBlockState()
        for(z in 3..11 step 4) {
            generateBox(level, box, 1, 1, z, 1, 4, z, ligature, ligature, false)
            generateBox(level, box, 12, 1, z, 12, 4, z, ligature, ligature, false)
        }
        generateBox(level, box, 1, 0, 7, 12, 0, 7, ligature, ligature, false)
        generateBox(level, box, 1, 5, 3, 1, 5, 11, ligature, ligature, false)
        generateBox(level, box, 12, 5, 3, 12, 5, 11, ligature, ligature, false)
    }
    private fun placeLowerDecorations(level: WorldGenLevel, box: BoundingBox) {
        val light: BlockState = HexBlocks.SCONCE.defaultBlockState()
        for(z in 1..13 step 4) {
            placeBlock(level, light.setValue(BlockStateProperties.FACING, Direction.EAST), 2, 3, z, box)
            placeBlock(level, light.setValue(BlockStateProperties.FACING, Direction.WEST), 11, 3, z, box)
        }
        val seat: BlockState = HexBlocks.EDIFIED_STAIRS.defaultBlockState()
        val tablePillar: BlockState = HexBlocks.EDIFIED_FENCE.defaultBlockState()
        val tableTop: BlockState = HexBlocks.EDIFIED_PRESSURE_PLATE.defaultBlockState()
        for(x in 3..10 step 7) {
            for(z in 4..8 step 4) {
                placeBlock(level, seat.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), x, 1, z, box)
            }
            for(z in 6..10 step 4) {
                placeBlock(level, seat.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), x, 1, z, box)
            }
            for(z in 5..9 step 4) {
                placeBlock(level, tablePillar, x, 1, z, box)
                placeBlock(level, tableTop, x, 2, z, box)
            }
        }
        val logs: List<BlockState> = listOf(HexBlocks.EDIFIED_LOG_AMETHYST.defaultBlockState(), HexBlocks.EDIFIED_LOG_AVENTURINE.defaultBlockState(), HexBlocks.EDIFIED_LOG_CITRINE.defaultBlockState(), HexBlocks.EDIFIED_LOG_PURPLE.defaultBlockState())
        val leaves: List<BlockState> = listOf(HexBlocks.AMETHYST_EDIFIED_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true), HexBlocks.AVENTURINE_EDIFIED_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true), HexBlocks.CITRINE_EDIFIED_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true), HexBlocks.AMETHYST_EDIFIED_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true))
        val treeXZs: List<Pair<Int, Int>> = listOf(Pair(5, 5), Pair(8, 5), Pair(5, 9), Pair(8, 9))
        for(idx in 0..3) {
            val (x, z) = treeXZs[idx]
            for(y in 1..3) {
                placeBlock(level, logs[idx], x, y, z, box)
            }
            placeBlock(level, leaves[idx], x, 4, z, box)
        }
    }
    private fun placeLowerBookshelves(level: WorldGenLevel) {
        for(z in 2..12 step 2) {
            for(y in 1..4) {
                placeAkashicBookshelf(level, Direction.EAST, 1, y, z)
                placeAkashicBookshelf(level, Direction.WEST, 12, y, z)
            }
        }
    }
    private fun placeLowerChest(level: WorldGenLevel, random: RandomSource, box: BoundingBox) {
        for(x in 3..10 step 7) {
            for(z in 5..9 step 4) {
                createChest(level, box, random, x, 0, z, HexMob.id("chests/akashic_library"))
            }
        }
    }
    private fun placeUpper(level: WorldGenLevel, structureManager: StructureManager, generator: ChunkGenerator, random: RandomSource, box: BoundingBox, chunkPos: ChunkPos, pos: BlockPos) {
        placeUpperFloor(level, random, box)
        placeUpperPillars(level, random, box)
        placeUpperLigatures(level, box)
        placeUpperDecorations(level, box)
        placeUpperBookshelves(level)
        placeUpperChest(level, structureManager, generator, random, box, chunkPos, pos)
    }
    private fun placeUpperFloor(level: WorldGenLevel, random: RandomSource, box: BoundingBox) {
        generateBox(level, box, 1, 5, 1, 12, 5, 2, false, random, AKASHIC_PLANKS_SELECTOR)
        generateBox(level, box, 1, 5, 12, 12, 5, 13, false, random, AKASHIC_PLANKS_SELECTOR)
        generateBox(level, box, 2, 5, 3, 3, 5, 11, false, random, AKASHIC_PLANKS_SELECTOR)
        generateBox(level, box, 10, 5, 3, 11, 5, 11, false, random, AKASHIC_PLANKS_SELECTOR)
    }
    private fun placeUpperPillars(level: WorldGenLevel, random: RandomSource, box: BoundingBox) {
        for(z in 1..13 step 4) {
            generateBox(level, box, 1, 6, z, 1, 9, z, false, random, AKASHIC_PLANKS_SELECTOR)
            generateBox(level, box, 12, 6, z, 12, 9, z, false, random, AKASHIC_PLANKS_SELECTOR)
        }
    }
    private fun placeUpperLigatures(level: WorldGenLevel, box: BoundingBox) {
        val ligature: BlockState = HexBlocks.AKASHIC_LIGATURE.defaultBlockState()
        for(z in 3..11 step 4) {
            generateBox(level, box, 1, 6, z, 1, 9, z, ligature, ligature, false)
            generateBox(level, box, 12, 6, z, 12, 9, z, ligature, ligature, false)
        }
    }
    private fun placeUpperDecorations(level: WorldGenLevel, box: BoundingBox) {
        val fence: BlockState = HexBlocks.EDIFIED_FENCE.defaultBlockState()
        val ewFence: BlockState = fence.setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true)
        val nsFence: BlockState = fence.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true)
        val neFence: BlockState = fence.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.EAST, true)
        val nwFence: BlockState = fence.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.WEST, true)
        val seFence: BlockState = fence.setValue(BlockStateProperties.SOUTH, true).setValue(BlockStateProperties.EAST, true)
        val swFence: BlockState = fence.setValue(BlockStateProperties.SOUTH, true).setValue(BlockStateProperties.WEST, true)
        placeBlock(level, neFence, 3, 6, 2, box)
        placeBlock(level, nwFence, 10, 6, 2, box)
        placeBlock(level, seFence, 3, 6, 12, box)
        placeBlock(level, swFence, 10, 6, 12, box)
        generateBox(level, box, 4, 6, 2, 9, 6, 2, ewFence, ewFence, false)
        generateBox(level, box, 4, 6, 12, 9, 6, 12, ewFence, ewFence, false)
        generateBox(level, box, 3, 6, 3, 3, 6, 11, nsFence, nsFence, false)
        generateBox(level, box, 10, 6, 3, 10, 6, 11, nsFence, nsFence, false)
        val light: BlockState = HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.defaultBlockState()
        for(x in 5..8 step 3) {
            for(z in 4..10 step 3) {
                generateBox(level, box, x, 6, z, x, 7, z, light, light, false)
                generateBox(level, box, x, 8, z, x, 9, z, fence, fence, false)
            }
        }
        val ladder = Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
        generateBox(level, box, 10, 1, 13, 10, 5, 13, ladder, ladder, false)
    }
    private fun placeUpperBookshelves(level: WorldGenLevel) {
        for(z in 2..12 step 2) {
            for(y in 6..9) {
                placeAkashicBookshelf(level, Direction.EAST, 1, y, z)
                placeAkashicBookshelf(level, Direction.WEST, 12, y, z)
            }
        }
    }
    private fun placeUpperChest(level: WorldGenLevel, structureManager: StructureManager, generator: ChunkGenerator, random: RandomSource, box: BoundingBox, chunkPos: ChunkPos, pos: BlockPos) {

    }
    private fun placeAkashicBookshelf(level: WorldGenLevel, direction: Direction, x: Int, y: Int, z: Int) {
        val shelf: BlockState = HexBlocks.AKASHIC_BOOKSHELF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction).mirror(mirror).rotate(rotation)
        val pos: BlockPos = getWorldPos(x, y, z)
        level.setBlock(pos, shelf, 2)
    }
    override fun getType(): StructurePieceType {
        return HexMobStructurePieceTypes.AKASHIC_LIBRARY.get()
    }
    companion object {
        @JvmStatic
        fun createAkashicLibraryPiece(
            pieces: StructurePieceAccessor,
            random: RandomSource,
            x: Int,
            y: Int,
            z: Int,
            orientation: Direction,
            genDepth: Int
        ): AkashicLibraryPiece? {
            var boundingBox = BoundingBox.orientBox(x, y, z, -4, -1, 0, 14, 11, 15, orientation)
            if (!isOkBox(boundingBox) || pieces.findCollisionPiece(boundingBox) != null) {
                boundingBox = BoundingBox.orientBox(x, y, z, -4, -1, 0, 14, 6, 15, orientation)
                if (!isOkBox(boundingBox) || pieces.findCollisionPiece(boundingBox) != null) {
                    return null
                }
            }
            return AkashicLibraryPiece(genDepth, random, boundingBox, orientation)
        }
        class SmoothStoneSelector: BlockSelector() {
            override fun next(random: RandomSource, x: Int, y: Int, z: Int, wall: Boolean) {
                next = if (wall) {
                    val f = random.nextFloat()
                    if (f < 0.2f) {
                        Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                    } else if (f < 0.5f) {
                        Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                    } else if (f < 0.55f) {
                        Blocks.INFESTED_STONE_BRICKS.defaultBlockState()
                    } else {
                        Blocks.STONE_BRICKS.defaultBlockState()
                    }
                } else {
                    Blocks.CAVE_AIR.defaultBlockState()
                }
            }
        }
        val SMOOTH_STONE_SELECTOR = SmoothStoneSelector()
        class AkashicPlanksSelector: BlockSelector() {
            override fun next(random: RandomSource, x: Int, y: Int, z: Int, wall: Boolean) {
                next = if(wall) {
                    try {
                        val candidates = BuiltInRegistries.BLOCK.getTag(HexMobTags.BlockTags.AKASHIC_LIBRARY_WALL).get()
                        val idx = random.nextInt(candidates.size())
                        candidates[idx].value().defaultBlockState()
                    } catch (e: Exception) {
                        Blocks.CAVE_AIR.defaultBlockState()
                    }
                } else {
                    Blocks.CAVE_AIR.defaultBlockState()
                }
            }
        }
        val AKASHIC_PLANKS_SELECTOR = AkashicPlanksSelector()
    }
}