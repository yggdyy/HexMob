package pub.pigeon.yggdyy.hexmob.content.ur_circle

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import pub.pigeon.yggdyy.hexmob.api.mishap.MishapFlickeringEntity

/**
 * 大环专属的"拒绝引用"事故：试图用大环的 EntityIota 施法时，在飘忽窗口内
 * 会给施法者 3 秒失明——仿佛你抓住的不是实体，而是自然本身。
 */
class UrCircleFlickerMishap(entity: Entity) : MishapFlickeringEntity(entity) {

    override fun flickerMessage(): Component =
        Component.translatable("mishap.hexmob.ur_circle_flickering")

    override fun flickerEffect(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val caster = env.castingEntity
        if (caster is LivingEntity) {
            caster.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 60)) // 3 秒失明
        }
    }
}
