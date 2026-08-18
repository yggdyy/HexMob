package pub.pigeon.yggdyy.hexmob.api.entity

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel

/**
 * A living entity that can carry and expose a spell iota.
 *
 * Implements [ADIotaHolder] directly, so any entity implementing this interface
 * is automatically readable/writable by the Cooler read/write spells
 * (OpTheCoolerRead / OpTheCoolerWrite) once the platform hooks it up as a
 * data holder (Fabric: HexCardinalComponents.IOTA_HOLDER; Forge:
 * HexCapabilities.IOTA).
 *
 * [writeIota] / [readIota] come with shared default implementations that handle
 * [IotaType] serialization — an implementing entity only needs to provide the
 * raw serialized-iota storage ([getIotaNbt] / [setIotaNbt]) and its server
 * level ([getServerLevel]). Storage/syncing remains the entity's responsibility.
 */
interface IotaEntity : ADIotaHolder {
    /** The raw serialized iota tag; back it with your entity's data (e.g. entityData). */
    fun getIotaNbt(): CompoundTag

    fun setIotaNbt(nbt: CompoundTag)

    /**
     * The server level backing deserialization.
     * @return the entity's level when it is on the server, null on the client.
     */
    fun getServerLevel(): ServerLevel?

    // --- ADIotaHolder (drives OpTheCoolerRead / OpTheCoolerWrite) ---

    override fun readIotaTag(): CompoundTag? = getIotaNbt()

    override fun writeIota(iota: Iota?, simulate: Boolean): Boolean {
        if (iota == null) return writeable()
        if (!simulate) {
            setIotaNbt(IotaType.serialize(iota))
        }
        return true
    }

    override fun writeable(): Boolean = true

    // --- convenience (serialization with the entity's own level) ---

    /** Serialize [iota] and store it on this entity. */
    fun writeIota(iota: Iota) {
        setIotaNbt(IotaType.serialize(iota))
    }

    /**
     * Deserialize and return the stored iota; [NullIota] when on the client
     * (where [IotaType.deserialize] cannot run, as it needs a [ServerLevel]).
     */
    fun readIota(): Iota {
        val level = getServerLevel()
        return if (level != null) IotaType.deserialize(getIotaNbt(), level) else NullIota()
    }
}
