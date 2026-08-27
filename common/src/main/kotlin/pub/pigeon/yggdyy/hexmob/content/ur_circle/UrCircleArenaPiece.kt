package pub.pigeon.yggdyy.hexmob.content.ur_circle

import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.StructurePiece
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities
import pub.pigeon.yggdyy.hexmob.registry.HexMobStructurePieceTypes

/**
 * ur_circle 祭坛结构块：放模板方块 + 实体。
 *
 * 实体（沉睡的大环）由模板自带（nbt 资源副本里已给实体加了 hexmob:Dormant 标记），
 * 走 StructureTemplate.placeInWorld 的实体放置逻辑（默认 ignoreEntities=false 会放实体），
 * 因此这里不需要手动读 entityInfoList（1.20.1 里它是 private）。
 */
class UrCircleArenaPiece private constructor(box: BoundingBox) :
    StructurePiece(HexMobStructurePieceTypes.UR_CIRCLE_ARENA.get(), 0, box) {

    private var templateId = ResourceLocation("hexmob", "ur_circle")
    private var rotation = Rotation.NONE
    private var pos = BlockPos.ZERO

    constructor(templateManager: StructureTemplateManager, templateId: ResourceLocation, pos: BlockPos, rotation: Rotation) :
        this(makeBox(templateManager, templateId, pos, rotation)) {
        this.templateId = templateId
        this.rotation = rotation
        this.pos = pos
    }

    constructor(context: StructurePieceSerializationContext, tag: CompoundTag) :
        this(makeBox(context.structureTemplateManager(), ResourceLocation(tag.getString("Template")), loadPos(tag), loadRotation(tag))) {
        templateId = ResourceLocation(tag.getString("Template"))
        rotation = loadRotation(tag)
        pos = loadPos(tag)
    }

    override fun addAdditionalSaveData(context: StructurePieceSerializationContext, tag: CompoundTag) {
        tag.putString("Template", templateId.toString())
        tag.putString("Rotation", rotation.name)
        tag.putInt("TPosX", pos.x)
        tag.putInt("TPosY", pos.y)
        tag.putInt("TPosZ", pos.z)
    }

    override fun postProcess(
        level: WorldGenLevel,
        structureManager: StructureManager,
        generator: ChunkGenerator,
        random: RandomSource,
        box: BoundingBox,
        chunkPos: ChunkPos,
        pos: BlockPos
    ) {
        val server = (level.level as? net.minecraft.server.level.ServerLevel)?.server ?: return
        val template = server.structureManager.get(templateId).orElse(null) ?: return
        val settings = StructurePlaceSettings()
            .setRotation(rotation)
            .setMirror(Mirror.NONE)
            .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
            .setIgnoreEntities(true) // 模板方块照放，实体不放：模板里有两只大环，代码只放一只
        template.placeInWorld(level, pos, pos, settings, random, 2)
        // 老板只在 piece 原点所在 chunk 生成一次：StructurePiece.postProcess 会对每个
        // 与 box 相交的 chunk 各调用一次，祭坛 11×11 横跨 2×2 chunk，不加门控会叠出 4 只。
        if (chunkPos.x == SectionPos.blockToSectionCoord(pos.x) && chunkPos.z == SectionPos.blockToSectionCoord(pos.z)) {
            spawnArenaBoss(level)
        }
    }

    /**
     * 祭坛老板：代码生成一只**沉睡**的大环，放在清空区正中。
     * - 模板 nbt 原样保留两只，但 placeInWorld 已 ignoreEntities，这里只放一只；
     * - setDormant(true) 先于 addFreshEntity：实体被 ProtoChunk 序列化暂存时
     *   会把 hexmob:Dormant 写进 NBT，实体化后天生沉睡；
     * - UrCircleEntity 实体化后的首个 tick 还有 checkArenaDormancy 兜底自查。
     */
    private fun spawnArenaBoss(level: WorldGenLevel) {
        val boss = UrCircleEntity(HexMobEntities.UR_CIRCLE.get(), level.level)
        boss.setPos(pos.x + 5.0, pos.y + 3.0, pos.z + 5.0)
        boss.setDormant(true)
        level.addFreshEntityWithPassengers(boss)
    }

    override fun getType(): StructurePieceType = HexMobStructurePieceTypes.UR_CIRCLE_ARENA.get()

    companion object {
        private fun loadRotation(tag: CompoundTag): Rotation = Rotation.valueOf(tag.getString("Rotation"))
        private fun loadPos(tag: CompoundTag): BlockPos = BlockPos(tag.getInt("TPosX"), tag.getInt("TPosY"), tag.getInt("TPosZ"))

        private fun makeBox(templateManager: StructureTemplateManager, templateId: ResourceLocation, pos: BlockPos, rotation: Rotation): BoundingBox {
            val template = templateManager.get(templateId).orElse(null)
            val size = template?.getSize(rotation) ?: return BoundingBox.fromCorners(pos, pos)
            return BoundingBox.fromCorners(pos, pos.offset(size.x - 1, size.y - 1, size.z - 1))
        }
    }
}
