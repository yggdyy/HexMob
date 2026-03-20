package pub.pigeon.yggdyy.hexmob.content.stimulated_pattern

import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.DifficultyInstance
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration
import net.minecraft.world.phys.Vec3
import pub.pigeon.yggdyy.hexmob.HexMob
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities.STIMULATED_PATTERN
import java.util.*

class StimulatedPatternEntity(type: EntityType<StimulatedPatternEntity>, level: Level) : FlyingMob(type, level) {
    init {
    }
    override fun defineSynchedData() {
        super.defineSynchedData()
        this.entityData.define(PATTERN, HexPattern.fromAngles("", HexDir.EAST).serializeToNBT())
    }
    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        if(compound.contains(PATTERN_KEY)) {
            setPatternNbt(compound.getCompound(PATTERN_KEY))
        }
    }
    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put(PATTERN_KEY, getPatternNbt())
    }
    private fun getPatternNbt(): CompoundTag = this.entityData.get(PATTERN)
    private fun setPatternNbt(nbt: CompoundTag) = this.entityData.set(PATTERN, nbt)
    fun getPattern(): HexPattern = HexPattern.fromNBT(getPatternNbt())
    fun setPattern(pat: HexPattern) = setPatternNbt(pat.serializeToNBT())
    override fun finalizeSpawn(
        level: ServerLevelAccessor,
        difficulty: DifficultyInstance,
        reason: MobSpawnType,
        spawnData: SpawnGroupData?,
        dataTag: CompoundTag?
    ): SpawnGroupData? {
        isNoGravity = true
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag)
    }
    override fun tick() {
        super.tick()
        if(tickCount < 10 && !level().isClientSide && getPattern().angles.size <= 0) {
            setPattern(HexActions.REGISTRY.getRandom(getRandom()).get().value().prototype)
        }
    }
    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(16, RandomMoveGoal(this))
    }
    override fun getHurtSound(damageSource: DamageSource): SoundEvent = SoundEvents.AMETHYST_CLUSTER_HIT
    companion object {
        val PATTERN: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(StimulatedPatternEntity::class.java, EntityDataSerializers.COMPOUND_TAG)
        val PATTERN_KEY: String = HexMob.id("pattern").toString()
        fun registerAttributes(): AttributeSupplier.Builder {
            return Mob.createMobAttributes()
        }
        fun spawnInGeode(context: FeaturePlaceContext<GeodeConfiguration>, sugPos: BlockPos) {
            val entity = StimulatedPatternEntity(STIMULATED_PATTERN.get(), context.level().level)
            for(i in 0..10) {
                val now: BlockPos = sugPos.offset(0, i, 0)
                if(context.level().getBlockState(now).isAir && !context.level().getBlockState(now.above()).isAir) {
                    entity.setPos(now.center.add(0.0, -0.5, 0.0))
                    context.level().level.addFreshEntity(entity)
                    return
                }
            }
        }
        private class RandomMoveGoal(val mob: FlyingMob, val interval: Int = 40): Goal() {
            init {
                flags = EnumSet.of(Flag.MOVE)
            }
            var next: Int = interval
            override fun canUse(): Boolean {
                return if(next <= 0) {
                    next = interval
                    true
                } else {
                    next--
                    false
                }
            }
            override fun start() {
                super.start()
                val target: Vec3 = Vec3(mob.random.nextGaussian(), mob.random.nextGaussian(), mob.random.nextGaussian()).normalize()
                mob.setXxa(target.x.toFloat() * 0.3F)
                mob.setYya((target.y.toFloat() - 0.3F) * 0.3F)
                mob.setZza(target.z.toFloat() * 0.3F)
            }
            override fun canContinueToUse(): Boolean {
                return mob.random.nextInt(interval * 2) != 0
            }
            override fun stop() {
                super.stop()
                mob.setXxa(0F)
                mob.setYya(0F)
                mob.setZza(0F)
                mob.setDeltaMovement(0.0, 0.0, 0.0)
            }
        }
    }
}