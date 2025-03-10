package com.gmail.theminiluca.grim.guardian.utils.config.model.tool;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public enum ToolType {
    PICKAXE(MaterialTags.PICKAXES),
    AXE(MaterialTags.AXES),
    SHOVEL(MaterialTags.SHOVELS),
    SWORD(MaterialTags.SWORDS);

    private final boolean hasMaterial;
    @Getter
    private final MaterialSetTag materialTags;

    ToolType(boolean hasMaterial, MaterialSetTag materialTags) {
        this.hasMaterial = hasMaterial;
        this.materialTags = materialTags;
    }

    ToolType(MaterialSetTag materialTags) {
        this.materialTags = materialTags;
        this.hasMaterial = true;
    }


    public boolean hasMaterial() {
        return hasMaterial;
    }

    public static ToolType valueOf(ItemStack itemStack) {
        for (ToolType toolType : ToolType.values()) {
            if (toolType.getMaterialTags().isTagged(itemStack)) {
                return toolType;
            }
        }
        return null;
    }
}