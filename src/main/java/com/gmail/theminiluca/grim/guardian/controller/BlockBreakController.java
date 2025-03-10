package com.gmail.theminiluca.grim.guardian.controller;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.enums.FluidTag;
import ac.grim.grimac.utils.inventory.EnchantmentHelper;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.Formula;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockBreakController implements PacketListener, Listener {


    public static boolean DISABLE = false;
    public static final Map<UUID, Integer> runnable = new HashMap<>();

    public void stopDigging(User user, Vector3i vector3i) {
        Player player = Bukkit.getPlayer(user.getUUID());
        if (player == null) return;
        destroyBlockProgress(player, player.getWorld(), vector3i, -1);
    }


    public void stopRunnable(User user, WrapperPlayClientPlayerDigging digging) {
        if (runnable.containsKey(user.getUUID())) {
            stopDigging(user, digging.getBlockPosition());
            if (!runnable.containsKey(user.getUUID())) return;
            Bukkit.getScheduler().cancelTask(runnable.get(user.getUUID()));
            runnable.remove(user.getUUID());
            Player player = Bukkit.getPlayer(user.getUUID());
            if (player == null) return;
            Bukkit.getScheduler().runTask(GrimGuardian.getInstance(), () -> {
                BlockDamageAbortEvent blockDamageAbortEvent = new BlockDamageAbortEvent(player
                        , player.getWorld().getBlockAt(digging.getBlockPosition().getX(), digging.getBlockPosition().getY(), digging.getBlockPosition().getZ()), player.getInventory().getItemInMainHand());
                Bukkit.getPluginManager().callEvent(blockDamageAbortEvent);
            });
        }
    }


    public void destroyBlockProgress(Player breakPlayer, World world, Vector3i blockposition, int progress) {
        for (User user : PacketEvents.getAPI().getProtocolManager().getUsers()) {
            if (user.getUUID() == null) continue;
            Player player = Bukkit.getPlayer(user.getUUID());
            if (player == null || player.getWorld() != world) continue;

            double d0 = blockposition.getX() - player.getLocation().getX();
            double d1 = blockposition.getY() - player.getLocation().getY();
            double d2 = blockposition.getZ() - player.getLocation().getZ();
            double distanceSquared = d0 * d0 + d1 * d1 + d2 * d2;

            if (distanceSquared >= 1024.0D) continue;
            if (breakPlayer != null && !player.canSee(breakPlayer)) continue;

            user.sendPacket(new WrapperPlayServerBlockBreakAnimation(user.hashCode() >> 5, blockposition, (byte) progress));
        }
    }


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
            if (action.equals(DiggingAction.START_DIGGING)) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(GrimGuardian.getInstance(), () -> {
                    final ServerPlayer serverPlayer = GrimGuardian.getInstance().getServerPlayer(player);
                    final Vector3i vector3i = digging.getBlockPosition();
                    final Block targetBlock = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
//                    final BlockPos blockPos = ((CraftBlock) targetBlock).getPosition();
                    if (runnable.containsKey(user.getUUID())) {
                        stopRunnable(user, digging);
                    }
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
                    @NotNull GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(player);
//                    ms.put(player.getUniqueId(), System.currentTimeMillis());
                    runnable.put(user.getUUID(), new BukkitRunnable() {
                        float workingTime = 0;
                        int backProgress = 0;

                        @Override
                        public void run() {
                            if (!blockBreakSpeed.isBreakable()) {
                                this.cancel();
                                return;
                            }
                            Block block = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                            if (!player.isOnline() || !grimPlayer.bukkitPlayer.isOnline()) {
                                stopRunnable(user, digging);
                                this.cancel();
                                return;
                            }
                            if (player.getInventory().getItemInMainHand() == itemStack) {
                                this.cancel();
                                return;
                            }
                            if (block.getType().equals(Material.AIR)) {
                                this.cancel();
                                return;
                            }
                            byte progress = (byte) ((workingTime / ((((float) blockBreakSpeed.getTick()) + 6.0F))) * 10);
                            BlockDamageEvent blockDamageEvent = new BlockDamageEvent(player, block, player.getInventory().getItemInMainHand(), blockBreakSpeed.isInstantBreak());
                            Bukkit.getServer().getPluginManager().callEvent(blockDamageEvent);
                            if (!blockDamageEvent.isCancelled())
                                if (backProgress != progress) {
                                    destroyBlockProgress(player, player.getWorld(), vector3i, progress);
                                }

                            float worktime = 1.0F;
//                                int hasteLevel = Math.max(digSpeed.isEmpty() ? 0 : digSpeed.getAsInt(), conduit.isEmpty() ? 0 : conduit.getAsInt());
//                            worktime *= (float) ConfigHandler.evaluate(ConfigHandler.HASTE_EQUATION, grimPlayer, block.getType().getHardness());
                            worktime *= (float) ConfigYaml.getInstance().getFormula(Formula.HASTE).evaluate(grimPlayer, block.getType().getHardness());
                            worktime *= (float) ConfigYaml.getInstance().getFormula(Formula.MINING_FATIGUE).evaluate(grimPlayer, block.getType().getHardness());
                            if (!grimPlayer.packetStateData.packetPlayerOnGround) {
                                worktime /= 5.0F;
                            }
                            if (grimPlayer.fluidOnEyes == FluidTag.WATER) {
                                if (grimPlayer.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21) && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                                    worktime *= (float) grimPlayer.compensatedEntities.self.getAttributeValue(ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes.PLAYER_SUBMERGED_MINING_SPEED);
                                } else {
                                    if (EnchantmentHelper.getMaximumEnchantLevel(grimPlayer.getInventory(), EnchantmentTypes.AQUA_AFFINITY, ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents
                                            .getAPI().getServerManager().getVersion().toClientVersion()) == 0) {
                                        worktime /= 5.0F;
                                    }
                                }
                            }
                            if (!blockDamageEvent.isCancelled()) workingTime += Math.max(worktime, 0);
                            backProgress = progress;

                            if ((progress >= 10 || blockDamageEvent.getInstaBreak()) && !blockDamageEvent.isCancelled()) {
                                this.cancel();
                                if (!serverPlayer.canInteractWithBlock(targetBlock, 0.0D)) {
                                    return;
                                }
                                stopDigging(user, digging.getBlockPosition());
                                ServerLevel world = GrimGuardian.getInstance().getServerLevel(player.getWorld());
                                world.levelEvent(block);
                                if (!blockBreakSpeed.isCorrectToolForDrop())
                                    block.getDrops().clear();
                                player.breakBlock(block);

                            }
                        }
                    }.runTaskTimer(GrimGuardian.getInstance(), 0L, 1L).getTaskId());
                });
            } else {
                stopRunnable(user, digging);
                event.setCancelled(true);
            }
        }
    }
}
