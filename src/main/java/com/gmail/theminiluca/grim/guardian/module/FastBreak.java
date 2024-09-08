package com.gmail.theminiluca.grim.guardian.module;

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
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

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


    public static class BlockBreakSpeedCancelled implements PacketListener {
        @Override
        public void onPacketSend(PacketSendEvent event) {
            User user = event.getUser();
            if (user.getUUID() == null) return;
            Player player = Bukkit.getPlayer(user.getUUID());
            if (player == null) {
                return;
            }
            if (event.getPacketType().equals(PacketType.Play.Server.UPDATE_ATTRIBUTES)) {
                if (event.isCancelled()) {
                    GrimGuardian.getInstance().getLogger().severe("I'm not sure which plugin is responsible, " +
                            "but canceling the event causes the FastBreak module to malfunction and not work properly.");
                    GrimGuardian.getInstance().getLogger().severe("It was applied forcefully.");
                }
                WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(event);
                if (user.getEntityId() != packet.getEntityId()) return;
                int i = 0;
                Iterator<WrapperPlayServerUpdateAttributes.Property> iterator = packet.getProperties().iterator();
                while (iterator.hasNext()) {
                    WrapperPlayServerUpdateAttributes.Property attributes = iterator.next();
                    if (!Attributes.PLAYER_BLOCK_BREAK_SPEED.equals(attributes.getAttribute())) continue;
                    GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(player);
                    if (grimPlayer.compensatedEntities.getSelf().getAttributeValue(ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes.PLAYER_BLOCK_BREAK_SPEED) == 0.0F) {
                        iterator.remove();  // 안전하게 리스트에서 요소 제거
                        continue;
                    }
                    attributes.setValue(0.0F);
                }
                if (packet.getProperties().isEmpty()) {
                    event.setCancelled(true);
                }
            }
        }
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
            if (player.getGameMode().equals(GameMode.CREATIVE)) return;
            if (action.equals(DiggingAction.RELEASE_USE_ITEM)) return;
            if (action.equals(DiggingAction.DROP_ITEM)) return;
            if (action.equals(DiggingAction.DROP_ITEM_STACK)) return;
            if (action.equals(DiggingAction.SWAP_ITEM_WITH_OFFHAND)) return;
            if (action.equals(DiggingAction.START_DIGGING)) {
                final ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                final Vector3i vector3i = digging.getBlockPosition();
                final Block targetBlock = player.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                final BlockPos blockPos = ((CraftBlock) targetBlock).getPosition();
                event.setCancelled(true);
                if (runnable.containsKey(user.getUUID())) {
                    stopRunnable(user, digging);
                }
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

                        if ((progress >= 10 || blockDamageEvent.getInstaBreak()) && !blockDamageEvent.isCancelled()) {
                            this.cancel();
                            stopDigging(user, digging.getBlockPosition());
                            ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
                            BlockState iblockdata = ((CraftBlock) block).getNMS();
                            BlockPos blockPos = ((CraftBlock) block).getPosition();
                            if (iblockdata.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                                world.levelEvent(net.minecraft.world.level.block.LevelEvent.SOUND_EXTINGUISH_FIRE, blockPos, 0);
                            } else {
                                world.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK
                                        , blockPos,
                                        net.minecraft.world.level.block.Block.getId(iblockdata));
                            }
                            player.breakBlock(block);
                        }
                    }
                }.runTaskTimer(GrimGuardian.getInstance(), 1L, 1L).getTaskId());
            } else {
                stopRunnable(user, digging);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {

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
