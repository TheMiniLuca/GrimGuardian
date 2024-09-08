package com.gmail.theminiluca.grim.guardian.module;

import ac.grim.grimac.GrimAC;
import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.utils.enums.FluidTag;
import ac.grim.grimac.utils.inventory.EnchantmentHelper;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FastBreak implements PacketListener, Listener {

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
            if (player != null && player.getWorld() == world) {
                double d0 = (double) blockposition.getX() - player.getLocation().getX();
                double d1 = (double) blockposition.getY() - player.getLocation().getY();
                double d2 = (double) blockposition.getZ() - player.getLocation().getZ();

                if (breakPlayer != null && !player.canSee(breakPlayer)) {
                    continue;
                }
                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                    user.sendPacket(new WrapperPlayServerBlockBreakAnimation(user.hashCode() >> 5
                            , blockposition, (byte) progress));
                }
            }
        }
    }

    public void breakNaturally(Player player) {

    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() instanceof PacketType.Play.Server) {
            System.out.println(event.getPacketType().getName());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = PacketEvents.getAPI().getProtocolManager().getUser(player.getUniqueId());
        user.sendPacket(new WrapperPlayServerUpdateAttributes(user.getEntityId(), List.of(new WrapperPlayServerUpdateAttributes
                .Property(Attributes.PLAYER_BLOCK_BREAK_SPEED, 0.0D, new ArrayList<>()))));
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        User user = event.getUser();

        if (event.getPacketType().equals(PacketType.Play.Client.PLAYER_DIGGING)) {
            Player player = (Player) event.getPlayer();
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
            if (event.isCancelled()) return;
            DiggingAction action = digging.getAction();
            AttributeInstance instance = player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
            if (instance == null) {
                return;
            }
            user.sendPacket(new WrapperPlayServerUpdateAttributes(user.getEntityId(), List.of(new WrapperPlayServerUpdateAttributes
                    .Property(Attributes.PLAYER_BLOCK_BREAK_SPEED, 0.0D, new ArrayList<>()))));
            if (player.getGameMode().equals(GameMode.CREATIVE)) return;
            if (action.equals(DiggingAction.RELEASE_USE_ITEM)) return;
            if (action.equals(DiggingAction.DROP_ITEM)) return;
            if (action.equals(DiggingAction.DROP_ITEM_STACK)) return;
            if (action.equals(DiggingAction.SWAP_ITEM_WITH_OFFHAND)) return;
            if (action.equals(DiggingAction.START_DIGGING)) {
                event.setCancelled(true);
                if (runnable.containsKey(user.getUUID())) {
                    stopRunnable(user, digging);
                }
                Vector3i vector3i = digging.getBlockPosition();
                Block defaultBlock = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                org.bukkit.inventory.ItemStack itemStack = player.getInventory().getItemInMainHand();
                ItemStack tools = SpigotConversionUtil.fromBukkitItemStack(itemStack);
                final int breakTime = getDefaultBreakTick(player, defaultBlock, tools);
                GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(player);
                runnable.put(user.getUUID(), new BukkitRunnable() {
                    float workingTime = 0;
                    int backProgress = 0;

                    @Override
                    public void run() {
                        if (breakTime == -2) {
                            this.cancel();
                            return;
                        }
                        Block block = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                        if (!player.isOnline()) {
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
                        byte progress = (byte) ((workingTime / ((((float) breakTime) + 6.0F))) * 10);
                        BlockDamageEvent blockDamageEvent = new BlockDamageEvent(player, block, BlockFace.valueOf(digging.getBlockFace().name())
                                , player.getInventory().getItemInMainHand(), breakTime == -1.0F);
                        Bukkit.getServer().getPluginManager().callEvent(blockDamageEvent);
                        if (!blockDamageEvent.isCancelled())
                            if (backProgress != progress) {
                                destroyBlockProgress(player, player.getWorld(), vector3i, progress);
                            }

                        PotionEffect haste = player.getPotionEffect(PotionEffectType.HASTE);
                        PotionEffect fatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
                        float worktime = 1.0F;
                        if (haste != null) {
                            worktime += ((haste.getAmplifier() + 1) * 0.2F);
                        }
                        if (fatigue != null) {
                            worktime -= ((fatigue.getAmplifier() + 1) * 0.3F);
                        }
                        if (!grimPlayer.packetStateData.packetPlayerOnGround) {
                            worktime /= 5.0F;
                        }
                        if (grimPlayer.fluidOnEyes == FluidTag.WATER) {
                            if (grimPlayer.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21) && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                                worktime *= (float) grimPlayer.compensatedEntities.getSelf().getAttributeValue(ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes.PLAYER_SUBMERGED_MINING_SPEED);
                            } else {
                                if (EnchantmentHelper.getMaximumEnchantLevel(grimPlayer.getInventory(), EnchantmentTypes.AQUA_AFFINITY, ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents
                                        .getAPI().getServerManager().getVersion().toClientVersion()) == 0) {
                                    worktime /= 5.0F;
                                }
                            }
                        }
                        if (!blockDamageEvent.isCancelled()) workingTime += Math.max(worktime, 0);
                        backProgress = progress;

                        if ((progress >= 10 || breakTime == -1.0F) && !blockDamageEvent.isCancelled()) {
                            this.cancel();
                            stopDigging(user, digging.getBlockPosition());
                            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                            PacketPlayInBlockDig
                            entityPlayer.e.a(new BlockPosition(vector3i.getX(), vector3i.getY(), vector3i.getZ()), );

                            entityPlayer.e.a(new BlockPosition(vector3i.getX(), vector3i.getY(), vector3i.getZ()));
                        }
                    }
                }.runTaskTimer(GrimGuardian.getInstance(), 1L, 1L).getTaskId());
            } else {
                stopRunnable(user, digging);
                event.setCancelled(true);
            }
        }
    }

    public Number getAttributeTools(ItemStack itemStack, boolean flag) {
        float speedMultiplier = 1.0f;
        int tier = 0;
        if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.WOOD_TIER)) { // Tier 0
            speedMultiplier = 2.0f;
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.STONE_TIER)) { // Tier 1
            speedMultiplier = 4.0f;
            tier = 1;
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.IRON_TIER)) { // Tier 2
            speedMultiplier = 6.0f;
            tier = 2;
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.DIAMOND_TIER)) { // Tier 3
            speedMultiplier = 8.0f;
            tier = 3;
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.GOLD_TIER)) { // Tier 0
            speedMultiplier = 12.0f;
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.NETHERITE_TIER)) { // Tier 4
            speedMultiplier = 9.0f;
            tier = 4;
        }
        return flag ? speedMultiplier : tier;
    }

    public int getDefaultBreakTick(Player player, Block block, ItemStack itemStack) {
        if (block.getType().getHardness() == 0.0F) return -1;
        if (block.getType().getHardness() < 0.0F) return -2;
        WrappedBlockState blockState = SpigotConversionUtil.fromBukkitBlockData(block.getBlockData());
        boolean isCorrectToolForDrop = false;

        float speedMultiplier = 1.0f;
        float hardness = block.getType().getHardness();

        // 1.13 and below need their own huge methods to support this...
        if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.AXE)) {
            isCorrectToolForDrop = BlockTags.MINEABLE_AXE.contains(blockState.getType());
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.PICKAXE)) {
            isCorrectToolForDrop = BlockTags.MINEABLE_PICKAXE.contains(blockState.getType());
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.SHOVEL)) {
            isCorrectToolForDrop = BlockTags.MINEABLE_SHOVEL.contains(blockState.getType());
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.HOE)) {
            isCorrectToolForDrop = BlockTags.MINEABLE_HOE.contains(blockState.getType());
        }
        if (isCorrectToolForDrop) {
            int tier = getAttributeTools(itemStack, false).intValue();
            speedMultiplier = getAttributeTools(itemStack, true).floatValue();
            if (tier < 3 && BlockTags.NEEDS_DIAMOND_TOOL.contains(blockState.getType())) {
                isCorrectToolForDrop = false;
            } else if (tier < 2 && BlockTags.NEEDS_IRON_TOOL.contains(blockState.getType())) {
                isCorrectToolForDrop = false;
            } else if (tier < 1 && BlockTags.NEEDS_STONE_TOOL.contains(blockState.getType())) {
                isCorrectToolForDrop = false;
            }
        }

        // Shears can mine some blocks faster
        if (itemStack.getType() == ItemTypes.SHEARS) {
            isCorrectToolForDrop = true;

            if (blockState.getType() == StateTypes.COBWEB || BlockTags.LEAVES.contains(blockState.getType())) {
                speedMultiplier = 15.0f;
            } else if (BlockTags.WOOL.contains(blockState.getType())) {
                speedMultiplier = 5.0f;
            } else if (blockState.getType() == StateTypes.VINE ||
                    blockState.getType() == StateTypes.GLOW_LICHEN) {
                speedMultiplier = 2.0f;
            } else {
                isCorrectToolForDrop = blockState.getType() == StateTypes.COBWEB ||
                        blockState.getType() == StateTypes.REDSTONE_WIRE ||
                        blockState.getType() == StateTypes.TRIPWIRE;
            }
        }

        // Swords can also mine some blocks faster
        if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.SWORD)) {
            if (blockState.getType() == StateTypes.COBWEB) {
                speedMultiplier = 15.0f;
            } else if (blockState.getType().getMaterialType() == MaterialType.PLANT ||
                    BlockTags.LEAVES.contains(blockState.getType()) ||
                    blockState.getType() == StateTypes.PUMPKIN ||
                    blockState.getType() == StateTypes.MELON) {
                speedMultiplier = 1.5f;
            }

            isCorrectToolForDrop = blockState.getType() == StateTypes.COBWEB;
        }
        ItemMeta im = SpigotConversionUtil.toBukkitItemStack(itemStack).getItemMeta();
        int digSpeed = 0;
        if (im != null) {
            digSpeed = im.getEnchantLevel(Enchantment.EFFICIENCY);
            if (speedMultiplier > 1.0f) {
                if (digSpeed > 0) {
                    speedMultiplier += digSpeed * digSpeed + 1;
                }
            }
        }
        AttributeInstance instance = player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED);
        if (instance == null || instance.getValue() == 0.0f || instance.getDefaultValue() == 0.0f || instance.getBaseValue() == 0.0f) {
            return -2;
        }
        speedMultiplier *= (float) instance.getValue();

        PotionEffect haste = player.getPotionEffect(PotionEffectType.HASTE);
        PotionEffect fatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
        float instant = (getAttributeTools(itemStack, true).floatValue() + (((digSpeed) * (digSpeed)) + 1))
                * (1.0f + (0.2f * (haste != null ? haste.getAmplifier() + 1 : 0)));
        if (speedMultiplier == 0.0F) {
            return -2;
        }
        float damage = speedMultiplier / hardness;

        boolean canHarvest = !blockState.getType().isRequiresCorrectTool() || isCorrectToolForDrop;
        if (instant >= hardness * 30 && isCorrectToolForDrop && fatigue == null) {
            return -1; // instant break
        }
        if (canHarvest) {
            damage /= 30;
        } else {
            damage /= 100;
        }

        return Math.round(1 / damage);
    }
}
