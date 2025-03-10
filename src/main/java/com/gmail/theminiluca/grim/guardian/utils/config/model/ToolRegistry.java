package com.gmail.theminiluca.grim.guardian.utils.config.model;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.StateSetTag;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.Tool;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

@Getter
@EqualsAndHashCode
public class ToolRegistry {

    public static final double DEFAULT_MULTIPLIER = 1.0D;
    public static final int DEFAULT_TIER = 1;


    protected final @NotNull Tool tool;
    protected final int tier;
    protected final double multiplier;
    protected final @NotNull Map<StateSetTag, BlockRegistry> blockRegistries = new HashMap<>();

    protected ToolRegistry(@NotNull Tool tool, int tier, double multiplier, @NotNull ConfigurationSection memorySection) {
        if (memorySection.isSet("multiplier")) {
            multiplier = memorySection.getDouble("multiplier");
        }
        if (memorySection.isSet("tier")) {
            tier = memorySection.getInt("tier");
        }
        this.tool = tool;
        this.tier = tier;
        this.multiplier = multiplier;
    }

    public static ToolRegistry ofTool(@NotNull Tool tool, int tier, double multiplier, @NotNull ConfigurationSection memorySection) {
        ToolRegistry toolRegistry = new ToolRegistry(tool, tier, multiplier, memorySection);

        toolRegistry.load(memorySection);
        return toolRegistry;
    }

    @Override
    public String toString() {
        return "ToolRegistry{" +
                "tool=" + tool +
                ", tier=" + tier +
                ", multiplier=" + multiplier +
                ", blockRegistries=" + blockRegistries +
                '}';
    }

    public void load(@NotNull ConfigurationSection memorySection) {
        ConfigurationSection blockSection = memorySection.getConfigurationSection("blocks");
        if (blockSection != null) {
            for (String key : blockSection.getKeys(false)) {
                ConfigurationSection section = blockSection.getConfigurationSection(key);
                if (section == null) continue;

                StateSetTag stateSetTag = StateSetTag.valueOf(key.toLowerCase());
                if (stateSetTag == null) {
//                    StateSetTag.STATE_MAP.forEach((key1, value) -> GrimGuardian.log().info(key1 + " = " + value + " stats"));
                    GrimGuardian.log().warning("The provided '%s' is not a valid StateType.".formatted(key));
                    continue;
                }
                blockRegistries.put(stateSetTag, new BlockRegistry(stateSetTag, section));
            }
        }
    }


    public double getMultiplier(StateType state) {
        BlockRegistry registry = getBlockRegistry(state);
        if (registry != null) {
            return registry.getMultiplier();
        }
        return multiplier;
    }


    public int getTier(StateType state) {
        BlockRegistry registry = getBlockRegistry(state);
        if (registry != null) {
            return registry.getTier();
        }
        return tier;
    }

    public Boolean isCorrectForDrops(StateType state) {
        BlockRegistry registry = getBlockRegistry(state);
        if (registry != null) {
            return registry.isCorrectForDrops();
        }
        return null;
    }

    public @Nullable BlockRegistry getBlockRegistry(StateType state) {
        for (BlockRegistry registry : blockRegistries.values()) {
            for (StateType type : registry.getStateSetTag()) {
//                GrimGuardian.log().info("%s==%s : %s".formatted(type.getName(), state.getName(), type.getName().equals(state.getName())));
                if (type.getName().equals(state.getName())) {
                    return registry;
                }
            }
        }
        return null;
    }
}
