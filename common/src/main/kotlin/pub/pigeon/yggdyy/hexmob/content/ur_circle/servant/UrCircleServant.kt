package pub.pigeon.yggdyy.hexmob.content.ur_circle.servant

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.entity.monster.Vex
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.content.ur_circle.UrCircleEntity
import kotlin.math.atan2

/**
 * 大环的下级生物（参考卫道士+恼鬼）：直接继承原版恼鬼 Vex——
 * 飞行、穿墙、追击主人目标、近战挥砍的整套 AI 全部复用。
 *
 * 召唤时 setOwner(大环)，它的 CopyOwnerTargetGoal 会自动攻击大环的目标，
 * 也就是"大环锁谁，下属砍谁"。材质放在 assets/hexmob/textures/entity/ 下可自行修改。
 *
 * 注意：原版恼鬼的飞行速度是写死在它内部 MoveControl 里的固定 0.25 系数，
 * 改 MOVEMENT_SPEED 属性没用，所以这里换了一个减半的自定义飞行控制器。
 */
class UrCircleServant(entityType: EntityType<out Vex>, level: Level) : Vex(entityType, level) {

    init {
        // 换用更慢的飞行控制器
        moveControl = ServantMoveControl(this)
    }

    /** 下属死亡：每击败一个下属，给大环造成 5 点直伤（见 UrCircleEntity.onServantKilled）。 */
    override fun die(source: DamageSource) {
        super.die(source)
        val owner = owner
        if (owner is UrCircleEntity) {
            owner.onServantKilled()
        }
    }

    companion object {
        fun registerAttributes(): AttributeSupplier.Builder =
            Vex.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
    }

    /** 减半版恼鬼飞行控制：原版 0.25 速度系数 → 这里 SERVANT_SPEED（约一半），朝向逻辑照搬。 */
    private class ServantMoveControl(private val servant: UrCircleServant) : MoveControl(servant) {
        override fun tick() {
            if (operation == MoveControl.Operation.MOVE_TO) {
                val v = Vec3(wantedX - mob.x, wantedY - mob.y, wantedZ - mob.z)
                val dist = v.length()
                if (dist < mob.bbWidth) {
                    operation = MoveControl.Operation.WAIT
                    mob.setDeltaMovement(mob.deltaMovement.scale(0.5))
                } else {
                    mob.setDeltaMovement(mob.deltaMovement.scale(0.2).add(v.scale(SERVANT_SPEED / dist)))
                    if (mob.target == null) {
                        val mv = mob.deltaMovement
                        mob.yRot = (-atan2(mv.x, mv.z).toFloat()) * 180F / Math.PI.toFloat()
                        mob.yBodyRot = mob.yRot
                    } else {
                        val dx = mob.target!!.x - mob.x
                        val dz = mob.target!!.z - mob.z
                        mob.yRot = (-atan2(dx, dz).toFloat()) * 180F / Math.PI.toFloat()
                        mob.yBodyRot = mob.yRot
                    }
                }
            }
        }

        companion object {
            /** 飞行速度系数（原版恼鬼为 0.25，这里约减半）。 */
            const val SERVANT_SPEED = 0.12
        }
    }
}
