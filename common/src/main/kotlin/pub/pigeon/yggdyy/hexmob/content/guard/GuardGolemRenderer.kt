package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.IronGolemModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob

/** 板岩傀儡渲染器：**原版铁傀儡模型/渲染器**（含挥拳下砸动画）+ 玩家贴图替换。 */
class GuardGolemRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GuardGolem, IronGolemModel<GuardGolem>>(
        context,
        IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)),
        0.7F
    ) {
    override fun getTextureLocation(entity: GuardGolem): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE: ResourceLocation = HexMob.id("textures/entity/guard/guard_golem.png")
    }
}