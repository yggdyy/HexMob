package pub.pigeon.yggdyy.hexmob.api.entity

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

/**
 * Helpers for implementing [IotaEntity] with a `SynchedEntityData`-backed
 * `CompoundTag` slot (the standard way to sync an entity's stored iota to the
 * client). An implementing entity wires these up like:
 *
 * ```
 * companion object {
 *     private val IOTA = defineIotaAccessor(MyEntity::class.java)
 * }
 * override fun defineSynchedData() {
 *     entityData.define(IOTA, emptyIotaTag())
 * }
 * override fun getIotaNbt()  = entityData.get(IOTA)
 * override fun setIotaNbt(t) { entityData.set(IOTA, t) }
 * override fun getServerLevel() = level() as? ServerLevel
 * ```
 */

/** Define the iota data slot, used inside [Entity.defineSynchedData]. */
fun defineIotaAccessor(entityClass: Class<out Entity>): EntityDataAccessor<CompoundTag> =
    SynchedEntityData.defineId(entityClass, EntityDataSerializers.COMPOUND_TAG)

/** A fresh stored tag holding a [NullIota] (the "no iota stored" default). */
fun emptyIotaTag(): CompoundTag = IotaType.serialize(NullIota())

/**
 * Deserialize [nbt] into an [Iota].
 * @param level the level to deserialize against; pass null on the client where
 *        [IotaType.deserialize] cannot run → returns a [NullIota] instead.
 */
fun deserializeIota(nbt: CompoundTag, level: ServerLevel?): Iota =
    if (level != null) IotaType.deserialize(nbt, level) else NullIota()
