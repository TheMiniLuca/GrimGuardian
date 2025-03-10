package com.gmail.theminiluca.grim.guardian.utils.config.model;

import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.Tool;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.ToolType;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class MultiToolRegistry extends ToolRegistry {

    private final @NotNull Map<Tool, ToolRegistry> toolRegistries = new HashMap<>();

    private MultiToolRegistry(@NotNull Tool tool, int tier, double defaultMultiplier, @NotNull ConfigurationSection memorySection) {
        super(tool, tier, defaultMultiplier, memorySection);
    }

    public static MultiToolRegistry ofMultiTool(@NotNull Tool tool, @NotNull ConfigurationSection memorySection) {
        double defaultMultiplier;
        if (memorySection.isSet("multiplier")) {
            defaultMultiplier = memorySection.getDouble("multiplier");
        } else {
            defaultMultiplier = DEFAULT_MULTIPLIER;
        }
        int tier;
        if (memorySection.isSet("tier")) {
            tier = memorySection.getInt("tier");
        } else {
            throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted("tier"));
        }
        MultiToolRegistry registry = new MultiToolRegistry(tool, tier, defaultMultiplier, memorySection);
        registry.load(memorySection);
        return registry;
    }

    public void load(@NotNull ConfigurationSection memorySection) {
        ConfigurationSection toolSection = memorySection.getConfigurationSection("tools");
        if (toolSection != null) {
            for (ToolType toolType : ToolType.values()) {
                ConfigurationSection section = toolSection.getConfigurationSection(toolType.name().toLowerCase());
                if (section == null) continue;
                Tool tools = new Tool(toolType, tool.getMineral());
                this.toolRegistries.put(tools, ToolRegistry.ofTool(tools, this.tier, this.multiplier, section));
            }
        }
        super.load(memorySection);
    }

    @Override
    public String toString() {
        return "MultiToolRegistry{" +
                "toolRegistries=" + toolRegistries +
                ", tool=" + tool +
                ", tier=" + tier +
                ", multiplier=" + multiplier +
                ", blockRegistries=" + blockRegistries +
                '}';
    }
}
