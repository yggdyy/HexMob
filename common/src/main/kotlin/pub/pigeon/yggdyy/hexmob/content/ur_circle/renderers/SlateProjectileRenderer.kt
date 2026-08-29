package pub.pigeon.yggdyy.hexmob.content.ur_circle.renderers

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.client.render.PatternColors
import at.petrak.hexcasting.client.render.PatternRenderer
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import org.joml.Quaternionf
import org.joml.Vector3f
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.SlateProjectile

/** 石板弹渲染：小号石板 + 随携带图案绘制，朝向飞行方向。 */
class SlateProjectileRenderer(val context: EntityRendererProvider.Context) : EntityRenderer<SlateProjectile>(context) {
    override fun getTextureLocation(entity: SlateProjectile): ResourceLocation = HexMob.id("null")

    override fun render(entity: SlateProjectile, entityYaw: Float, partialTick: Float, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
        val model = context.modelManager.getModel(ModelResourceLocation(HexAPI.modLoc("slate"), "energized=true,face=wall,facing=south,waterlogged=false"))
        poseStack.pushPose()
        poseStack.scale(0.6F, 0.6F, 0.6F)
        // 让石板正对飞行方向
        val v = entity.deltaMovement
        if (v.lengthSqr() > 1.0E-7) {
            poseStack.mulPose(Quaternionf().rotateTo(Vector3f(0F, 0F, 1F), v.normalize().toVector3f()))
        }
        poseStack.translate(-0.5, -0.5, 0.0)
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), Blocks.AIR.defaultBlockState(), model, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY)
        poseStack.translate(0.0, 0.0, 0.063)
        PatternRenderer.renderPattern(entity.pattern, poseStack, WorldlyPatternRenderHelpers.WORLDLY_SETTINGS, PatternColors.SLATE_WOBBLY_PURPLE_COLOR, 0.0, 512)
        poseStack.popPose()
    }
}
