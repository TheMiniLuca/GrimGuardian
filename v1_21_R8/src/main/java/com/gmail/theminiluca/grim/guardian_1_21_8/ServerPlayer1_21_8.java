package com.gmail.theminiluca.grim.guardian_1_21_8;

import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ServerPlayer1_21_8 extends com.gmail.theminiluca.grim.guardian.hook.ServerPlayer {
    private final net.minecraft.server.level.ServerPlayer serverPlayer;
    public ServerPlayer1_21_8(@NotNull Player player) {
        super(player);
        this.serverPlayer = ((CraftPlayer) player).getHandle();
    }


    @Override
    public boolean canInteractWithBlock(@NotNull Block block, double value) {
        final BlockPos blockPos = ((CraftBlock) block).getPosition();

        return serverPlayer.canInteractWithBlock(blockPos, value);
    }

    @Override
    public double getBlockBreakSpeed() {
        @Nullable AttributeInstance attributeInstance = getPlayer().getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (attributeInstance == null) {
            throw new IllegalStateException("Player has no BLOCK_BREAK_SPEED attribute");
        }
        return attributeInstance.getValue();
    }
}
