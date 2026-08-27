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
import pub.pigeon.yggdyy.hexmob.content.ur_circle.CircleState
import pub.pigeon.yggdyy.hexmob.content.ur_circle.PartRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCirclePart
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.SlatePart

class SlateRenderer(id: ResourceLocation): PartRenderer(id) {
    override fun render(part: UrCirclePart, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, partialTick: Float, context: EntityRendererProvider.Context
    ) {
        val slate: SlatePart = part as? SlatePart ?: return
        val circle = slate.parentMob
        // 石板在 ecliptic（外圈），全局序号 = 14 + 圈内序号
        val gidx = 14 + circle.ecliptic.indexOf(slate)
        var lit = true
        if (circle.circleState == CircleState.DYING) {
            if (gidx >= 14 && circle.stateTicks >= circle.deathVanishTick(gidx)) return // 已消失
            lit = circle.stateTicks < circle.deathExtinguishTick(gidx) // 熄灭=深色模型+图案熄灭
        }
        val energized = if (lit) "energized=true" else "energized=false"
        val model: BakedModel = context.modelManager.getModel(ModelResourceLocation(HexAPI.modLoc("slate"), "$energized,face=wall,facing=south,waterlogged=false"))
        poseStack.pushPose()
        // 环刃风暴 + 服务端配置体积倍率：石板体积随总放大倍率膨胀（绕中心缩放）
        val s = circle.totalScale()
        val w = slate.bbWidth * s
        val h = slate.bbHeight * s
        poseStack.translate(-w / 2.0, -h / 2.0, 0.0)
        poseStack.scale(w, h, w)
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), Blocks.AIR.defaultBlockState(), model, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY)
        if (lit) {
            poseStack.translate(0.0, 0.0, 0.063)
            PatternRenderer.renderPattern(slate.pattern, poseStack, WorldlyPatternRenderHelpers.WORLDLY_SETTINGS, PatternColors.SLATE_WOBBLY_PURPLE_COLOR, 0.0, 512)
        }
        poseStack.popPose()
    }
}