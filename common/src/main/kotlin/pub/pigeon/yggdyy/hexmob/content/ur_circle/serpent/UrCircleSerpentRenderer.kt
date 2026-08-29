package pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent

import at.petrak.hexcasting.common.lib.HexBlocks
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * 长蛇渲染器：把身体拖尾每一节渲染成一个石板块（蛇头稍大）。
 * 蛇身在客户端本地确定性构建（与服务端一致），无需额外同步。
 */
class UrCircleSerpentRenderer(val context: EntityRendererProvider.Context) : EntityRenderer<UrCircleSerpent>(context) {
    override fun getTextureLocation(entity: UrCircleSerpent): ResourceLocation = HexMob.id("null")

    override fun render(
        entity: UrCircleSerpent, entityYaw: Float, partialTick: Float,
        poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
        val interp = Vec3(
            Mth.lerp(partialTick.toDouble(), entity.xo, entity.x),
            Mth.lerp(partialTick.toDouble(), entity.yo, entity.y),
            Mth.lerp(partialTick.toDouble(), entity.zo, entity.z)
        )
        val state = HexBlocks.SLATE_BLOCK.defaultBlockState()
        val br = Minecraft.getInstance().blockRenderer
        for (i in entity.trail.indices) {
            val rel = entity.trail[i].subtract(interp)
            poseStack.pushPose()
            // renderSingleBlock 以原点绘制 0..1 单位立方体 → 平移到节中心
            poseStack.translate(rel.x - 0.5, rel.y - 0.5, rel.z - 0.5)
            val s = if (i == 0) 1.15F else 0.85F
            poseStack.scale(s, s, s)
            br.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY)
            poseStack.popPose()
        }
    }
}
