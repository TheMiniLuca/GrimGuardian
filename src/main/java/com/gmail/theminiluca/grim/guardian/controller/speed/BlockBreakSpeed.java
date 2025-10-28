package com.gmail.theminiluca.grim.guardian.controller.speed;

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
import com.gmail.theminiluca.grim.guardian.controller.BlockBreakContext;
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

    public static BlockBreakSpeed getVanillaBlockBreakSpeed(@NotNull BlockBreakContext context) {
        float blockHardness = context.getBlockHardness();
        if (blockHardness == 0.0F) return instant(true);
        if (blockHardness < 0.0F) return indestructible();
        WrappedBlockState blockState = SpigotConversionUtil.fromBukkitBlockData(context.getBlock().getBlockData());
        boolean isCorrectToolForDrop;

        float speedMultiplier;

        CorrectToolChecker.Result result = context.getCorrectToolChecker().getCorrectToolResult();
        isCorrectToolForDrop = result.isCorrectTool();
        speedMultiplier = result.getSpeedMultiplier();

        // 1.13 and below need their own huge methods to support this...




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
        float damage = speedMultiplier / blockHardness;

        boolean canHarvest = !blockState.getType().isRequiresCorrectTool() || isCorrectToolForDrop;
        if (ConfigYaml.getInstance().getFormula(Formula.INSTANT).evaluate(grimPlayer, blockHardness) == 1d && isCorrectToolForDrop && fatigue == null) {
            return instant(true);
        }
        if (canHarvest) {
            damage /= (float) ConfigYaml.getInstance().getFormula(Formula.CORRECT).evaluate(grimPlayer, blockHardness);
        } else {
            damage /= (float) ConfigYaml.getInstance().getFormula(Formula.INCORRECT).evaluate(grimPlayer, blockHardness);
        }
        return breakable(Math.round(1 / damage), isCorrectToolForDrop);
    }
}