package pub.pigeon.yggdyy.hexmob.content.guard

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.AnimationUtils
import net.minecraft.client.model.ArmedModel
import net.minecraft.client.model.HierarchicalModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.Items

/**
 * 掠夺者/卫道士风格守卫模型：包一层原版 ModelLayers.PILLAGER / VINDICATOR 烘好的
 * ModelPart（几何与贴图 UV 和原版完全一致，配合玩家自己改的 guard_*.png）。
 *
 * 姿态简化：主人未醒（不具攻击性）→ 双手交叉站桩；醒着 → 双手摆动/挥武器。
 * 实现 ArmedModel 让 ItemInHandLayer 能把手持物（弩/斧）画到手上。
 */
class GuardIllagerModel<T : Entity>(private val root: ModelPart) : HierarchicalModel<T>(), ArmedModel {
    private val head = root.getChild("head")
    private val hat = head.getChild("hat")
    private val arms = root.getChild("arms")
    private val leftLeg = root.getChild("left_leg")
    private val rightLeg = root.getChild("right_leg")
    private val leftArm = root.getChild("left_arm")
    private val rightArm = root.getChild("right_arm")

    override fun root(): ModelPart = root

    /** 手持物渲染锚点：弩/斧画在对应手臂上（ItemInHandLayer 用）。 */
    override fun translateToHand(arm: HumanoidArm, poseStack: PoseStack) {
        (if (arm == HumanoidArm.RIGHT) rightArm else leftArm).translateAndRotate(poseStack)
    }

    override fun setupAnim(entity: T, limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float) {
        head.yRot = netHeadYaw * DEG_TO_RAD
        head.xRot = headPitch * DEG_TO_RAD
        // 一直用左右臂，隐藏交叉的 arms 部分
        arms.visible = false
        leftArm.visible = true
        rightArm.visible = true

        val mob = entity as? Mob
        val mainHand = mob?.mainHandItem
        if (mainHand != null && mainHand.`is`(Items.CROSSBOW)) {
            // 弓箭守卫（掠夺者式弩手）：
            // 拉弦蓄力（用弩中 isUsingItem）→ 双臂后拉；平时/瞄准 → 双臂端弩随头部瞄准
            if (mob.isUsingItem) {
                AnimationUtils.animateCrossbowCharge(rightArm, leftArm, mob, true)
            } else {
                AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, true)
            }
        } else if (mob != null && mob.isAggressive) {
            // 近战守卫（斧头守卫）：攻击时挥下手中武器，否则摆臂
            AnimationUtils.swingWeaponDown(rightArm, leftArm, mob, attackTime, ageInTicks)
        } else {
            // 站岗/行走：手臂随步伐摆动（沉睡站岗时 limbSwingAmount≈0 自然下垂）
            rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Math.PI.toFloat()) * 2.0F * limbSwingAmount * 0.5F
            leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F
            rightArm.zRot = 0.0F
            leftArm.zRot = 0.0F
        }

        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Math.PI.toFloat()) * 1.4F * limbSwingAmount
        hat.copyFrom(head)
    }

    companion object {
        private const val DEG_TO_RAD = 0.017453292F
    }
}
