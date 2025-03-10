package com.gmail.theminiluca.grim.guardian.utils.config.model;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.StateSetTag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
public class BlockRegistry {

    private final @NotNull StateSetTag stateSetTag;
    private final double multiplier;
    private final int tier;
    private final boolean correctForDrops;

    public BlockRegistry(@NotNull StateSetTag stateSetTag, @NotNull ConfigurationSection memorySection) {
        this.stateSetTag = stateSetTag;
        if (memorySection.isSet("multiplier")) {
            this.multiplier = memorySection.getDouble("multiplier");
        } else {
            this.multiplier = ToolRegistry.DEFAULT_MULTIPLIER;
        }
        if (memorySection.isSet("tier")) {
            this.tier = memorySection.getInt("tier");
        } else {
            this.tier = ToolRegistry.DEFAULT_TIER;
        }
        if (memorySection.isSet("is_correct_tool")) {
            this.correctForDrops = memorySection.getBoolean("is_correct_tool");
        } else {
            this.correctForDrops = true;
        }
    }

    @Override
    public String toString() {
        return "BlockRegistry{" +
                "stateSetTag=" + stateSetTag +
                ", multiplier=" + multiplier +
                ", tier=" + tier +
                ", correctForDrops=" + correctForDrops +
                '}';
    }
}
