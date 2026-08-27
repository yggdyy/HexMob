package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob

/** 傀儡守卫渲染器：铁傀儡几何模型 + guard_golem.png（贴图可直接改）。 */
class GuardGolemRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GuardGolem, GuardGolemModel<GuardGolem>>(
        context,
        GuardGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)),
        0.7F
    ) {
    override fun getTextureLocation(entity: GuardGolem): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE: ResourceLocation = HexMob.id("textures/entity/guard/guard_golem.png")
    }
}
