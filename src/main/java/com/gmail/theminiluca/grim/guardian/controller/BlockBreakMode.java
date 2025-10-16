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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.event.BlockImpactEvent;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.Formula;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public class BlockBreakMode {

    private final @NotNull ServerPlayer serverPlayer;
    private final @NotNull ServerLevel serverLevel;
    private final @NotNull Player player;
    private final @NotNull WrapperPlayClientPlayerDigging digging;
    private @Nullable BukkitTask bukkitTask;

    public BlockBreakMode(@NotNull ServerPlayer serverPlayer, @NotNull ServerLevel serverLevel, @NotNull WrapperPlayClientPlayerDigging digging) {
        this.serverPlayer = serverPlayer;
        this.player = serverPlayer.getPlayer();
        this.serverLevel = serverLevel;
        this.digging = digging;
    }

    public void cancel() {
        cancelTask();
        BlockBreakController.BLOCK_BREAK_MODE_MAP.remove(player.getUniqueId());

    }
    public void cancelTask() {
        if (bukkitTask == null) return;
        serverLevel.cancelBlockProgress(player.getEntityId(), digging.getBlockPosition());
        bukkitTask.cancel();
        Bukkit.getScheduler().getMainThreadExecutor(GrimGuardian.getInstance()).execute(() -> {
            new BlockDamageAbortEvent(player
                    , player.getWorld()
                    .getBlockAt(digging.getBlockPosition().getX(), digging.getBlockPosition().getY(), digging.getBlockPosition().getZ())
                    , player.getInventory().getItemInMainHand()).callEvent();
        });

    }

    public void run() {
        final Vector3i vector3i = digging.getBlockPosition();
        final Block targetBlock = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
//                    final BlockPos blockPos = ((CraftBlock) targetBlock).getPosition();
        cancel();


        org.bukkit.inventory.ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemStack tools = SpigotConversionUtil.fromBukkitItemStack(itemStack);
        final BlockBreakSpeed blockBreakSpeed = BlockBreakSpeed.getDefaultBreakTick(player, targetBlock, tools);
        if (!serverPlayer.canInteractWithBlock(targetBlock, 0.0D)) {
            return;
        }
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, itemStack, targetBlock
                , BlockFace.valueOf(digging.getBlockFace().name())
                , EquipmentSlot.HAND, new Vector(targetBlock.getLocation().getX(), targetBlock.getLocation().getY(), targetBlock.getLocation().getZ()));
        Bukkit.getServer().getPluginManager().callEvent(interactEvent);
        if (interactEvent.useInteractedBlock() == Event.Result.DENY) {
            return;
        }
        @NotNull GrimPlayer grimPlayer = Objects.requireNonNull(GrimAPI.INSTANCE.getPlayerDataManager()
                .getPlayer(player.getUniqueId()));
//                    ms.put(player.getUniqueId(), System.currentTimeMillis());
        bukkitTask = new BukkitRunnable() {
            float workingTime = 0;
            int backProgress = 0;

            @Override
            public void run() {
                if (!blockBreakSpeed.isBreakable()) {
                    BlockBreakController.BLOCK_BREAK_MODE_MAP.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }
                Block block = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                float hardness = block.getType().getHardness();
                if (!player.isOnline() || !grimPlayer.platformPlayer.isOnline()) {
                    BlockBreakMode.this.cancel();
                    return;
                }
                if (player.getInventory().getItemInMainHand() == itemStack) {
                    BlockBreakMode.this.cancel();
                    return;
                }
                if (block.getType().isAir()) {
                    BlockBreakMode.this.cancel();
                    return;
                }
                byte progress = (byte) ((workingTime / ((((float) blockBreakSpeed.getTick()) + 6.0F))) * 10);
                BlockImpactEvent blockImpactEvent = new BlockImpactEvent(player, block, BlockFace.valueOf(digging.getBlockFace().name()), player.getInventory().getItemInMainHand(), blockBreakSpeed.isInstantBreak());
                blockImpactEvent.callEvent();
                if (!blockImpactEvent.isCancelled())
                    if (backProgress != progress) {
                        serverLevel.destroyBlockProgress(serverPlayer.getPlayer().getEntityId(), vector3i, progress);
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
                    this.cancel();
                    if (!serverPlayer.canInteractWithBlock(targetBlock, 0.0D)) {
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
