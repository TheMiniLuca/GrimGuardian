package com.gmail.theminiluca.grim.guardian.utils.config.model.tool;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode
public class Tool {
    private final @Nullable Material material;
    private final @Nullable ToolType toolType;
    private final @Nullable MineralTool mineral;

    public Tool(@Nullable Material material) {
        this.material = material;
        this.toolType = null;
        this.mineral = null;
    }

    public Tool(@Nullable ToolType toolType) {
        this.material = null;
        this.toolType = toolType;
        this.mineral = null;
    }

    public Tool(@Nullable MineralTool mineral) {
        this.material = null;
        this.toolType = null;
        this.mineral = mineral;
    }


    public Tool(@Nullable ToolType toolType, @Nullable MineralTool mineral) {
        this.material = null;
        this.toolType = toolType;
        this.mineral = mineral;
    }

    @Override
    public String toString() {
        return "Tool{" +
                "material=" + material +
                ", toolType=" + toolType +
                ", mineral=" + mineral +
                '}';
    }
}