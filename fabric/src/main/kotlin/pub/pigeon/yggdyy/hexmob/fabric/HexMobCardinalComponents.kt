package pub.pigeon.yggdyy.hexmob.fabric

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import at.petrak.hexcasting.fabric.cc.adimpl.CCIotaHolder
import dev.onyxstudios.cca.api.v3.component.ComponentFactory
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import pub.pigeon.yggdyy.hexmob.api.entity.IotaEntity

/**
 * Fabric hook: registers every entity implementing [IotaEntity] with Hex
 * Casting's `IOTA_HOLDER` Cardinal Component, so spells like
 * OpTheCoolerRead / OpTheCoolerWrite can read/write them on Fabric.
 *
 * Registered via the `cardinal-components-entity` entrypoint in fabric.mod.json.
 */
object HexMobCardinalComponents : EntityComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(
            { clazz -> IotaEntity::class.java.isAssignableFrom(clazz) },
            HexCardinalComponents.IOTA_HOLDER,
            ComponentFactory { provider -> EntityIotaHolderComponent(provider as IotaEntity) },
        )
    }

    /** Bridges the CCA holder view back to the entity's own storage. */
    private class EntityIotaHolderComponent(
        private val entity: IotaEntity,
    ) : CCIotaHolder {
        override fun readIotaTag(): CompoundTag? = entity.getIotaNbt()

        override fun writeable(): Boolean = true

        override fun writeIota(iota: Iota?, simulate: Boolean): Boolean {
            if (iota == null) return writeable()
            if (!simulate) {
                entity.writeIota(iota)
            }
            return true
        }

        // Storage lives on the entity (entityData / its own NBT), not on the
        // component's tag, so the component's serialization hooks are no-ops.
        override fun readFromNbt(tag: CompoundTag) { /* no-op */ }

        override fun writeToNbt(tag: CompoundTag) { /* no-op */ }
    }
}
