package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.animal.IronGolem
import net.minecraft.world.level.Level

/**
 * 板岩傀儡（铁傀儡型）：**直接继承原版 IronGolem**——
 * 攻击动画（抬臂挥拳）由原版 IronGolemModel 读原版的 doHurtTarget 攻击 tick，零自定义。
 * 不调用 super.registerGoals() 以跳过铁傀儡自带的索敌/交互目标；只加站桩闸门/索敌/近战/游荡。
 * 主人（附近清醒的大环）醒着时解除站桩并攻击玩家。
 * 通用行为（唤醒扫描/召唤标记）来自 [GuardDormant] 接口的 GuardCore 组合实现。
 */
class GuardGolem(type: EntityType<out GuardGolem>, level: Level) : IronGolem(type, level), GuardDormant {

    override val guardCore: GuardCore = GuardCore()

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        saveGuardNbt(tag)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        loadGuardNbt(tag)
    }

    /** 守卫之间互不伤害（板岩兵/弩手/傀儡不会互打）。 */
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (isAllyAttack(source)) return false
        return super.hurt(source, amount)
    }

    /** 板岩生物免疫摔落伤害。 */
    override fun causeFallDamage(fallDistance: Float, multiplier: Float, source: DamageSource): Boolean = false

    override fun registerGoals() {
        // 不调用 super（跳过铁傀儡自带的愤怒/护村目标）
        goalSelector.addGoal(1, GuardIdleGateGoal(this) { isMasterAwake() })
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, true))
        goalSelector.addGoal(7, RandomStrollGoal(this, 0.8))
        // 索敌优先级：大环仇恨目标 > 附近玩家 > 受伤反击；全部仅唤醒时生效
        targetSelector.addGoal(1, GuardMasterHateGoal(this, awake = { isMasterAwake() }, master = { findMaster() }))
        targetSelector.addGoal(2, GuardTargetGoal(this) { isMasterAwake() })
        targetSelector.addGoal(3, GuardHurtByTargetGoal(this) { isMasterAwake() })
    }

    companion object {
        /** 板岩傀儡：铁傀儡原版属性骨架（100 血/护甲/抗击退）上做调整。 */
        fun registerAttributes(): AttributeSupplier.Builder = IronGolem.createAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.22)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 13.0)
    }
}