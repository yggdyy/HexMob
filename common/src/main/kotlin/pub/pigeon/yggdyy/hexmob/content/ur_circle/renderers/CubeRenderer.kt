package pub.pigeon.yggdyy.hexmob.content.ur_circle.renderers

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import pub.pigeon.yggdyy.hexmob.content.ur_circle.PartRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCirclePart
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.CubePart

class CubeRenderer(id: ResourceLocation): PartRenderer(id) {
    override fun render(part: UrCirclePart, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, partialTick: Float, context: EntityRendererProvider.Context) {
        val cube: CubePart = part as? CubePart ?: return
        val model: BakedModel = context.modelManager.getModel(ModelResourceLocation(cube.modelId, cube.modelVariant))
        poseStack.pushPose()
        poseStack.translate(-cube.bbWidth / 2.0, -cube.bbHeight / 2.0, -cube.bbWidth / 2.0)
        poseStack.scale(cube.bbWidth, cube.bbHeight, cube.bbWidth)
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), Blocks.AIR.defaultBlockState(), model, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY)
        poseStack.popPose()
    }
}