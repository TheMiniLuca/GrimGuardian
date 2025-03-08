package com.gmail.theminiluca.grim.guardian_1_21_4;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class ServerPlayer1_21_4 extends com.gmail.theminiluca.grim.guardian.hook.ServerPlayer {
    private final net.minecraft.server.level.ServerPlayer serverPlayer;
    public ServerPlayer1_21_4(@NotNull Player player) {
        super(player);
        this.serverPlayer = ((CraftPlayer) player).getHandle();
    }

    @Override
    public boolean canInteractWithBlock(@NotNull Block block, double value) {
        final BlockPos blockPos = ((CraftBlock) block).getPosition();
        return serverPlayer.canInteractWithBlock(blockPos, value);
    }
}
