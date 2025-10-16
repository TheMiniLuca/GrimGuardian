package com.gmail.theminiluca.grim.guardian_1_21_4;

import com.github.retrooper.packetevents.util.Vector3i;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.NotNull;

@Getter
public class ServerLevel1_21_4 extends ServerLevel {
    private final net.minecraft.server.level.ServerLevel serverLevel;

    public ServerLevel1_21_4(@NotNull World world) {
        super(world);
        this.serverLevel = ((CraftWorld) world).getHandle();
    }

    @Override
    public void levelEvent(Block block) {
        BlockState blockState = ((CraftBlock) block).getNMS();
        BlockPos blockPos = ((CraftBlock) block).getPosition();
        if (blockState.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
            serverLevel.levelEvent(net.minecraft.world.level.block.LevelEvent.SOUND_EXTINGUISH_FIRE, blockPos, 0);
        } else {
            serverLevel.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK
                    , blockPos,
                    net.minecraft.world.level.block.Block.getId(blockState));
        }
    }

    @Override
    public void destroyBlockProgress(int breakerId, @NotNull Block pos, int progress) {
        BlockPos blockPos = ((CraftBlock) pos).getPosition();
        serverLevel.destroyBlockProgress(breakerId, blockPos, progress);
    }
    @Override
    public void cancelBlockProgress(int breakerId, @NotNull Block pos) {
        destroyBlockProgress(breakerId, pos, -1);
    }

    @Override
    public int getMaxBuildHeight() {
        return serverLevel.getMaxY();
    }
}
