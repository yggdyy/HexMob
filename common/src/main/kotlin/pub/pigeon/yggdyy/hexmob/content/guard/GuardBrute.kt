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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor

/**
 * 斧头守卫：外观像卫道士，手拿铁斧，主人醒来后近战劈砍。
 * 贴图：assets/hexmob/textures/entity/guard/guard_brute.png
 */
class GuardBrute(type: EntityType<out GuardBrute>, level: Level) : CrystalGuardEntity(type, level) {

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, false))
    }

    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        reason: MobSpawnType,
        spawnData: SpawnGroupData?,
        dataTag: CompoundTag?
    ): SpawnGroupData? {
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack(Items.IRON_AXE))
        setDropChance(EquipmentSlot.MAINHAND, 0.0F)
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag)
    }

    companion object {
        fun registerAttributes(): AttributeSupplier.Builder = createMobAttributes()
            .add(Attributes.MAX_HEALTH, 44.0)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
    }
}
