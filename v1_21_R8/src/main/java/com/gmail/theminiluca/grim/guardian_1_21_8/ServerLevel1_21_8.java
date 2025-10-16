package com.gmail.theminiluca.grim.guardian_1_21_8;

import com.github.retrooper.packetevents.util.Vector3i;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class ServerLevel1_21_8 extends ServerLevel {
    private final net.minecraft.server.level.ServerLevel serverLevel;

    public ServerLevel1_21_8(@NotNull World world) {
        super(world);
        this.serverLevel = ((CraftWorld) world).getHandle();
    }

    @Override
    public void destroyBlockProgress(int breakerId, @NotNull Vector3i pos, int progress) {
        serverLevel.destroyBlockProgress(breakerId, new BlockPos(pos.x, pos.y, pos.z), progress);
    }
    public void cancelBlockProgress(int breakerId, @NotNull Vector3i pos) {
        destroyBlockProgress(breakerId, pos, -1);
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

}
