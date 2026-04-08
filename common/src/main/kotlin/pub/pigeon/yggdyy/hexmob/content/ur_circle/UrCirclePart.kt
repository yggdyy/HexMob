package pub.pigeon.yggdyy.hexmob.content.ur_circle

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.content.HMEntityPart

abstract class UrCirclePart(val parentMob: UrCircleEntity, val id: ResourceLocation, width: Float, height: Float) : HMEntityPart(parentMob.type, parentMob.level()) {
    val size: EntityDimensions
    var posNow: Vec3 = Vec3.ZERO
    var posPrev: Vec3 = Vec3.ZERO
    var dirNow = Vec3(0.0, 0.0, 1.0)
    var dirPrev = Vec3(0.0, 0.0, 1.0)
    init {
        size = EntityDimensions.scalable(width, height)
        this.refreshDimensions()
    }
    override fun defineSynchedData() {}
    override fun readAdditionalSaveData(compound: CompoundTag) {}
    override fun addAdditionalSaveData(compound: CompoundTag) {}
    override fun isPickable(): Boolean = true
    override fun getPickResult(): ItemStack? = parentMob.pickResult
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        return if (isInvulnerableTo(source)) false else parentMob.hurt(source, amount)
    }
    override fun `is`(entity: Entity): Boolean = this === entity || parentMob === entity
    override fun getDimensions(pose: Pose): EntityDimensions = size
    override fun shouldBeSaved(): Boolean = false
    override fun getAddEntityPacket(): Packet<ClientGamePacketListener> {
        throw UnsupportedOperationException()
    }
    override fun shouldInteract(): Boolean = true
    override fun shouldRenderAsPart(): Boolean = true
    fun changeState(pos: Vec3, dir: Vec3) {
        xOld = x
        yOld = y
        zOld = z
        setPos(pos.add(0.0, -bbHeight / 2.0, 0.0))
        posPrev = posNow
        posNow = pos
        dirPrev = dirNow
        dirNow = dir
    }
}