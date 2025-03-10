package com.gmail.theminiluca.grim.guardian.utils.config.model.tool;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public enum MineralTool {
    WOOD(MaterialTags.WOODEN_TOOLS),
    STONE(MaterialTags.STONE_TOOLS),
    IRON(MaterialTags.IRON_TOOLS),
    GOLD(MaterialTags.GOLDEN_TOOLS),
    DIAMOND(MaterialTags.DIAMOND_TOOLS),
    NETHERITE(MaterialTags.NETHERITE_TOOLS);

    private final MaterialSetTag materialTags;

    MineralTool(MaterialSetTag materialTags) {
        this.materialTags = materialTags;
    }

    public static MineralTool valueOf(ItemStack itemStack) {
        for (MineralTool mineralTool : MineralTool.values()) {
            if (mineralTool.getMaterialTags().isTagged(itemStack)) {
                return mineralTool;
            }
        }
        return null;
    }
}