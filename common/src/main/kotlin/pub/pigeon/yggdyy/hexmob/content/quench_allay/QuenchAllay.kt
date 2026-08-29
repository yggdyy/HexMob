package pub.pigeon.yggdyy.hexmob.content.quench_allay

import at.petrak.hexcasting.api.addldata.ADMediaHolder
import at.petrak.hexcasting.api.utils.compareMediaItem
import at.petrak.hexcasting.api.utils.downcast
import at.petrak.hexcasting.api.utils.extractMedia
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.api.utils.vecFromNBT
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.allay.Allay
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.api.entity.CastingEntity
import pub.pigeon.yggdyy.hexmob.api.entity.IotaEntity
import pub.pigeon.yggdyy.hexmob.api.entity.defineIotaAccessor
import pub.pigeon.yggdyy.hexmob.api.entity.emptyIotaTag

/**
 * A quenched allay (淬晶悦灵): an allay that doubles as an iota carrier and
 * caster, and can be given a movement target as a stored vector.
 *
 * Note: this is an ALLAY, not a sheep — it does not use the sheep's behaviour
 * registry, wool colouring, or spawn rules.
 */
class QuenchAllay(entityType: EntityType<out QuenchAllay>, level: Level) : Allay(entityType, level), CastingEntity,
    IotaEntity {

    companion object {
        private val IOTA = defineIotaAccessor(QuenchAllay::class.java)
        private val TARGET = defineIotaAccessor(QuenchAllay::class.java)
        private val IOTA_KEY: String = HexMob.id("iota").toString()
        private val TARGET_KEY: String = HexMob.id("target").toString()
    }

    /**
     * Pays media from both hands *and* this allay's inventory (allays carry
     * their items in the inventory, not in a held slot). Any item that exposes
     * a media holder works here — amethyst dust/shards, media phials (媒质之瓶),
     * packaged spells, etc. — consumed in Hex's priority order.
     */
    override fun consumeMedia(cost: Long, simulate: Boolean): Long {
        if (cost <= 0) return 0
        val sources = mutableListOf<ADMediaHolder>()
        for (hand in arrayOf(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND)) {
            IXplatAbstractions.INSTANCE.findMediaHolder(getItemInHand(hand))
                ?.takeIf { it.canProvide() }?.let(sources::add)
        }
        val inv = getInventory()
        for (i in 0 until inv.containerSize) {
            IXplatAbstractions.INSTANCE.findMediaHolder(inv.getItem(i))
                ?.takeIf { it.canProvide() }?.let(sources::add)
        }
        sources.sortWith(::compareMediaItem)
        sources.reverse()
        var costLeft = cost
        for (holder in sources) {
            if (costLeft <= 0) break
            costLeft -= extractMedia(holder, costLeft, false, simulate)
        }
        return costLeft
    }

    override fun getCastingRange(): Double = 32.0

    /** A quench allay casts as if enlightened (it may use great spells). */
    override fun isEnlightened(): Boolean = true

    // 数据放 entityData：自动同步到客户端
    override fun defineSynchedData() {
        super.defineSynchedData()
        this.entityData.define(IOTA, emptyIotaTag())
        this.entityData.define(TARGET, emptyIotaTag())
    }

    // 存盘/读盘：iota 槽与 target 槽分开持久化
    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        if (compound.contains(IOTA_KEY)) {
            setIotaNbt(compound.getCompound(IOTA_KEY))
        }
        if (compound.contains(TARGET_KEY)) {
            setAllayTarget(compound.getCompound(TARGET_KEY))
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put(IOTA_KEY, getIotaNbt())
        compound.put(TARGET_KEY, getAllayTargetTag())
    }

    override fun getIotaNbt(): CompoundTag = this.entityData.get(IOTA)

    override fun setIotaNbt(nbt: CompoundTag) {
        this.entityData.set(IOTA, nbt)
    }

    override fun getServerLevel(): ServerLevel? = level() as? ServerLevel

    fun getAllayTargetTag(): CompoundTag = this.entityData.get(TARGET)

    fun setAllayTarget(nbt: CompoundTag) {
        this.entityData.set(TARGET, nbt)
    }

    fun setAllayTarget(vec: Vec3) {
        this.setAllayTarget(vec.serializeToNBT())
    }

    fun getAllayTarget(): Vec3 {
        val tag = this.getAllayTargetTag()
        return if (tag.type == LongArrayTag.TYPE) {
            val lat = tag.downcast(LongArrayTag.TYPE)
            vecFromNBT(lat.asLongArray)
        } else {
            vecFromNBT(tag)
        }
    }

    /** 清空移动目标（备用方法）。 */
    fun clearAllayTarget() {
        this.entityData.set(TARGET, emptyIotaTag())
    }

    /** 默认为空 target（emptyIotaTag）；只有确实设置了向量目标时才与之不同。 */
    private fun hasTarget(): Boolean = this.getAllayTargetTag() != emptyIotaTag()

    override fun tick() {
        super.tick()
        if (this.tickCount % 7 == 0 && hasTarget()) {
            val vec = this.getAllayTarget()
            this.navigation.moveTo(vec.x, vec.y, vec.z, 1.0)
        }
    }
}
