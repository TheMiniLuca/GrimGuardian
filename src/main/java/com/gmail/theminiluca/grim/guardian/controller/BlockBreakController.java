package com.gmail.theminiluca.grim.guardian.controller;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockBreakController implements PacketListener, Listener {


    public static boolean DISABLE = false;
    public static final Map<UUID, BlockBreakMode> BLOCK_BREAK_MODE_MAP = new HashMap<>();





    @SuppressWarnings("removal")
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        User user = event.getUser();

        if (event.getPacketType().equals(PacketType.Play.Client.PLAYER_DIGGING)) {
            Player player = event.getPlayer();
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
            DiggingAction action = digging.getAction();
            if (event.isCancelled()) return;
            if (DISABLE) {
//                if (action.equals(DiggingAction.START_DIGGING)) {
//                    ms.put(player.getUniqueId(), System.currentTimeMillis());
//                }
//                if (action.equals(DiggingAction.FINISHED_DIGGING)) {
//                    long m = (System.currentTimeMillis() - ms.get(player.getUniqueId()));
//                    player.sendMessage(m + "ms ( %d tick )".formatted(m / 50));
//                }
//                return;
                return;
            }
            if (player.getGameMode().equals(GameMode.CREATIVE)) return;
            if (action.equals(DiggingAction.RELEASE_USE_ITEM) || action.equals(DiggingAction.DROP_ITEM) || action.equals(DiggingAction.DROP_ITEM_STACK) || action.equals(DiggingAction.SWAP_ITEM_WITH_OFFHAND)) {
                return;
            }
            if (!action.equals(DiggingAction.START_DIGGING)) {
                event.setCancelled(true);
                @Nullable BlockBreakMode blockBreakMode = BLOCK_BREAK_MODE_MAP.get(user.getUUID());
                if (blockBreakMode == null) return;
                Bukkit.getScheduler().getMainThreadExecutor(GrimGuardian.getInstance()).execute(() -> {
                    blockBreakMode.cancel();
                    BLOCK_BREAK_MODE_MAP.remove(user.getUUID());
                });
            } else {
                event.setCancelled(true);
                Bukkit.getScheduler().getMainThreadExecutor(GrimGuardian.getInstance()).execute(() -> {
                    final ServerPlayer serverPlayer = GrimGuardian.getInstance().getServerPlayer(player);
                    final ServerLevel level = GrimGuardian.getInstance().getServerLevel(player.getWorld());
                    BlockBreakMode mode = new BlockBreakMode(serverPlayer, level, digging);
                    mode.run();
                });
            }
        }
    }
}
