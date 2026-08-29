package pub.pigeon.yggdyy.hexmob.content.ur_circle

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.ur_circle.renderers.CubeRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.renderers.SlateRenderer

class UrCircleRenderer(val context: EntityRendererProvider.Context) : EntityRenderer<UrCircleEntity>(context) {
    override fun getTextureLocation(entity: UrCircleEntity): ResourceLocation = HexMob.id("null")
    override fun render(entity: UrCircleEntity, entityYaw: Float, partialTick: Float, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight)
        val parts: List<UrCirclePart> = entity.getAllParts()
        for(part in parts) {
            if(!part.shouldRenderAsPart()) {
                continue
            }
            val p: Vec3 = part.posPrev.lerp(part.posNow, partialTick.toDouble())
            // PoseStack 的基点是实体的"插值位置"，这里也必须减插值位置；若减上一 tick 的原始位置(xo,yo,zo)，
            // 移动时整个结构会多出 movement*partialTick 的每帧偏移，表现为大环发抖。
            val deltaP: Vec3 = p.subtract(
                Mth.lerp(partialTick.toDouble(), entity.xo, entity.x),
                Mth.lerp(partialTick.toDouble(), entity.yo, entity.y),
                Mth.lerp(partialTick.toDouble(), entity.zo, entity.z)
            )
            val d: Vec3 = part.dirPrev.lerp(part.dirNow, partialTick.toDouble())
            poseStack.pushPose()
            poseStack.translate(deltaP.x, deltaP.y, deltaP.z)
            poseStack.mulPose(Quaternionf().rotateTo(Vector3f(0F, 0F, 1F), d.toVector3f()))
            for(renderer in renderers) {
                if(renderer.id == part.id) {
                    renderer.render(part, poseStack, buffer, packedLight, partialTick, context)
                    break
                }
            }
            poseStack.popPose()
        }
    }
    companion object {
        val renderers: MutableList<PartRenderer> = mutableListOf(
            CubeRenderer(HexMob.id("cube")),
            SlateRenderer(HexMob.id("slate"))
        )
    }
}