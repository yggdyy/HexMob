package pub.pigeon.yggdyy.hexmob.content.ur_circle.renderers

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.client.render.PatternColors
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers
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
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.SlatePart

class SlateRenderer(id: ResourceLocation): PartRenderer(id) {
    override fun render(part: UrCirclePart, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, partialTick: Float, context: EntityRendererProvider.Context
    ) {
        val slate: SlatePart = part as? SlatePart ?: return
        val model: BakedModel = context.modelManager.getModel(ModelResourceLocation(HexAPI.modLoc("slate"), "energized=true,face=wall,facing=south,waterlogged=false"))
        poseStack.pushPose()
        poseStack.translate(-slate.bbWidth / 2.0, -slate.bbHeight / 2.0, 0.0)
        poseStack.scale(slate.bbWidth, slate.bbHeight, slate.bbWidth)
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), Blocks.AIR.defaultBlockState(), model, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY)
        poseStack.translate(0.0, 0.0, 0.063)
        PatternRenderer.renderPattern(slate.pattern, poseStack, WorldlyPatternRenderHelpers.WORLDLY_SETTINGS, PatternColors.SLATE_WOBBLY_PURPLE_COLOR, 0.0, 512)
        poseStack.popPose()
    }
}