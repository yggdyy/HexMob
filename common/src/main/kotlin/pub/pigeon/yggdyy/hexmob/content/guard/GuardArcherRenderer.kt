package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.IllagerModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.IllagerRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.resources.ResourceLocation
import pub.pigeon.yggdyy.hexmob.HexMob

/** 板岩弩手渲染器：**原版掠夺者模型/渲染器**（含端弩、拉弦、庆典全部原版动画）+ 玩家贴图替换。
 * 武器渲染层（ItemInHandLayer）显式加上：实测不手动加则手持武器不渲染。 */
class GuardArcherRenderer(context: EntityRendererProvider.Context) :
    IllagerRenderer<GuardArcher>(
        context,
        IllagerModel(context.bakeLayer(ModelLayers.PILLAGER)),
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