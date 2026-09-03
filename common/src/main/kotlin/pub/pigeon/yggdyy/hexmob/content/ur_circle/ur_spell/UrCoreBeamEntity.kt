package pub.pigeon.yggdyy.hexmob.content.ur_circle.ur_spell

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/**
 * 【核心光束载体】ur_beam 法术的隐藏实体：本身不可见（渲染器只画链接光束），
 * 纯粹作为"施法者→目标"光束的同步载体，复刻原版 EndCrystal 的 beamTarget 同步方式。
 *
 * 服务端每 tick（BeamTicker）把实体位置设到施法者眼睛、beamTarget 设到目标身体中心，
 * 客户端渲染器读取两端绘制末影水晶式链接光束。倒计时结束由 BeamTicker discard。
 */
class UrCoreBeamEntity(entityType: EntityType<out Entity>, level: Level) : Entity(entityType, level) {
    init {
        noPhysics = true
        noCulling = true
    }

    override fun defineSynchedData() {
        entityData.define(BEAM_TARGET, Vector3f(0F, 0F, 0F))
    }

    /** 光束指向的终点（世界坐标，浮点精度，随目标移动每 tick 更新）。 */
    fun setBeamTarget(pos: Vec3) {
        entityData.set(BEAM_TARGET, Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat()))
    }

    fun beamTarget(): Vec3? {
        val v = entityData.get(BEAM_TARGET)
        // 默认值 (0,0,0) 视为未设置
        return if (v.x == 0F && v.y == 0F && v.z == 0F) null
        else Vec3(v.x.toDouble(), v.y.toDouble(), v.z.toDouble())
    }

    override fun shouldBeSaved(): Boolean = false
    override fun addAdditionalSaveData(compound: CompoundTag) {}
    override fun readAdditionalSaveData(compound: CompoundTag) {}
    override fun getAddEntityPacket(): Packet<ClientGamePacketListener> = ClientboundAddEntityPacket(this)

    companion object {
        private val BEAM_TARGET: EntityDataAccessor<Vector3f> =
            SynchedEntityData.defineId(UrCoreBeamEntity::class.java, EntityDataSerializers.VECTOR3)
    }
}
