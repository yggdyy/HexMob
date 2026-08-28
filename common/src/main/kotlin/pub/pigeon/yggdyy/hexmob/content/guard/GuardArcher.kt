package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal
import net.minecraft.world.entity.monster.AbstractIllager
import net.minecraft.world.entity.monster.CrossbowAttackMob
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor

/**
 * 板岩弩手（掠夺者型）：**继承原版 AbstractIllager + CrossbowAttackMob**，
 * 动画完全交给原版 IllagerModel（getArmPose → CROSSBOW_HOLD/CHARGE/ATTACKING 等）。
 */
class GuardArcher(type: EntityType<out GuardArcher>, level: Level) :
    GuardIllagerBase(type, level), CrossbowAttackMob {

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(2, RangedCrossbowAttackGoal(this, 1.0, 8.0F))
    }

    /** 掠夺者同款持弩姿态：让原版 IllagerModel 自动做端弩/拉弦动画。 */
    override fun getArmPose(): AbstractIllager.IllagerArmPose {
        return when {
            isCelebrating() -> AbstractIllager.IllagerArmPose.CELEBRATING
            isAggressive() -> AbstractIllager.IllagerArmPose.ATTACKING
            isUsingItem() && mainHandItem.`is`(Items.CROSSBOW) -> AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE
            mainHandItem.`is`(Items.CROSSBOW) -> AbstractIllager.IllagerArmPose.CROSSBOW_HOLD
            else -> AbstractIllager.IllagerArmPose.NEUTRAL
        }
    }

    override fun setChargingCrossbow(charging: Boolean) {
        // 拉弦状态由 isUsingItem 驱动（getArmPose→CROSSBOW_CHARGE）
    }

    override fun shootCrossbowProjectile(target: LivingEntity, stack: ItemStack, projectile: Projectile, power: Float) {
    }

    override fun onCrossbowAttackPerformed() {
    }

    /** RangedAttackMob 抽象成员：走弩体系，这里不用。 */
    override fun performRangedAttack(target: LivingEntity, velocity: Float) {
    }

    /** 真正的射击：每个攻击周期必然调用，直接朝目标放箭（不依赖弩装填状态）。 */
    override fun performCrossbowAttack(target: LivingEntity, power: Float) {
        val arrow = Arrow(level(), this)
        arrow.setBaseDamage(4.0)
        arrow.setShotFromCrossbow(true)
        val inaccuracy = (14 - level().difficulty.id * 4).toFloat()
        arrow.shoot(
            target.x - x,
            target.y + target.eyeHeight * 0.5 - (y + eyeHeight * 0.5),
            target.z - z,
            2.2F,
            inaccuracy,
        )
        level().addFreshEntity(arrow)
        level().playSound(null, blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F)
    }

    /** 大环召唤时补发弩（召唤不走 finalizeSpawn，否则裸手）。 */
    override fun equipOnSummon() {
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack(Items.CROSSBOW))
        setDropChance(EquipmentSlot.MAINHAND, 0.0F)
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        reason: MobSpawnType,
        spawnData: SpawnGroupData?,
        dataTag: CompoundTag?
    ): SpawnGroupData? {
        equipOnSummon()
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag)
    }

    companion object {
        fun registerAttributes(): AttributeSupplier.Builder = baseAttributes()
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
    }
}