package pub.pigeon.yggdyy.hexmob.content.ur_circle

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
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
            val deltaP: Vec3 = p.subtract(entity.xo, entity.yo, entity.zo)
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