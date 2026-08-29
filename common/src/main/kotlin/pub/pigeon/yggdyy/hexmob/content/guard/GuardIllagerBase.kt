package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.monster.AbstractIllager
import net.minecraft.world.level.Level

/**
 * 板岩守卫（掠夺者/卫道士系）基类：
 * **直接继承原版 AbstractIllager**（借用其 CrossbowAttackMob 协处理、庆典标记与
 * 原版 IllagerModel 的全部动画：端弩/拉弦/挥武器/庆典，零自定义动画代码）。
 * 不调用 super.registerGoals() 以完全跳过 Raider 的袭击/巡逻目标；只加站桩闸门/索敌/游荡。
 * 主人（附近清醒的大环）醒着时解除站桩并攻击玩家。
 * 通用行为（唤醒扫描/召唤标记）来自 [GuardDormant] 接口的 GuardCore 组合实现。
 */
abstract class GuardIllagerBase(type: EntityType<out GuardIllagerBase>, level: Level) :
    AbstractIllager(type, level), GuardDormant {

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
        // 不调用 super（跳过 Raider 的袭击/巡逻目标）
        goalSelector.addGoal(1, GuardIdleGateGoal(this) { isMasterAwake() })
        goalSelector.addGoal(7, RandomStrollGoal(this, 0.8))
        // 索敌优先级：大环仇恨目标 > 附近玩家 > 受伤反击；全部仅唤醒时生效
        targetSelector.addGoal(1, GuardMasterHateGoal(this, awake = { isMasterAwake() }, master = { findMaster() }))
        targetSelector.addGoal(2, GuardTargetGoal(this) { isMasterAwake() })
        targetSelector.addGoal(3, GuardHurtByTargetGoal(this) { isMasterAwake() })
    }

    /** Raider 抽象成员：不留袭击加成（守卫不参与袭击）。 */
    override fun applyRaidBuffs(wave: Int, unusedFalse: Boolean) {
    }

    /** Raider 抽象成员：无庆祝音效（守卫不参与袭击庆祝）。 */
    override fun getCelebrateSound(): SoundEvent = SoundEvents.EMPTY

    companion object {
        /** 板岩守卫（掠夺者系）基础属性：原版怪物骨架 + 常用项。 */
        fun baseAttributes(): AttributeSupplier.Builder = createMonsterAttributes()
    }
}