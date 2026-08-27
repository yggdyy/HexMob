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
import net.minecraft.world.entity.monster.CrossbowAttackMob
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor

/**
 * 弓箭守卫：外观像掠夺者，拿弩。
 * 完整弩战 AI（RangedCrossbowAttackGoal，原版掠夺者同款）：
 * 锁定目标 → 端起弩 → 拉弦蓄力（CROSSBOW_CHARGE 动画）→ performCrossbowAttack 射出箭矢。
 * 说明：原版对 mob 的弩"装填→发射"在部分场景装填失败（只有蓄力动画、不出箭），
 * 所以这里绕过装填状态机——release 路径的 shootCrossbowProjectile 置空，
 * 命中时机统一走 performCrossbowAttack 直接射箭。蓄力动画不受影响。
 * 贴图：assets/hexmob/textures/entity/guard/guard_archer.png
 */
class GuardArcher(type: EntityType<out GuardArcher>, level: Level) :
    CrystalGuardEntity(type, level), CrossbowAttackMob {

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(2, RangedCrossbowAttackGoal(this, 1.0, 8.0F))
    }

    /** 释放弩时的发射（原版路径）：置空，避免与 performCrossbowAttack 双发。 */
    override fun shootCrossbowProjectile(target: LivingEntity, stack: ItemStack, projectile: Projectile, power: Float) {
    }

    /** 真正的射击：每次攻击周期必然调用（READY_TO_ATTACK），直接朝目标放箭。 */
    override fun performCrossbowAttack(target: LivingEntity, power: Float) {
        val arrow = Arrow(level(), this)
        arrow.setBaseDamage(4.0)
        arrow.setShotFromCrossbow(true)
        val inaccuracy = (14 - level().difficulty.id * 4).toFloat()
        val dx = target.x - x
        val dy = target.y + target.eyeHeight * 0.5 - (y + eyeHeight * 0.5)
        val dz = target.z - z
        arrow.shoot(dx, dy, dz, 2.2F, inaccuracy)
        level().addFreshEntity(arrow)
        level().playSound(null, blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F)
    }

    override fun setChargingCrossbow(charging: Boolean) {
        // 蓄力动画由模型读 isUsingItem 驱动，无需额外状态
    }

    override fun performRangedAttack(target: LivingEntity, power: Float) {
        // CrossbowAttackMob 走 performCrossbowAttack，这里不用
    }

    override fun onCrossbowAttackPerformed() {
        // 无额外演出
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        reason: MobSpawnType,
        spawnData: SpawnGroupData?,
        dataTag: CompoundTag?
    ): SpawnGroupData? {
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack(Items.CROSSBOW))
        setDropChance(EquipmentSlot.MAINHAND, 0.0F)
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag)
    }

    companion object {
        fun registerAttributes(): AttributeSupplier.Builder = createMobAttributes()
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.32)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
    }
}