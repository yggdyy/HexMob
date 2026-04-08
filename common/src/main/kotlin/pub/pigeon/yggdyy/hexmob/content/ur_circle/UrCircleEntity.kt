package pub.pigeon.yggdyy.hexmob.content.ur_circle

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.content.IHMMultipartEntity
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.CubePart
import pub.pigeon.yggdyy.hexmob.content.ur_circle.subentities.SlatePart
import pub.pigeon.yggdyy.hexmob.util.rotateDA

class UrCircleEntity(entityType: EntityType<out Mob>, level: Level) : Mob(entityType, level), Enemy, IHMMultipartEntity<UrCirclePart> {
    val equator: MutableList<UrCirclePart> = mutableListOf(
        CubePart(this, HexAPI.modLoc("impetus/empty"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/look"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/redstone"), "energized=false,facing=south,powered=false"),
        CubePart(this, HexAPI.modLoc("impetus/rightclick"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/empty"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/look"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("impetus/redstone"), "energized=true,facing=south,powered=false"),
        CubePart(this, HexAPI.modLoc("impetus/rightclick"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("directrix/empty"), "energized=false,facing=south"),
        CubePart(this, HexAPI.modLoc("directrix/boolean"), "energized=false,facing=south,state=false"),
        CubePart(this, HexAPI.modLoc("directrix/redstone"), "energized=false,facing=south,powered=false"),
        CubePart(this, HexAPI.modLoc("directrix/empty"), "energized=true,facing=south"),
        CubePart(this, HexAPI.modLoc("directrix/boolean"), "energized=true,facing=south,state=false"),
        CubePart(this, HexAPI.modLoc("directrix/redstone"), "energized=true,facing=south,powered=false"),
    )
    val ecliptic: MutableList<UrCirclePart> = mutableListOf(
        SlatePart(this, HexPattern.fromAnglesUnchecked("eqawwwwqqaw", HexDir.SOUTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("e", HexDir.EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("waqw", HexDir.SOUTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("wedw", HexDir.NORTH_WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qsq", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("aawqqeee", HexDir.NORTH_WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qqaeqwaeswqwq", HexDir.NORTH_WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qaq", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("wawwawwewwqsq", HexDir.WEST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("qaqwqaaswa", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("wqadaqw", HexDir.NORTH_EAST)),
        SlatePart(this, HexPattern.fromAnglesUnchecked("eqqwqwqeda", HexDir.SOUTH_EAST)),
    )
    val earth: CubePart = CubePart(this, HexMob.id("cube"), HexAPI.modLoc("quenched_allay_bricks"), "")
    var equatorRadius: Vec3
        get() = Vec3(entityData.get(EQUATOR_RADIUS))
        set(value) = entityData.set(EQUATOR_RADIUS, value.toVector3f())
    var equatorNormal: Vec3
        get() = Vec3(entityData.get(EQUATOR_NORMAL))
        set(value) = entityData.set(EQUATOR_NORMAL, value.toVector3f())
    var equatorRotation: Float
        get() = entityData.get(EQUATOR_ROTATION)
        set(value) = entityData.set(EQUATOR_ROTATION, value)
    var eclipticRadius: Vec3
        get() = Vec3(entityData.get(ECLIPTIC_RADIUS))
        set(value) = entityData.set(ECLIPTIC_RADIUS, value.toVector3f())
    var eclipticNormal: Vec3
        get() = Vec3(entityData.get(ECLIPTIC_NORMAL))
        set(value) = entityData.set(ECLIPTIC_NORMAL, value.toVector3f())
    var eclipticRotation: Float
        get() = entityData.get(ECLIPTIC_ROTATION)
        set(value) = entityData.set(ECLIPTIC_ROTATION, value)
    var earthRadius: Vec3
        get() = Vec3(entityData.get(EARTH_RADIUS))
        set(value) = entityData.set(EARTH_RADIUS, value.toVector3f())
    var earthNormal: Vec3
        get() = Vec3(entityData.get(EARTH_NORMAL))
        set(value) = entityData.set(EARTH_NORMAL, value.toVector3f())
    var earthRotation: Float
        get() = entityData.get(EARTH_ROTATION)
        set(value) = entityData.set(EARTH_ROTATION, value)
    init {
        noPhysics = true
        noCulling = true
        IHMMultipartEntity.instances.add(this)
    }
    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(EQUATOR_RADIUS, Vector3f(4F, 0F, 0F))
        entityData.define(EQUATOR_NORMAL, Vector3f(0F, 0.917F, -0.399F))
        entityData.define(EQUATOR_ROTATION, 0F)
        entityData.define(ECLIPTIC_RADIUS, Vector3f(6F, 0F, 0F))
        entityData.define(ECLIPTIC_NORMAL, Vector3f(0F, 0.917F, 0.399F))
        entityData.define(ECLIPTIC_ROTATION, 0F)
        entityData.define(EARTH_RADIUS, Vector3f(0.1F, 0F, 0F))
        entityData.define(EARTH_NORMAL, Vector3f(0F, 1F, 0F))
        entityData.define(EARTH_ROTATION, 0F)
    }
    override fun aiStep() {
        super.aiStep()
        updateShape()
        updatePartsPos()
    }
    fun updateShape() {
        if(!level().isClientSide) {
            equatorRotation += -1
            eclipticRotation += 1
        }
    }
    fun updatePartsPos() {
        val origin: Vec3 = position().add(0.0, bbHeight / 2.0, 0.0)
        earth.changeState(origin, if(target != null) target!!.position().subtract(origin).normalize() else Vec3(0.0, 0.0, 1.0))
        for(i in 0..<equator.size) {
            val deg: Float = equatorRotation + (i / equator.size.toFloat() * 360F)
            val delta: Vec3 = equatorRadius.rotateDA(deg, equatorNormal)
            equator[i].changeState(origin.add(delta), equatorNormal.cross(delta).normalize())
        }
        for(i in 0..<ecliptic.size) {
            val deg: Float = eclipticRotation + (i / ecliptic.size.toFloat() * 360F)
            val delta: Vec3 = eclipticRadius.rotateDA(deg, eclipticNormal)
            ecliptic[i].changeState(origin.add(delta), delta.normalize())
        }
    }
    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)

    }
    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)

    }
    override fun getAllParts(): List<UrCirclePart> {
        return buildList {
            addAll(equator)
            addAll(ecliptic)
            add(earth)
        }
    }
    override fun shouldRecord(): Boolean = isAlive
    override fun addEffect(effectInstance: MobEffectInstance, entity: Entity?): Boolean = false
    override fun canRide(vehicle: Entity): Boolean = false
    override fun canChangeDimensions(): Boolean = false
    override fun isPickable(): Boolean = false
    override fun getExperienceReward() = Enemy.XP_REWARD_BOSS
    override fun isNoGravity(): Boolean = true
    override fun recreateFromPacket(packet: ClientboundAddEntityPacket) {
        super.recreateFromPacket(packet)
        val parts: List<UrCirclePart> = getAllParts()
        for(i in parts.indices) {
            parts[i].setId(packet.id + i + 1)
        }
    }
    companion object {
        val EQUATOR_RADIUS: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EQUATOR_NORMAL: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EQUATOR_ROTATION: EntityDataAccessor<Float> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.FLOAT)
        val ECLIPTIC_RADIUS: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val ECLIPTIC_NORMAL: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val ECLIPTIC_ROTATION: EntityDataAccessor<Float> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.FLOAT)
        val EARTH_RADIUS: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EARTH_NORMAL: EntityDataAccessor<Vector3f> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.VECTOR3)
        val EARTH_ROTATION: EntityDataAccessor<Float> = SynchedEntityData.defineId(UrCircleEntity::class.java, EntityDataSerializers.FLOAT)
        fun registerAttributes(): AttributeSupplier.Builder = createMobAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.ARMOR, 20.0).add(Attributes.ARMOR_TOUGHNESS, 10.0)
    }
}