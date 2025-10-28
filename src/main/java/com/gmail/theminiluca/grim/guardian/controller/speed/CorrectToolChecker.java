package com.gmail.theminiluca.grim.guardian.controller.speed;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.gmail.theminiluca.grim.guardian.event.BlockBreakModeStartEvent;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.BlockRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry.DEFAULT_MULTIPLIER;

@Getter
public class CorrectToolChecker {

    private final @NotNull BlockBreakModeStartEvent startEvent;

    public CorrectToolChecker(@NotNull BlockBreakModeStartEvent startEvent) {
        this.startEvent = startEvent;
    }

    @Getter
    @AllArgsConstructor
    public static class Result {
        private final float speedMultiplier;
        private final boolean correctTool;
    }

    public @NotNull Result getCorrectToolResult() {
        ItemStack itemStack = startEvent.getProtocolItem();
        Block block = startEvent.getBlock();
        boolean isCorrectToolForDrop;

        float speedMultiplier = (float) DEFAULT_MULTIPLIER;
        int tier;

        // 1.13 and below need their own huge methods to support this...

        WrappedBlockState blockState = SpigotConversionUtil.fromBukkitBlockData(block.getBlockData());
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
        return new Result(speedMultiplier, isCorrectToolForDrop);
    }
}
