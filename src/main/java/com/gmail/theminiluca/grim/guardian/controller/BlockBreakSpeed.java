package com.gmail.theminiluca.grim.guardian.controller;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.BlockRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.Formula;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry.DEFAULT_MULTIPLIER;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockBreakSpeed {
    private final int tick;
    private final boolean correctToolForDrop;

    public boolean isInstantBreak() {
        return tick == -1;
    }

    public boolean isBreakable() {
        return tick != -2;
    }

    public static BlockBreakSpeed instant(boolean correctToolForDrop) {
        return new BlockBreakSpeed(-1, correctToolForDrop);
    }

    public static BlockBreakSpeed indestructible() {
        return new BlockBreakSpeed(-2, false);
    }

    public static BlockBreakSpeed breakable(int tick, boolean requiresCorrectTool) {
        return new BlockBreakSpeed(tick, requiresCorrectTool);
    }


    public static BlockBreakSpeed getDefaultBreakTick(Player player, Block block, ItemStack itemStack) {
        if (block.getType().getHardness() == 0.0F) return instant(true);
        if (block.getType().getHardness() < 0.0F) return indestructible();
        WrappedBlockState blockState = SpigotConversionUtil.fromBukkitBlockData(block.getBlockData());
        boolean isCorrectToolForDrop;

        float speedMultiplier = (float) DEFAULT_MULTIPLIER;
        float hardness = block.getType().getHardness();
        int tier;

        // 1.13 and below need their own huge methods to support this...


        @Nullable ToolRegistry toolRegistry = ConfigYaml.getInstance().getToolRegistry(SpigotConversionUtil.toBukkitItemStack(itemStack));
        boolean correct = false;
        if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.AXE)) {
            correct = BlockTags.MINEABLE_AXE.contains(blockState.getType());
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.PICKAXE)) {
            correct = BlockTags.MINEABLE_PICKAXE.contains(blockState.getType());
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.SHOVEL)) {
            correct = BlockTags.MINEABLE_SHOVEL.contains(blockState.getType());
        } else if (itemStack.getType().hasAttribute(ItemTypes.ItemAttribute.HOE)) {
            correct = BlockTags.MINEABLE_HOE.contains(blockState.getType());
        }
        if (correct) {
            speedMultiplier = (float) (toolRegistry == null ? DEFAULT_MULTIPLIER : toolRegistry.getMultiplier());
            tier = (toolRegistry == null ? ToolRegistry.DEFAULT_TIER : toolRegistry.getTier());
            if (tier < 3 && BlockTags.NEEDS_DIAMOND_TOOL.contains(blockState.getType())) {
                correct = false;
            } else if (tier < 2 && BlockTags.NEEDS_IRON_TOOL.contains(blockState.getType())) {
                correct = false;
            } else if (tier < 1 && BlockTags.NEEDS_STONE_TOOL.contains(blockState.getType())) {
                correct = false;
            }
        }
        Boolean flag = toolRegistry != null ? toolRegistry.isCorrectForDrops(blockState.getType()) : null;
        if (flag == null) {
            isCorrectToolForDrop = correct;
        } else {
            isCorrectToolForDrop = flag;
        }

        BlockRegistry registry = toolRegistry == null ? null : toolRegistry.getBlockRegistry(blockState.getType());
        if (registry != null) {
            isCorrectToolForDrop = registry.isCorrectForDrops();
            speedMultiplier = (float) registry.getMultiplier();
        }


//        @NotNull Optional<com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool> component =
//                itemStack.getComponent(com.github.retrooper.packetevents.protocol.component.ComponentTypes.TOOL);
//        if (component.isPresent()) {
//            @NotNull com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool itemTool = component.get();
//            speedMultiplier = itemTool.getDefaultMiningSpeed();
//            loop:
//            for (@NotNull ItemTool.Rule rule : itemTool.getRules()) {
//                @Nullable MappedEntitySet<StateType.Mapped> set = rule.getBlocks();
//                if (set == null) continue;
//                @Nullable List<StateType.Mapped> entities = set.getEntities();
//                if (entities == null || entities.isEmpty()) continue;
//                for (@NotNull StateType.Mapped state : set.getEntities()) {
//                    if (state.getStateType().equals(blockState.getType())) {
//                        @Nullable Float speed = rule.getSpeed();
//                        if (speed == null) {
//                            continue loop;
//                        }
//                        speedMultiplier = speed;
//                        break loop;
//                    }
//                }
//            }
//        }
        @NotNull final GrimPlayer grimPlayer = Objects.requireNonNull(GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(player.getUniqueId()));
        int digSpeed = itemStack.getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY, PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
        if (speedMultiplier > 1.0f) {
            if (grimPlayer.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21) && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
                speedMultiplier += (float) grimPlayer.compensatedEntities.self.getAttributeValue(Attributes.MINING_EFFICIENCY);
            }
            if (digSpeed > 0) {

                speedMultiplier += (float) ConfigYaml.getInstance().getFormula(Formula.EFFICIENCY).evaluate(grimPlayer, block.getType().getHardness());
            }
        }

        double blockBreak = GrimGuardian.getInstance().getServerPlayer(player).getBlockBreakSpeed();
        if (blockBreak == 0.0f) {
            return indestructible();
        }
        speedMultiplier *= (float) blockBreak;


        PotionEffect fatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
        if (speedMultiplier == 0.0F) {
            return indestructible();
        }
        float damage = speedMultiplier / hardness;

        boolean canHarvest = !blockState.getType().isRequiresCorrectTool() || isCorrectToolForDrop;
        if (ConfigYaml.getInstance().getFormula(Formula.INSTANT).evaluate(grimPlayer, block.getType().getHardness()) == 1d && isCorrectToolForDrop && fatigue == null) {
            return instant(true);
        }
        if (canHarvest) {
            damage /= (float) ConfigYaml.getInstance().getFormula(Formula.CORRECT).evaluate(grimPlayer, block.getType().getHardness());
        } else {
            damage /= (float) ConfigYaml.getInstance().getFormula(Formula.INCORRECT).evaluate(grimPlayer, block.getType().getHardness());
        }
        return breakable(Math.round(1 / damage), isCorrectToolForDrop);
    }
}
