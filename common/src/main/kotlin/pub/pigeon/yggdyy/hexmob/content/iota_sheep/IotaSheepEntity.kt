package pub.pigeon.yggdyy.hexmob.content.iota_sheep

import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.utils.ERROR_COLOR
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnGroupData
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import org.joml.Vector3f
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.api.entity.IotaEntity
import pub.pigeon.yggdyy.hexmob.api.entity.defineIotaAccessor
import pub.pigeon.yggdyy.hexmob.api.entity.emptyIotaTag
import pub.pigeon.yggdyy.hexmob.api.sheep.IotaSheepBehaviors
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities

class IotaSheepEntity(entityType: EntityType<IotaSheepEntity>, world: Level) : Sheep(entityType, world), IotaEntity {

    companion object {
        private val IOTA = defineIotaAccessor(IotaSheepEntity::class.java)
        private val IOTA_KEY: String = HexMob.id("iota").toString()
        // 0 = no override; otherwise an ARGB wool colour set by a behaviour.
        private val DYNAMIC_COLOUR: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(IotaSheepEntity::class.java, EntityDataSerializers.INT)
    }

    /** Throttle for the read-feedback so rapid reads don't machinegun particles. */
    private var lastFeedbackTick = Int.MIN_VALUE

    // 数据放 entityData：自动同步到客户端
    override fun defineSynchedData() {
        super.defineSynchedData()
        this.entityData.define(IOTA, emptyIotaTag())
        this.entityData.define(DYNAMIC_COLOUR, 0)
    }

    // 存盘/读盘：iota 随实体 NBT 持久化
    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        if (compound.contains(IOTA_KEY)) {
            setIotaNbt(compound.getCompound(IOTA_KEY))
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put(IOTA_KEY, getIotaNbt())
    }

    override fun getIotaNbt(): CompoundTag = this.entityData.get(IOTA)

    override fun setIotaNbt(nbt: CompoundTag) {
        this.entityData.set(IOTA, nbt)
        this.entityData.set(DYNAMIC_COLOUR, 0) // a new iota starts with no colour override
    }

    override fun getServerLevel(): ServerLevel? = level() as? ServerLevel

    // ---------- flavour helpers (client-safe, drive rendering + particles) ----------

    /** True ARGB of the wool colour: a behaviour override if set, else the stored iota's type colour, else white. */
    fun getIotaArgb(): Int {
        val dynamic = this.entityData.get(DYNAMIC_COLOUR)
        if (dynamic != 0) return dynamic
        val type = IotaType.getTypeFromTag(getIotaNbt())
        if (type == null || type == HexIotaTypes.NULL) return 0xFF_FFFFFF.toInt()
        val argb = IotaType.getColor(getIotaNbt())
        return if (argb == ERROR_COLOR) 0xFF_FFFFFF.toInt() else argb
    }

    /** Whether a real (non-null) iota is stored. */
    fun hasIota(): Boolean {
        val type = IotaType.getTypeFromTag(getIotaNbt())
        return type != null && type != HexIotaTypes.NULL
    }

    /** Override the wool colour (used by behaviours, e.g. ListIota cycling); synced to the client. */
    fun setDynamicColour(argb: Int) {
        this.entityData.set(DYNAMIC_COLOUR, argb)
    }

    fun clearDynamicColour() {
        this.entityData.set(DYNAMIC_COLOUR, 0)
    }

    // ---------- movement helpers (public so behaviours / other mods can steer the sheep) ----------

    fun moveToward(x: Double, y: Double, z: Double, speed: Double) {
        this.navigation.moveTo(x, y, z, speed)
    }

    fun moveToward(target: Entity, speed: Double) {
        this.navigation.moveTo(target, speed)
    }

    fun stopMoving() {
        this.navigation.stop()
    }

    // ---------- ④ ambient: slow iota-colored dust around the sheep ----------
    // ---------- + behaviour dispatch (IotaType -> IotaSheepBehavior) ----------
    override fun tick() {
        super.tick()
        val world = level() as? ServerLevel ?: return
        if (hasIota() && tickCount % 24 == 0) {
            val pos = position()
            spawnIotaDust(world, pos.x, pos.y, pos.z, 0.7, 3)
        }
        IotaSheepBehaviors.tick(this)
    }

    // ---------- ⑤ feedback: particle burst + amethyst chime on write / read / shear ----------
    private fun iotaFeedback(strong: Boolean) {
        if (tickCount - lastFeedbackTick < 4) return
        lastFeedbackTick = tickCount
        val world = level() as? ServerLevel ?: return
        val pos = position()
        spawnIotaDust(world, pos.x, pos.y, pos.z, if (strong) 1.1 else 0.6, if (strong) 28 else 12)
        world.playSound(
            null, blockPosition(),
            if (strong) SoundEvents.AMETHYST_CLUSTER_PLACE else SoundEvents.AMETHYST_CLUSTER_HIT,
            SoundSource.HOSTILE, if (strong) 1.1f else 0.45f, 1.0f + random.nextFloat() * 0.25f,
        )
    }

