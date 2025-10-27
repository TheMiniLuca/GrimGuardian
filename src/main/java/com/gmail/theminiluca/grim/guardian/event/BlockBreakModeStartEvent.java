package com.gmail.theminiluca.grim.guardian.event;

import com.gmail.theminiluca.grim.guardian.controller.BlockBreakMode;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlockBreakModeStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull ServerPlayer serverPlayer;
    private final @NotNull ServerLevel serverLevel;
    private final @NotNull PlayerInteractEvent interactEvent;
    public BlockBreakModeStartEvent(@NotNull ServerPlayer serverPlayer, @NotNull ServerLevel serverLevel, @NotNull PlayerInteractEvent interactEvent) {
        this.serverPlayer = serverPlayer;
        this.serverLevel = serverLevel;
        this.interactEvent = interactEvent;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract("->new")
    public BlockBreakMode getFinalBreakMode() {
        return new BlockBreakMode(serverPlayer, serverLevel, interactEvent);
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
