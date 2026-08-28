package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.monster.AbstractIllager
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor

/**
 * 板岩兵（卫道士型）：**继承原版 AbstractIllager**，动画交给原版 IllagerModel
 * （攻击时 getArmPose→ATTACKING → 原版模型挥动持斧手臂）。
 */
class GuardBrute(type: EntityType<out GuardBrute>, level: Level) : GuardIllagerBase(type, level) {

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, false))
    }

    /** 照原版卫道士（Vindicator.getArmPose）：庆祝→CELEBRATING、有目标→ATTACKING（挥斧）、否则 NEUTRAL。 */
    override fun getArmPose(): AbstractIllager.IllagerArmPose {
        return when {
            isCelebrating() -> AbstractIllager.IllagerArmPose.CELEBRATING
            isAggressive() -> AbstractIllager.IllagerArmPose.ATTACKING
            else -> AbstractIllager.IllagerArmPose.NEUTRAL
        }
    }

    /** 大环召唤时补发铁斧（召唤不走 finalizeSpawn，否则裸手）。 */
    override fun equipOnSummon() {
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack(Items.IRON_AXE))
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
            .add(Attributes.MAX_HEALTH, 44.0)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
    }
}