package pub.pigeon.yggdyy.hexmob.content.ur_circle.spells

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.damagesource.DamageSource
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity

/**
 * 【召唤下属】Summon Servants——大环召唤一批仆从护卫自己：
 * 恼鬼式飞行近战下属 40% + 弓箭守卫/斧头守卫/傀儡守卫 各 20%（守卫计入召唤预算）。
 * 仅当血量 >90% 且存活下属少于上限时可吟唱。
 *
 * 配合保护机制：下属存活期间大环停转/不动/无敌（见 UrCircleEntity.isProtected），
 * 玩家必须先清完下属才能对大环造成伤害。
 *
 * **可打断**（阈值 [INTERRUPT_THRESHOLD]=20）：打断召唤可以阻止护盾补员。
 */
class SummonServantSkill : UrCircleSkill("summon_servants", 70) {

    override fun canUse(circle: UrCircleEntity): Boolean {
        if (circle.level().isClientSide) return false
        return circle.health / circle.maxHealth > 0.7F && circle.livingServants() < MAX_SERVANTS
    }

    override val weight: Int = 3
    override val channelInterruptible: Boolean = true

    override fun channelParticleType(circle: UrCircleEntity) = ParticleTypes.END_ROD
    override fun channelParticlesPerPulse(circle: UrCircleEntity): Int = 12

    // 受伤控制：全额承伤 + 仅 20+ 打断
    override fun onChannelHurt(circle: UrCircleEntity, source: DamageSource, amount: Float): Boolean {
        if (amount > 0.0F) {
            circle.applyChannelDamage(source, amount)
        }
        if (amount >= INTERRUPT_THRESHOLD) {
            circle.interruptChannel()
        }
        return true
    }

    override fun cast(circle: UrCircleEntity) {
        if (circle.level().isClientSide) return
        val missing = MAX_SERVANTS - circle.livingServants()
        circle.summonPets(missing)
    }

    companion object {
        const val MAX_SERVANTS = 8
        /** 打断阈值：单次原始伤害 ≥20 才打断。 */
        const val INTERRUPT_THRESHOLD = 20.0F
    }
}
