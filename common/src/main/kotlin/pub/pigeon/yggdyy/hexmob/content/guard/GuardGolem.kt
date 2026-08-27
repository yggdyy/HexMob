package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.level.Level

/**
 * 傀儡守卫：外观像铁傀儡，重装肉盾，主人醒来后近战重拳。
 * 攻击动画由模型读 getAttackAnim（每次命中挥拳必触发，铁傀儡式下砸）。
 * 贴图：assets/hexmob/textures/entity/guard/guard_golem.png
 */
class GuardGolem(type: EntityType<out GuardGolem>, level: Level) : CrystalGuardEntity(type, level) {

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(2, MeleeAttackGoal(this, 1.0, true))
    }

    companion object {
        fun registerAttributes(): AttributeSupplier.Builder = createMobAttributes()
            .add(Attributes.MAX_HEALTH, 110.0)
            .add(Attributes.MOVEMENT_SPEED, 0.22)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 13.0)
            .add(Attributes.ARMOR, 6.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
    }
}
