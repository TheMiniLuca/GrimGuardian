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
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.controller.speed.BlockBreakSpeed;
import com.gmail.theminiluca.grim.guardian.controller.speed.CorrectToolChecker;
import com.gmail.theminiluca.grim.guardian.event.BlockImpactEvent;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.Formula;
import com.google.common.base.Preconditions;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.gmail.theminiluca.grim.guardian.listener.BukkitListener.BLOCK_BREAK_MODE_MAP;

@Setter
@Getter
public class BlockBreakMode {



    private final @NotNull BlockBreakContext context;
    private final @NotNull BlockBreakSpeed blockBreakSpeed;
    protected float totalProgress = 0;
    protected float tickProgress = 0;
    protected byte backProgress = 0;
    protected @Nullable BukkitTask bukkitTask;

    public BlockBreakMode(@NotNull BlockBreakContext context) {
        this.context  =context;
        this.blockBreakSpeed = createBlockBreakSpeed();
    }

    public void cancel() {
        cancelTask();
        BLOCK_BREAK_MODE_MAP.remove(context.player.getUniqueId());

    }

    protected @NotNull BlockBreakSpeed createBlockBreakSpeed() {
        return BlockBreakSpeed.getVanillaBlockBreakSpeed(, block, packetItemStack, blockHardness, correctToolChecker);
    }

//    public void destroyBlockProgress(final int progress) {
//        serverLevel.destroyBlockProgress(serverPlayer.getPlayer().getEntityId() + 1, block, progress);
//    }
    protected void cancelTask() {
        if (bukkitTask == null) return;
//        destroyBlockProgress(player, player.getWorld(), );
        destroyBlockProgress(-1);
//        serverLevel.cancelBlockProgress(player.getEntityId() + 1, block);
        bukkitTask.cancel();
        bukkitTask = null;
//        Bukkit.getScheduler().getMainThreadExecutor(GrimGuardian.getInstance()).execute(() -> {
//            new BlockDamageAbortEvent(player
//                    , player.getWorld()
//                    .getBlockAt(digging.getBlockPosition().getX(), digging.getBlockPosition().getY(), digging.getBlockPosition().getZ())
//                    , player.getInventory().getItemInMainHand()).callEvent();
//        });

    }

    public void destroyBlockProgress(int progress) {
        for (User user : PacketEvents.getAPI().getProtocolManager().getUsers()) {
            if (user.getUUID() == null) continue;
            Player player = Bukkit.getPlayer(user.getUUID());
            if (player == null || player.getWorld() != world) continue;

            double d0 = vector3i.getX() - player.getLocation().getX();
            double d1 = vector3i.getY() - player.getLocation().getY();
            double d2 = vector3i.getZ() - player.getLocation().getZ();
            double distanceSquared = d0 * d0 + d1 * d1 + d2 * d2;

            if (distanceSquared >= 1024.0D) continue;
            if (!player.canSee(this.player)) continue;

            user.sendPacket(new WrapperPlayServerBlockBreakAnimation(user.getEntityId() + 1, vector3i, (byte) progress));
        }
    }

    protected final boolean checkBreakConditions() {
        if (isUnbreakable()) {
            cancel();
            return true;
        }
        if (isPlayerOffline()) {
            cancel();
            return true;
        }
        if (isItemChanged()) {
            cancel();
            return true;
        }
        if (isBlockGone()) {
            cancel();
            return true;
        }
        return false;
    }

    protected boolean isUnbreakable() {
        return !blockBreakSpeed.isBreakable();
    }

    protected boolean isPlayerOffline() {
        return !player.isOnline() || !grimPlayer.platformPlayer.isOnline();
    }

    protected boolean isItemChanged() {
        return player.getInventory().getItemInMainHand() == itemStack;
    }

    protected boolean isBlockGone() {
        return block.getType().isAir();
    }

    protected float blockhardness() {
        return block.getType().getHardness();
    }


    protected byte getProgress() {
        return (byte) ((totalProgress / ((((float) blockBreakSpeed.getTick()) + 6.0F))) * 10);
    }

    protected float multiplyHaste() {
        return (float) ConfigYaml.getInstance().getFormula(Formula.HASTE).evaluate(grimPlayer, blockHardness);
    }

    protected float multiplyMiningFatigue() {
        return (float) ConfigYaml.getInstance().getFormula(Formula.MINING_FATIGUE).evaluate(grimPlayer, blockHardness);
    }

    protected float multiplyOffGround() {
        return 0.2f;
    }

    protected float multiplyAttribute() {
        return (float) grimPlayer.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
    }

    protected float multiplyExtra() {
        return 1.0f;
    }

    protected float multiplyAquaAffinity() {
        return 0.2f;
    }

    protected float defaultWorkTime() {
        return 1.0f;
    }
    protected float requiredProgress() {
        return 10.0f;
    }

    protected void setTotalProgress() {
        totalProgress += Math.max(tickProgress, 0);
    }

    public void run() {
        cancel();
        bukkitTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (checkBreakConditions()) {
                    BlockBreakMode.this.cancel();
                    return;
                }
                update();
            }
        }.runTaskTimerAsynchronously(GrimGuardian.getInstance(), 0L, 1L);
    }

    public void breakBlock() {
        Bukkit.getScheduler().getMainThreadExecutor(GrimGuardian.getInstance()).execute(() -> {
            player.breakBlock(block);
        });
    }
    public void update() {
        byte progress = getProgress();
        BlockImpactEvent blockImpactEvent = new BlockImpactEvent(player, block, BlockFace.valueOf(event.getBlockFace().name()), player.getInventory().getItemInMainHand(), blockBreakSpeed.isInstantBreak(), this);
        blockImpactEvent.callEvent();
        if (!blockImpactEvent.isCancelled())
            if (backProgress != progress) {
                destroyBlockProgress(progress);
//                destroyBlockProgress(progress);
            }
        if (!blockImpactEvent.isCancelled()) {
            tickProgress = defaultWorkTime();
            tickProgress *= multiplyHaste();
            tickProgress *= multiplyMiningFatigue();
            if (!grimPlayer.packetStateData.packetPlayerOnGround) {
                tickProgress *= multiplyOffGround();
            }
            if (grimPlayer.fluidOnEyes==FluidTag.WATER) {
                if (grimPlayer.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21)
                        && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                    tickProgress *= multiplyAttribute();
                } else {
                    if (EnchantmentHelper.getMaximumEnchantLevel(grimPlayer.getInventory(), EnchantmentTypes.AQUA_AFFINITY, ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents
                            .getAPI().getServerManager().getVersion().toClientVersion())==0) {
                        tickProgress *= multiplyAquaAffinity();
                    }
                }
            }
            tickProgress *= multiplyExtra();
            if (!blockImpactEvent.isCancelled()) setTotalProgress();
            backProgress = progress;
        }
        if ((progress >= requiredProgress() || blockImpactEvent.isInstantBreak()) && !blockImpactEvent.isCancelled()) {
            if (!serverPlayer.canInteractWithBlock(block, 1.0D)) {
                return;
            }
            BlockBreakMode.this.cancel();
            ServerLevel world = GrimGuardian.getInstance().getServerLevel(player.getWorld());
            world.levelEvent(block);
            if (!blockBreakSpeed.isCorrectToolForDrop())
                block.getDrops().clear();
            breakBlock();

        }
    }
}
