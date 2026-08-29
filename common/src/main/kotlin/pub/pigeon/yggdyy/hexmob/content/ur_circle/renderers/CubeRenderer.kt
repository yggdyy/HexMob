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
import pub.pigeon.yggdyy.hexmob.content.ur_circle.CircleState
import pub.pigeon.yggdyy.hexmob.content.ur_circle.PartRenderer
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCirclePart
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.CubePart

class CubeRenderer(id: ResourceLocation): PartRenderer(id) {
    override fun render(part: UrCirclePart, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, partialTick: Float, context: EntityRendererProvider.Context) {
        val cube: CubePart = part as? CubePart ?: return
        val circle = cube.parentMob
        // 死亡演出：促动石依次消失（跳过渲染）；核心在 CORE_BREAK_TICK 后被摧毁（跳过）
        if (circle.circleState == CircleState.DYING) {
            val idx = circle.equator.indexOf(cube)
            if (idx >= 0) {
                if (circle.stateTicks >= circle.deathVanishTick(idx)) return
            } else if (circle.stateTicks >= UrCircleEntity.CORE_BREAK_TICK) {
                return
            }
        }
        val model: BakedModel = context.modelManager.getModel(ModelResourceLocation(cube.modelId, effectiveVariant(cube)))
        poseStack.pushPose()
        // 环刃风暴 + 服务端配置体积倍率：部件体积随总放大倍率膨胀（绕中心缩放）
        val s = circle.totalScale()
        val w = cube.bbWidth * s
        val h = cube.bbHeight * s
        poseStack.translate(-w / 2.0, -h / 2.0, -w / 2.0)
        poseStack.scale(w, h, w)
        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), Blocks.AIR.defaultBlockState(), model, 1F, 1F, 1F, packedLight, OverlayTexture.NO_OVERLAY)
        poseStack.popPose()
    }

    /** 促动石明灭：吟唱时 energized 状态沿环流动；死亡演出时逐个熄灭。 */
    private fun effectiveVariant(cube: CubePart): String {
        val circle = cube.parentMob
        // 死亡演出：促动石（内圈 equator）逐个熄灭；核心保持点亮到终幕
        if (circle.circleState == CircleState.DYING) {
            val idx = circle.equator.indexOf(cube)
            if (idx >= 0) {
                val lit = circle.stateTicks < circle.deathExtinguishTick(idx)
                return cube.modelVariant.replace(Regex("energized=(true|false)"), if (lit) "energized=true" else "energized=false")
            }
            return cube.modelVariant
        }
        // 光炮吟唱自发光：所有促动石 energized=true 全亮
        if (circle.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)) {
            return cube.modelVariant.replace(Regex("energized=(true|false)"), "energized=true")
        }
        if (circle.circleState == CircleState.CHANNELING) {
            val idx = circle.equator.indexOf(cube)
            val on = (idx + circle.stateTicks / 2) % 2 == 0
            return cube.modelVariant.replace(Regex("energized=(true|false)"), if (on) "energized=true" else "energized=false")
        }
        return cube.modelVariant
    }
}