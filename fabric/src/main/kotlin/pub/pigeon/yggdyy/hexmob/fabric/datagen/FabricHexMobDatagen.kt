package pub.pigeon.yggdyy.hexmob.fabric.datagen

import pub.pigeon.yggdyy.hexmob.datagen.HexMobActionTags
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object FabricHexMobDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
        val pack = gen.createPack()

        pack.addProvider(::HexMobActionTags)
    }
}
