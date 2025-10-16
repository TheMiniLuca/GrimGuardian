package com.gmail.theminiluca.grim.guardian.controller;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.enums.FluidTag;
import ac.grim.grimac.utils.inventory.EnchantmentHelper;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.util.Vector3i;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.event.BlockImpactEvent;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.Formula;
import com.google.common.base.Preconditions;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.gmail.theminiluca.grim.guardian.listener.BukkitListener.BLOCK_BREAK_MODE_MAP;

@Getter
public class BlockBreakMode {

    private final @NotNull ServerPlayer serverPlayer;
    private final @NotNull ServerLevel serverLevel;
    private final @NotNull Player player;
    private final @NotNull GrimPlayer grimPlayer;
    private final @NotNull PlayerInteractEvent event;
    private final @NotNull Block block;
    private final @NotNull BlockBreakSpeed blockBreakSpeed;
    private final @NotNull org.bukkit.inventory.ItemStack itemStack;
    private final @NotNull ItemStack packetItemStack;
    private final int maxBuildHeight;


    private float workingTime = 0;
    private int backProgress = 0;
    private @Nullable BukkitTask bukkitTask;

    public BlockBreakMode(@NotNull ServerPlayer serverPlayer, @NotNull ServerLevel serverLevel, @NotNull PlayerInteractEvent event) {
        this.serverPlayer = serverPlayer;
        this.player = serverPlayer.getPlayer();
        this.serverLevel = serverLevel;
        this.event = event;
        @Nullable Block block = event.getClickedBlock();
        Preconditions.checkNotNull(block);
        this.block = block;
        itemStack = player.getInventory().getItemInMainHand();
        packetItemStack = SpigotConversionUtil.fromBukkitItemStack(itemStack);
        this.blockBreakSpeed = BlockBreakSpeed.getDefaultBreakTick(player, block, packetItemStack);
        this.grimPlayer = Objects.requireNonNull(GrimAPI.INSTANCE.getPlayerDataManager()
                .getPlayer(player.getUniqueId()), "grimPlayer cannot be null");
        this.maxBuildHeight = serverLevel.getMaxBuildHeight();
        
    }

    public void cancel() {
        cancelTask();
        BLOCK_BREAK_MODE_MAP.remove(player.getUniqueId());

    }

    public void destroyBlockProgress(final int progress) {
        serverLevel.destroyBlockProgress(serverPlayer.getPlayer().getEntityId() << 5, block, progress);
    }
    private void cancelTask() {
        if (bukkitTask == null) return;
        serverLevel.cancelBlockProgress(player.getEntityId() << 5, block);
        bukkitTask.cancel();
        bukkitTask = null;
//        Bukkit.getScheduler().getMainThreadExecutor(GrimGuardian.getInstance()).execute(() -> {
//            new BlockDamageAbortEvent(player
//                    , player.getWorld()
//                    .getBlockAt(digging.getBlockPosition().getX(), digging.getBlockPosition().getY(), digging.getBlockPosition().getZ())
//                    , player.getInventory().getItemInMainHand()).callEvent();
//        });

    }

    private boolean checkBreakConditions() {
        if (!blockBreakSpeed.isBreakable()) {
            BlockBreakMode.this.cancel();
            return true;
        }
        if (!player.isOnline() || !grimPlayer.platformPlayer.isOnline()) {
            BlockBreakMode.this.cancel();
            return true;
        }
        if (player.getInventory().getItemInMainHand() == itemStack) {
            BlockBreakMode.this.cancel();
            return true;
        }
        if (block.getType().isAir()) {
            BlockBreakMode.this.cancel();
            return true;
        }
        return false;
    }
    public void run() {
        cancel();
        bukkitTask = new BukkitRunnable() {

            @Override
            public void run() {
                float hardness = block.getType().getHardness();
                if (checkBreakConditions()) {
                    BlockBreakMode.this.cancel();
                    return;
                }

                byte progress = (byte) ((workingTime / ((((float) blockBreakSpeed.getTick()) + 6.0F))) * 10);
                BlockImpactEvent blockImpactEvent = new BlockImpactEvent(player, block, BlockFace.valueOf(event.getBlockFace().name()), player.getInventory().getItemInMainHand(), blockBreakSpeed.isInstantBreak());
                blockImpactEvent.callEvent();
                if (!blockImpactEvent.isCancelled())
//                    destroyBlockProgress(progress);
                    if (backProgress != progress) {
                        destroyBlockProgress(progress);
                    }

                float worktime = 1.0F;
//                                int hasteLevel = Math.max(digSpeed.isEmpty() ? 0 : digSpeed.getAsInt(), conduit.isEmpty() ? 0 : conduit.getAsInt());
//                            worktime *= (float) ConfigHandler.evaluate(ConfigHandler.HASTE_EQUATION, grimPlayer, block.getType().getHardness());
                worktime *= (float) ConfigYaml.getInstance().getFormula(Formula.HASTE).evaluate(grimPlayer, hardness);
                worktime *= (float) ConfigYaml.getInstance().getFormula(Formula.MINING_FATIGUE).evaluate(grimPlayer, hardness);
                if (!grimPlayer.packetStateData.packetPlayerOnGround) {
                    worktime /= 5.0F;
                }
                if (grimPlayer.fluidOnEyes == FluidTag.WATER) {
                    if (grimPlayer.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21)
                            && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                        worktime *= (float) grimPlayer.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
                    } else {
                        if (EnchantmentHelper.getMaximumEnchantLevel(grimPlayer.getInventory(), EnchantmentTypes.AQUA_AFFINITY, ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents
                                .getAPI().getServerManager().getVersion().toClientVersion()) == 0) {
                            worktime /= 5.0F;
                        }
                    }
                }
                if (!blockImpactEvent.isCancelled()) workingTime += Math.max(worktime, 0);
                backProgress = progress;

                if ((progress >= 10 || blockImpactEvent.isInstantBreak()) && !blockImpactEvent.isCancelled()) {
                    if (!serverPlayer.canInteractWithBlock(block, 0.0D)) {
                        return;
                    }
                    BlockBreakMode.this.cancel();
                    ServerLevel world = GrimGuardian.getInstance().getServerLevel(player.getWorld());
                    world.levelEvent(block);
                    if (!blockBreakSpeed.isCorrectToolForDrop())
                        block.getDrops().clear();
                    player.breakBlock(block);

                }
            }
        }.runTaskTimer(GrimGuardian.getInstance(), 0L, 1L);
    }
}
