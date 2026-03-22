package pub.pigeon.yggdyy.hexmob.content.amethyst_silverfish

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import pub.pigeon.yggdyy.hexmob.content.akashic_library.AmethystInfestedBlock
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.animation.RawAnimation
import software.bernie.geckolib.core.`object`.PlayState
import software.bernie.geckolib.util.GeckoLibUtil

class AmethystSilverfishEntity(entityType: EntityType<out Monster>, level: Level) : GeoEntity, Monster(entityType, level) {
    private val animatableInstance: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(AnimationController(this, "idle") { state ->
            if (!entityData.get(IS_FIGHTING)) state.setAndContinue(ANI_IDLE)
            else {
                state.resetCurrentAnimation()
                PlayState.STOP
            }
        })
        controllers.add(AnimationController(this, "move") { state ->
            if (entityData.get(IS_FIGHTING)) state.setAndContinue(ANI_MOVE)
            else {
                state.resetCurrentAnimation()
                PlayState.STOP
            }
        })
    }
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animatableInstance
    override fun getAmbientSound(): SoundEvent {
        return SoundEvents.SILVERFISH_AMBIENT
    }
    override fun getHurtSound(damageSource: DamageSource): SoundEvent {
        return SoundEvents.SILVERFISH_HURT
    }
    override fun getDeathSound(): SoundEvent {
        return SoundEvents.SILVERFISH_DEATH
    }
    override fun playStepSound(pos: BlockPos, state: BlockState) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15f, 1.0f)
    }
    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(1, ClimbOnTopOfPowderSnowGoal(this, level()))
        goalSelector.addGoal(4, MeleeAttackGoal(this, 1.0, false))
        goalSelector.addGoal(5, HideIntoBlockGoal(this))
        goalSelector.addGoal(6, RandomStrollGoal(this, 0.5))
        targetSelector.addGoal(1, HurtByTargetGoal(this).setAlertOthers())
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }
    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(IS_FIGHTING, false)
    }
    override fun tick() {
        super.tick()
        if(!level().isClientSide && tickCount % 20 == 0) {
            entityData.set(IS_FIGHTING, (target != null && target!!.isAlive))
        }
    }
    companion object {
        val ANI_IDLE: RawAnimation = RawAnimation.begin().thenLoop("idle")
        val ANI_MOVE: RawAnimation = RawAnimation.begin().thenLoop("move")
        val IS_FIGHTING: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(AmethystSilverfishEntity::class.java, EntityDataSerializers.BOOLEAN);
        fun registerAttributes(): AttributeSupplier.Builder = createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.MOVEMENT_SPEED, 0.2)
        class HideIntoBlockGoal(val silverfish: AmethystSilverfishEntity): Goal() {
            override fun canUse(): Boolean = silverfish.random.nextInt(120) == 0 && (silverfish.target == null || !silverfish.target!!.isAlive)
            override fun start() {
                super.start()
                val center: BlockPos = BlockPos.containing(silverfish.position())
                val candidates: Iterable<BlockPos> = BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))
                val level: ServerLevel = silverfish.level() as? ServerLevel ?: return
                for(p in candidates) {
                    val nowID: ResourceLocation = BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).block)
                    val toState: BlockState = AmethystInfestedBlock.infestMap[nowID]?.let { BuiltInRegistries.BLOCK.get(it).defaultBlockState() } ?: continue
                    level.setBlock(p, toState, 3)
                    silverfish.discard()
                    level.playSound(null, p, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE)
                    break
                }
            }
        }
    }
}