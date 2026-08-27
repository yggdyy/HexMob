package pub.pigeon.yggdyy.hexmob.content.guard

import net.minecraft.client.model.HierarchicalModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob

/**
 * 铁傀儡风格守卫模型：包一层原版 ModelLayers.IRON_GOLEM 烘好的 ModelPart
 * （几何与贴图 UV 和原版铁傀儡一致，配合 guard_golem.png）。
 *
 * 姿态简化：主人未醒 → 双手自然下垂站桩；醒着 → 双臂随行走摆动。
 */
class GuardGolemModel<T : Entity>(private val root: ModelPart) : HierarchicalModel<T>() {
    private val head = root.getChild("head")
    private val rightArm = root.getChild("right_arm")
    private val leftArm = root.getChild("left_arm")
    private val rightLeg = root.getChild("right_leg")
    private val leftLeg = root.getChild("left_leg")

    override fun root(): ModelPart = root

    override fun setupAnim(entity: T, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float) {
        head.yRot = netHeadYaw * DEG_TO_RAD
        head.xRot = headPitch * DEG_TO_RAD

        // 攻击动画：每次命中挥拳（getAttackAnim）→ 双臂从举起往下砸（铁傀儡式）
        val swing = (entity as? Mob)?.getAttackAnim(0.0F) ?: 0.0F
        if (swing > 0.05F) {
            rightArm.xRot = -1.2F + swing * 2.2F
            leftArm.xRot = -1.2F + swing * 2.2F
        } else {
            rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount
            leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount
        }

        rightLeg.xRot = -1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount
        leftLeg.xRot = 1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount
        rightLeg.yRot = 0.0F
        leftLeg.yRot = 0.0F
    }

    companion object {
        private const val DEG_TO_RAD = 0.017453292F
    }
}
