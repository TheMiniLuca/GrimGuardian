package com.gmail.theminiluca.grim.guardian.controller;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.util.Vector3i;
import com.gmail.theminiluca.grim.guardian.controller.speed.BlockBreakSpeed;
import com.gmail.theminiluca.grim.guardian.controller.speed.CorrectToolChecker;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.google.common.base.Preconditions;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


@Getter
public class BlockBreakContext {

    protected final @NotNull ServerPlayer serverPlayer;
    protected final @NotNull ServerLevel serverLevel;
    protected final @NotNull Player player;
    protected final @NotNull GrimPlayer grimPlayer;
    protected final @NotNull PlayerInteractEvent event;
    protected final @NotNull Block block;
    protected final @NotNull World world;
    protected final @NotNull Vector3i vector3i;
    protected final @NotNull org.bukkit.inventory.ItemStack itemStack;
    protected final @NotNull ItemStack packetItemStack;
    protected final int maxBuildHeight;
    protected final float blockHardness;
    protected final @NotNull CorrectToolChecker correctToolChecker;


    public BlockBreakContext(@NotNull ServerPlayer serverPlayer, @NotNull ServerLevel serverLevel,
                          @NotNull PlayerInteractEvent event, float blockHardness, @NotNull CorrectToolChecker correctToolChecker) {
        this.serverPlayer = serverPlayer;
        this.player = serverPlayer.getPlayer();
        this.serverLevel = serverLevel;
        this.event = event;
        @Nullable Block block = event.getClickedBlock();
        Preconditions.checkNotNull(block);
        this.block = block;
        this.correctToolChecker = correctToolChecker;
        itemStack = player.getInventory().getItemInMainHand();
        packetItemStack = SpigotConversionUtil.fromBukkitItemStack(itemStack);
        this.blockHardness = blockHardness;
        this.grimPlayer = Objects.requireNonNull(GrimAPI.INSTANCE.getPlayerDataManager()
                .getPlayer(player.getUniqueId()), "grimPlayer cannot be null");
        this.maxBuildHeight = serverLevel.getMaxBuildHeight();
        this.vector3i = new Vector3i(block.getX(), block.getY(), block.getZ());
        this.world = serverLevel.getWorld();
    }
}
