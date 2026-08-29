package pub.pigeon.yggdyy.hexmob.content.ur_circle.servant

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.VexRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.monster.Vex
import pub.pigeon.yggdyy.hexmob.HexMob

/**
 * 下属渲染器：沿用原版恼鬼的模型与动画，只把材质换成 hexmob 自己的
 * （textures/entity/ur_circle_servant.png，可在资源目录里直接改）。
 * 冲刺（charging）时切另一张贴图。整体放大 1.5 倍以配合加大的体积。
 */
class UrCircleServantRenderer(context: EntityRendererProvider.Context) : VexRenderer(context) {
    override fun getTextureLocation(entity: Vex): ResourceLocation =
        if (entity.isCharging()) HexMob.id("textures/entity/ur_circle_servant_charging.png")
        else HexMob.id("textures/entity/ur_circle_servant.png")

    override fun scale(entity: Vex, poseStack: PoseStack, partialTick: Float) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE)
    }

    companion object {
        const val MODEL_SCALE = 1.5F
    }
}
