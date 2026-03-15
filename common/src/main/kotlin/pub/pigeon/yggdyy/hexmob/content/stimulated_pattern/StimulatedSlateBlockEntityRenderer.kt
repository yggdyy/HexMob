package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers
import at.petrak.hexcasting.common.blocks.circles.BlockSlate
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Vec3i
import net.minecraft.world.level.block.state.properties.AttachFace
import net.minecraft.world.phys.Vec3

class StimulatedSlateBlockEntityRenderer(val context: BlockEntityRendererProvider.Context): BlockEntityRenderer<StimulatedSlateBlockEntity> {
    override fun render(
        tile: StimulatedSlateBlockEntity,
        pPartialTick: Float,
        ps: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val pattern: HexPattern = tile.pattern ?: return
        val bs = tile.blockState
        val isOnWall = bs.getValue(BlockSlate.ATTACH_FACE) == AttachFace.WALL
        val isOnCeiling = bs.getValue(BlockSlate.ATTACH_FACE) == AttachFace.CEILING
        val facing = bs.getValue(BlockSlate.FACING).get2DDataValue()
        val wombly = bs.getValue(BlockSlate.ENERGIZED)
        ps.pushPose()
        var normal: Vec3? = null
        if (isOnWall) {
            ps.mulPose(Axis.ZP.rotationDegrees(180f))
            val tV = SLATE_FACINGS[facing % 4]
            ps.translate(tV.x.toFloat(), tV.y.toFloat(), tV.z.toFloat())
            ps.mulPose(Axis.YP.rotationDegrees(WALL_ROTATIONS[facing % 4].toFloat()))
            normal = WALL_NORMALS[facing % 4]
        } else {
            val tV = SLATE_FLOORCEIL_FACINGS[facing % 4]
            ps.translate(tV.x.toFloat(), tV.y.toFloat(), tV.z.toFloat())
            ps.mulPose(Axis.YP.rotationDegrees((facing * -90).toFloat()))
            ps.mulPose(Axis.XP.rotationDegrees((90 * if (isOnCeiling) -1 else 1).toFloat()))
            if (isOnCeiling) ps.translate(0f, -1f, 1f)
        }
        WorldlyPatternRenderHelpers.renderPattern(
            pattern,
            if (wombly) WorldlyPatternRenderHelpers.WORLDLY_SETTINGS_WOBBLY else WorldlyPatternRenderHelpers.WORLDLY_SETTINGS,
            StimulatedPatternEntityRenderer.COLOR,
            tile.blockPos.hashCode().toDouble(), ps, buffer, normal, null, light, 1
        )
        ps.popPose()
    }
    companion object {
        private val WALL_ROTATIONS = intArrayOf(180, 270, 0, 90)
        private val SLATE_FACINGS = arrayOf(Vec3i(0, -1, 0), Vec3i(-1, -1, 0), Vec3i(-1, -1, 1), Vec3i(0, -1, 1))
        private val WALL_NORMALS =
            arrayOf(Vec3(0.0, 0.0, -1.0), Vec3(-1.0, 0.0, 0.0), Vec3(0.0, 0.0, -1.0), Vec3(-1.0, 0.0, 0.0))
        private val SLATE_FLOORCEIL_FACINGS = arrayOf(Vec3i(0, 0, 0), Vec3i(1, 0, 0), Vec3i(1, 0, 1), Vec3i(0, 0, 1))
    }
}