package pub.pigeon.yggdyy.hexmob.mixin;

import at.petrak.hexcasting.common.blocks.decoration.BlockSconce;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

// This mixin aims to override rotate and mirror method in BlockSconce in order to make BlockSconce placeable during world generation.
// I have post an issue to Hexcasting, hope they will do this soon~
@Mixin(BlockSconce.class)
public abstract class BlockSconceMixin extends AmethystBlock {
    public BlockSconceMixin(Properties properties) {
        super(properties);
    }
    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(BlockSconce.FACING, rotation.rotate(state.getValue(BlockSconce.FACING)));
    }
    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockSconce.FACING)));
    }
}
