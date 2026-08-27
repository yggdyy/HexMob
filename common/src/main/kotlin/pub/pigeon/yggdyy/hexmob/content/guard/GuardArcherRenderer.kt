package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob

/** 弓箭守卫渲染器：掠夺者几何模型 + guard_archer.png（贴图可直接改），手持弩（不掉落）。 */
class GuardArcherRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<GuardArcher, GuardIllagerModel<GuardArcher>>(
        context,
        GuardIllagerModel(context.bakeLayer(ModelLayers.PILLAGER)),
        0.5F
    ) {
    init {
        addLayer(ItemInHandLayer(this, context.itemInHandRenderer))
    }

    override fun getTextureLocation(entity: GuardArcher): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE: ResourceLocation = HexMob.id("textures/entity/guard/guard_archer.png")
    }
}
