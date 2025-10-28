package com.gmail.theminiluca.grim.guardian.event;

import com.gmail.theminiluca.grim.guardian.controller.BlockBreakMode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class BlockImpactEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Player player;
    private final @NotNull Block block;
    private boolean instantBreak;
    private boolean cancelled;
    private final @NotNull ItemStack itemstack;
    private final @NotNull BlockFace blockFace;
    private final @NotNull BlockBreakMode mode;


    @ApiStatus.Internal
    public BlockImpactEvent(@NotNull Player player, @NotNull Block block, @NotNull BlockFace blockFace, @NotNull ItemStack itemInHand, boolean instaBreak, @NotNull BlockBreakMode mode) {
        super(true);
        this.block = block;
        this.blockFace = blockFace;
        this.instantBreak = instaBreak;
        this.player = player;
        this.itemstack = itemInHand;
        this.cancelled = false;
        this.mode = mode;
    }


    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}