    private fun spawnIotaDust(world: ServerLevel, x: Double, y: Double, z: Double, spread: Double, count: Int) {
        val argb = getIotaArgb()
        val r = (argb shr 16 and 0xFF) / 255f
        val g = (argb shr 8 and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        repeat(count) {
            world.addParticle(
                DustParticleOptions(Vector3f(r, g, b), 1.0f),
                x + (random.nextDouble() - 0.5) * spread,
                y + 0.4 + (random.nextDouble() - 0.5) * spread,
                z + (random.nextDouble() - 0.5) * spread,
                0.0, 0.0, 0.0,
            )
        }
    }

    // Cooler write -> burst + chime
    override fun writeIota(iota: Iota) {
        super.writeIota(iota)
        iotaFeedback(true)
    }

    // Cooler read -> subtle burst (skip when nothing stored)
    override fun readIota(): Iota {
        val iota = super.readIota()
        if (iota !is NullIota) iotaFeedback(false)
        return iota
    }

    /**
     * 羊毛颜色以所存 iota 的类型色为准（取最近的染料色）；未存 iota（或 NullIota）时为白色。
     * 供 vanilla 兜底渲染用；自定义渲染层用 [getIotaArgb]（精确 ARGB）。
     */
    override fun getColor(): DyeColor {
        val type = IotaType.getTypeFromTag(getIotaNbt())
        if (type == null || type == HexIotaTypes.NULL) return DyeColor.WHITE
        val argb = IotaType.getColor(getIotaNbt())
        if (argb == ERROR_COLOR) return DyeColor.WHITE
        return DyeColor.values()
            .minByOrNull { dye -> rgbDistance(dye.getFireworkColor(), argb) }
            ?: DyeColor.WHITE
    }

    private fun rgbDistance(a: Int, b: Int): Int {
        val dr = (a shr 16 and 0xFF) - (b shr 16 and 0xFF)
        val dg = (a shr 8 and 0xFF) - (b shr 8 and 0xFF)
        val db = (a and 0xFF) - (b and 0xFF)
        return dr * dr + dg * dg + db * db
    }

    /**
     * 剪切时掉落 2~3 个结念绳（HexItems.THOUGHT_KNOT），每个都写入一份当前存储的 iota 副本；
     * 未存 iota 时掉落空的结念绳。
     */
    override fun shear(source: SoundSource) {
        if (level().isClientSide || isSheared) return
        setSheared(true)
        level().playSound(null, this, SoundEvents.SHEEP_SHEAR, source, 1.0f, 1.0f)

        val stored = readIota() // 服务端；空时为 NullIota
        val count = 2 + random.nextInt(2) // 2..3
        repeat(count) {
            val knot = ItemStack(HexItems.THOUGHT_KNOT)
            if (stored !is NullIota) {
                HexItems.THOUGHT_KNOT.writeDatum(knot, stored)
            }
            spawnAtLocation(knot)
        }
        iotaFeedback(true)
    }

    // ---- 繁殖：后代仍是咒念羊并遗传 iota（引诱/繁殖食物维持原版小麦，不覆写 registerGoals） ----

    /** 后代为咒念羊；若任一亲本怀有 iota，随机继承其一（复制，不共享 NBT）。 */
    override fun getBreedOffspring(level: ServerLevel, other: AgeableMob): IotaSheepEntity {
        val baby = HexMobEntities.IOTA_SHEEP.get().create(level)
            ?: throw IllegalStateException("Failed to create iota sheep")
        val donor = if (random.nextBoolean()) this else other
        if (donor is IotaSheepEntity && donor.hasIota()) {
            baby.setIotaNbt(donor.getIotaNbt().copy())
        }
        return baby
    }

    // Natural-spawn: give a wild sheep a random iota so it actually shows
    // behaviour (and isn't just a plain sheep).
    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        reason: MobSpawnType,
        spawnData: SpawnGroupData?,
        dataTag: CompoundTag?,
    ): SpawnGroupData? {
        val data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag)
        if (!level.isClientSide && !hasIota()) {
            rollNaturalIota()
        }
        return data
    }

    private fun rollNaturalIota() {
        val roll = random.nextInt(100)
        val iota: Iota? = when {
            // 50%: a colourful list -> wool cycles through colours (List behaviour)
            roll < 50 -> ListIota(listOf(DoubleIota(0.0), BooleanIota(true), NullIota()))
            // 25%: a nearby point -> it wanders there (Vec3 behaviour)
            roll < 75 -> {
                val pos = position()
                Vec3Iota(pos.add((random.nextDouble() - 0.5) * 12.0, -0.5, (random.nextDouble() - 0.5) * 12.0))
            }
            // 10%: a nearby creature -> it trails it (Entity behaviour)
            roll < 85 -> {
                val nearby = level().getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(16.0))
                    .firstOrNull { it !== this }
                nearby?.let(::EntityIota)
            }
            // else: docile (empty)
            else -> null
        }
        if (iota != null) {
            setIotaNbt(IotaType.serialize(iota))
        }
    }
}
