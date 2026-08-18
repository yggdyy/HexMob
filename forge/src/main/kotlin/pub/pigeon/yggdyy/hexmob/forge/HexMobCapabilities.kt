package pub.pigeon.yggdyy.hexmob.forge

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.forge.cap.HexCapabilities
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.api.entity.IotaEntity
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Forge hook: attaches Hex Casting's `HexCapabilities.IOTA` capability to every
 * entity implementing [IotaEntity], so spells like OpTheCoolerRead /
 * OpTheCoolerWrite can read/write them on Forge.
 */
object HexMobCapabilities {
    private val IOTA_STORAGE_CAP: ResourceLocation = HexMob.id("iota_storage")

    fun init() {
        MOD_BUS.addListener(this::attachEntityCaps)
    }

    private fun attachEntityCaps(event: AttachCapabilitiesEvent<Entity>) {
        val entity = event.`object`
        if (entity is IotaEntity) {
            event.addCapability(IOTA_STORAGE_CAP, IotaHolderProvider(entity))
        }
    }

    /** Bridges the capability view back to the entity's own storage. */
    private class IotaHolderProvider(private val entity: IotaEntity) : ICapabilityProvider {
        private val lazy: LazyOptional<ADIotaHolder> = LazyOptional.of<ADIotaHolder> { entity }

        override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
            if (cap == HexCapabilities.IOTA) lazy.cast() else LazyOptional.empty()
    }
}
