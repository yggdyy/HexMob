package pub.pigeon.yggdyy.hexmob.content.crying_amethyst

import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration
import pub.pigeon.yggdyy.hexmob.registry.HexMobEntities

class CryingAmethystEntity(entityType: EntityType<CryingAmethystEntity>, level: Level) : Mob(entityType, level) {
    override fun getHurtSound(damageSource: DamageSource): SoundEvent = listOf(SoundEvents.GHAST_HURT, SoundEvents.AMETHYST_BLOCK_HIT)[random.nextInt(2)]
    override fun getAmbientSound(): SoundEvent = listOf(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundEvents.GHAST_AMBIENT)[random.nextInt(2)]
    override fun isPushable(): Boolean = false
    companion object {
        fun registerAttributes(): AttributeSupplier.Builder = createMobAttributes().add(Attributes.MAX_HEALTH, 10.0)
        fun spawnInGeode(context: FeaturePlaceContext<GeodeConfiguration>, sugPos: BlockPos) {
            val entity = CryingAmethystEntity(HexMobEntities.CRYING_AMETHYST.get(), context.level().level)
            for(i in 0..10) {
                val now: BlockPos = sugPos.offset(0, -i, 0)
                if(context.level().getBlockState(now).isAir && !context.level().getBlockState(now.below()).isAir) {
                    entity.setPos(now.center.add(0.0, -0.5, 0.0))
                    context.level().level.addFreshEntity(entity)
                    return
                }
            }
        }
    }
}