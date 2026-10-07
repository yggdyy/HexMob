package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Matrix4f
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * 【核心光束渲染器】把 ur_beam 法术的光束画成末影水晶治疗龙时的链接光束观感。
 * 复刻原版 [net.minecraft.client.renderer.entity.EnderDragonRenderer.renderCrystalBeams]
 * 的绘制（8 段绕轴光柱、近端收窄、UV 随 tickCount 滚动），但纹理换成本项目
 * assets/hexmob/textures/entity/ur_core_beam.png（用户可改）。
 *
 * 实体位置即光束起点（施法者眼睛），beamTarget 同步到目标身体中心 → 传相对偏移即可。
 */
class UrCoreBeamRenderer(context: EntityRendererProvider.Context) : EntityRenderer<UrCoreBeamEntity>(context) {
    override fun getTextureLocation(entity: UrCoreBeamEntity): ResourceLocation = TEXTURE

    override fun render(
        entity: UrCoreBeamEntity, entityYaw: Float, partialTick: Float,
        poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int
    ) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
        val target = entity.beamTarget() ?: return
        // 起点用插值位置（跟随施法者眼睛移动），终点直接取同步的目标点
        val ox = Mth.lerp(partialTick.toDouble(), entity.xo, entity.x)
        val oy = Mth.lerp(partialTick.toDouble(), entity.yo, entity.y)
        val oz = Mth.lerp(partialTick.toDouble(), entity.zo, entity.z)
        val rel = target.subtract(Vec3(ox, oy, oz))
        renderCrystalBeams(
            rel.x.toFloat(), rel.y.toFloat(), rel.z.toFloat(),
            partialTick, entity.tickCount, poseStack, buffer, packedLight
        )
    }

    /**
     * 原版 renderCrystalBeams 的复刻（去掉末影水晶的 (0,2,0) 顶部偏移——我们是施法者胸前发射）：
     * pose 起点即实体位置，按相对偏移 (x,y,z) 旋转，画 8 段半径 0.75 的绕轴光柱，
     * 长度=g，近端 x0.2 收窄、半透明。
     */
    private fun renderCrystalBeams(
        x: Float, y: Float, z: Float, partialTick: Float, tickCount: Int,
        poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int
    ) {
        val f = Mth.sqrt(x * x + z * z) // 水平距离
        val g = Mth.sqrt(x * x + y * y + z * z) // 光束全长
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotation(-Math.atan2(z.toDouble(), x.toDouble()).toFloat() - 1.5707964F))
        poseStack.mulPose(Axis.XP.rotation(-Math.atan2(f.toDouble(), y.toDouble()).toFloat() - 1.5707964F))

        val consumer = buffer.getBuffer(BEAM)
        val uStart = 0.0F - (tickCount + partialTick) * 0.01F
        val uEnd = g / 32.0F - (tickCount + partialTick) * 0.01F

        val pose = poseStack.last()
        val matrix4f: Matrix4f = pose.pose()
        val matrix3f: Matrix3f = pose.normal()
        var prevX = 0.0F
        var prevZ = 0.75F
        var prevU = 0.0F
        for (i in 1..8) {
            val rad = i * 6.2831855F / 8.0F
            val x1 = Mth.sin(rad) * 0.75F
            val z1 = Mth.cos(rad) * 0.75F
            val u = i / 8.0F
            // 近端（起点）半透明收窄
            consumer.vertex(matrix4f, prevX * 0.2F, prevZ * 0.2F, 0.0F)
                .color(0, 0, 0, 255).uv(prevU, uStart)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex()
            consumer.vertex(matrix4f, prevX, prevZ, g)
                .color(255, 255, 255, 255).uv(prevU, uEnd)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex()
            consumer.vertex(matrix4f, x1, z1, g)
                .color(255, 255, 255, 255).uv(u, uEnd)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex()
            consumer.vertex(matrix4f, x1 * 0.2F, z1 * 0.2F, 0.0F)
                .color(0, 0, 0, 255).uv(u, uStart)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex()
            prevX = x1
            prevZ = z1
            prevU = u
        }
        poseStack.popPose()
    }

    companion object {
        private val TEXTURE: ResourceLocation = HexMob.id("textures/entity/ur_core_beam.png")
        private val BEAM: RenderType = RenderType.entitySmoothCutout(TEXTURE)
    }
}
