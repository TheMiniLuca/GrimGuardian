package com.gmail.theminiluca.grim.guardian.listener;

import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.controller.BlockBreakMode;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.gmail.theminiluca.grim.guardian.GrimGuardian.ENABLE;

public class BukkitListener implements Listener {
    public static final @NotNull Map<UUID, BlockBreakMode> BLOCK_BREAK_MODE_MAP = new HashMap<>();
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockAbort(BlockDamageAbortEvent event) {
        if (!ENABLE) return;
        @Nullable BlockBreakMode blockBreakMode = BLOCK_BREAK_MODE_MAP.get(event.getPlayer().getUniqueId());
        if (blockBreakMode == null) return;
        blockBreakMode.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!ENABLE) return;
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            event.setCancelled(true);
            final ServerPlayer serverPlayer = GrimGuardian.getInstance().getServerPlayer(player);
            final ServerLevel level = GrimGuardian.getInstance().getServerLevel(player.getWorld());
            BlockBreakMode mode = new BlockBreakMode(serverPlayer, level, event);
            mode.run();
            BLOCK_BREAK_MODE_MAP.put(player.getUniqueId(), mode);
        }
    }
}
