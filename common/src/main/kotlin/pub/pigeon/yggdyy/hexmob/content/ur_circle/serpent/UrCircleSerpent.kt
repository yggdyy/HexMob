package pub.pigeon.yggdyy.hexmob.content.ur_circle.serpent

import at.petrak.hexcasting.common.lib.HexBlocks
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import pub.pigeon.yggdyy.hexmob.util.spawnParticle
import java.util.UUID
import kotlin.math.sin

/**
 * 促动石长蛇：由大环释放的独立临时蛇实体。
 * 蛇头沿朝向做蛇形摆动前进，身体是过去若干 tick 位置组成的拖尾（渲染成石板块）。
 * 不锁定目标（**不必中**）：朝向是释放瞬间锁定的，目标躲开就会落空。
 * 移动逻辑确定性（两端同步），伤害/粒子只在服务端跑并广播。
 */
class UrCircleSerpent(entityType: EntityType<out Entity>, level: Level) : Entity(entityType, level) {

    /** 身体拖尾：蛇头在前（绝对坐标）。 */
    val trail: ArrayDeque<Vec3> = ArrayDeque()
    private val contactCooldowns: MutableMap<UUID, Int> = HashMap()
    private var lifeTicks = 0
    /** 施法者（大环）：伤害归属。 */
    var owner: LivingEntity? = null

    init {
        noPhysics = true
        noCulling = true
    }

    /** 设置飞行朝向（在 addFreshEntity 前调用，随实体包同步到客户端）。 */
    fun setAim(x: Double, y: Double, z: Double) {
        entityData.set(AIM, Vector3f(x.toFloat(), y.toFloat(), z.toFloat()))
    }

    private fun aim(): Vec3 {
        val v = entityData.get(AIM)
        val d = Vec3(v.x.toDouble(), v.y.toDouble(), v.z.toDouble())
        return if (d.lengthSqr() < 1.0E-6) Vec3(0.0, 0.0, 1.0) else d.normalize()
    }

    override fun defineSynchedData() {
        entityData.define(AIM, Vector3f(0F, 0F, 1F))
    }

    override fun tick() {
        super.tick()
        // 蛇形游动：前进 + 横向正弦摆动（确定性，两端同步）
        val forward = aim()
        val lateral = forward.cross(UP).let { if (it.lengthSqr() < 1.0E-6) Vec3(1.0, 0.0, 0.0) else it.normalize() }
        val sway = sin(tickCount * 0.35) * SWAY_AMPLITUDE
        val prevSway = sin((tickCount - 1) * 0.35) * SWAY_AMPLITUDE
        val step = forward.scale(SPEED).add(lateral.scale(sway - prevSway))
        setPos(x + step.x, y + step.y, z + step.z)

        trail.addFirst(position())
        while (trail.size > MAX_SEGMENTS) trail.removeLast()

        if (!level().isClientSide) {
            hurtContacts()
            spawnBlockParticles()
            lifeTicks++
            if (lifeTicks >= LIFETIME) {
                discard()
            }
        }
    }

    /** 蛇头碰撞：对非敌对生物造成伤害（带每目标冷却）。 */
    private fun hurtContacts() {
        val it = contactCooldowns.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val next = e.value - 1
            if (next <= 0) it.remove() else e.setValue(next)
        }
        val head = position()
        val box = AABB(head, head).inflate(1.2)
        for (victim in level().getEntitiesOfClass(LivingEntity::class.java, box)) {
            //if (victim is Enemy) continue
            if (contactCooldowns.containsKey(victim.uuid)) continue
            if (victim.hurt(level().damageSources().mobAttack(owner), CONTACT_DAMAGE)) {
                contactCooldowns[victim.uuid] = CONTACT_COOLDOWN
            }
        }
    }

    /** 石板块 BLOCK 粒子：蛇头每 tick，蛇身每隔 2 tick。 */
    private fun spawnBlockParticles() {
        val state = HexBlocks.SLATE_BLOCK.defaultBlockState()
        val opt = BlockParticleOption(ParticleTypes.BLOCK, state)
        val p = position()
        spawnParticle(level(), opt, p.x, p.y, p.z, 0.0, 0.1, 0.0)
        if (tickCount % 2 == 0 && trail.size > 2) {
            val body = trail[trail.size / 2]
            spawnParticle(level(), opt, body.x, body.y, body.z, 0.0, 0.0, 0.0)
        }
    }

    override fun shouldBeSaved(): Boolean = false
    override fun addAdditionalSaveData(compound: CompoundTag) {}
    override fun readAdditionalSaveData(compound: CompoundTag) {}
    override fun getAddEntityPacket(): Packet<ClientGamePacketListener> = ClientboundAddEntityPacket(this)

    companion object {
        private val AIM: EntityDataAccessor<Vector3f> =
            SynchedEntityData.defineId(UrCircleSerpent::class.java, EntityDataSerializers.VECTOR3)
        private val UP = Vec3(0.0, 1.0, 0.0)
        const val SPEED = 0.7
        const val SWAY_AMPLITUDE = 1.2
        const val MAX_SEGMENTS = 16
        const val LIFETIME = 60
        const val CONTACT_DAMAGE = 8.0F
        const val CONTACT_COOLDOWN = 8
    }
}
