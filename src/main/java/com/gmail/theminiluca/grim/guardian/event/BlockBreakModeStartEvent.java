package com.gmail.theminiluca.grim.guardian.event;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.gmail.theminiluca.grim.guardian.controller.BlockBreakMode;
import com.gmail.theminiluca.grim.guardian.controller.speed.CorrectToolChecker;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Setter
@Getter
public class BlockBreakModeStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull ServerPlayer serverPlayer;
    private final @NotNull ServerLevel serverLevel;
    private final @NotNull PlayerInteractEvent interactEvent;
    private final @NotNull Block block;
    protected final @NotNull org.bukkit.inventory.ItemStack itemStack;
    private final @NotNull ItemStack protocolItem;
    private float blockHardness;
    private @NotNull CorrectToolChecker toolChecker;
    private @NotNull BlockBreakMode blockBreakMode;
    public BlockBreakModeStartEvent(@NotNull ServerPlayer serverPlayer, @NotNull ServerLevel serverLevel, @NotNull PlayerInteractEvent interactEvent) {
        this.serverPlayer = serverPlayer;
        this.serverLevel = serverLevel;
        this.interactEvent = interactEvent;
        this.block = Objects.requireNonNull(interactEvent.getClickedBlock());
        this.blockHardness = block.getType().getHardness();
        this.itemStack = serverPlayer.getPlayer().getInventory().getItemInMainHand();
        this.protocolItem = SpigotConversionUtil.fromBukkitItemStack(itemStack);
        this.toolChecker = new CorrectToolChecker(this);
        this.blockBreakMode = getInitBreakMode();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Contract("->new")
<<<<<<< Updated upstream
    public BlockBreakMode getFinalBreakMode() {
        return new BlockBreakMode(serverPlayer, serverLevel, interactEvent, blockHardness, toolChecker);
=======
    public BlockBreakMode getInitBreakMode() {
        return new BlockBreakMode(new BlockBreakContext(serverPlayer, serverLevel, interactEvent, blockHardness, toolChecker));
>>>>>>> Stashed changes
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
