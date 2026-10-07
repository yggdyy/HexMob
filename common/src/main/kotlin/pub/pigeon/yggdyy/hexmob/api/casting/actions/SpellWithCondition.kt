package pub.pigeon.yggdyy.hexmob.api.casting.actions

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap

/**
 * 带使用条件的施法动作模板：
 * - 解析阶段：不满足 [canUse] 时抛 [makeMishap]（可携带 args 定位 iota）；
 * - 通过后成绩扣费并返回 [RenderedSpell]，副作用统一放 [cast]（释放阶段执行）。
 * 注意：execute 解析出的参数在 cast 阶段不可直接取用——子类若需要在释放阶段用到
 * 解析期数据（实体/方块/图案等），应把数据捕获进 data class（参考
 * OpTransformStimulatedPatternSpell.Spell），而不是依赖本类字段。
 */
abstract class SpellWithCondition : SpellAction {

    /** 参数个数（SpellAction 抽象成员，子类必须声明）。 */
    abstract override val argc: Int

    /** 施放条件；解析阶段判定，不满足时抛 [makeMishap]。 */
    abstract fun canUse(env: CastingEnvironment): Boolean

    /** 条件不满足时抛出的事故（可带 args 定位 iota）。 */
    abstract fun makeMishap(args: List<Iota>, env: CastingEnvironment): Mishap

    /**
 * 实际施放（释放阶段执行，副作用放这里）。
 *
 * 无参数法术覆写它；需要解析期参数（实体/位置等）的法术请忽略本方法，
 * 改覆写 [makeEffect] 返回捕获数据的 data class（否则有参法术这里无意义）。
 */
open fun cast(env: CastingEnvironment) {}

    /** 媒质消耗。 */
    abstract fun cost(args: List<Iota>, env: CastingEnvironment): Long

    /** 施放粒子（默认无）。 */
    open fun particle(args: List<Iota>, env: CastingEnvironment): List<ParticleSpray> = emptyList()

    /** 操作计数（默认 1，多跳复杂操作可覆写）。 */
    open fun opcount(args: List<Iota>, env: CastingEnvironment): Long = 1

    /**
     * 构建释放阶段执行的 RenderedSpell（默认委托给 [cast]）。
     *
     * 若释放期需要用到解析期参数（目标实体/位置等），**不要**存进实例字段——Action 是
     * 注册表单例、一次施法共享，并发/重入会互相污染。请覆写此方法，返回把参数捕获进
     * data class 的 RenderedSpell（参考 OpTransformStimulatedPatternSpell.Spell 的写法）。
     */
    open fun makeEffect(args: List<Iota>, env: CastingEnvironment): RenderedSpell =
        object : RenderedSpell {
            override fun cast(env: CastingEnvironment) {
                // 匿名对象内必须显式 this@ 引用外部实例，否则会递归调用匿名对象自身
                this@SpellWithCondition.cast(env)
            }
        }

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (!canUse(env)) throw makeMishap(args, env)
        return SpellAction.Result(
            effect = makeEffect(args, env),
            cost = cost(args, env),
            particles = particle(args, env),
            opCount = opcount(args, env),
        )
    }
}