package pub.pigeon.yggdyy.hexmob.api.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.DyeColor

/**
 * 当一个 `EntityIota` 指向 [pub.pigeon.yggdyy.hexmob.api.entity.FlickeringEntity]
 * 且其引用正处于"飘忽"窗口时抛出的事故。
 *
 * 文本与效果均可定制：子类覆写 [flickerMessage]（文案）与
 * [flickerEffect]（效果）；不同 Boss 可以给玩家不同的"拒绝"体验
 * （例如大环：3 秒失明 + "接受了自然本身"）。
 */
open class MishapFlickeringEntity(val entity: Entity) : Mishap() {

    /** 事故文案；默认通用提示，子类可覆写。 */
    open fun flickerMessage(): Component =
        Component.translatable("mishap.hexmob.flickering_entity")

    /** 事故效果；默认无额外效果，子类可覆写（如施加失明等）。 */
    open fun flickerEffect(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // no-op by default
    }

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.PURPLE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        flickerEffect(env, errorCtx, stack)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        flickerMessage()
}
