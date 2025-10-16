package com.gmail.theminiluca.grim.guardian.utils.config;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.utils.config.model.MultiToolRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.BreakFormula;
import com.gmail.theminiluca.grim.guardian.utils.config.model.formula.Formula;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.Tool;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.MineralTool;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.ToolType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry.DEFAULT_MULTIPLIER;
import static com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry.DEFAULT_TIER;

@Slf4j
@Getter
public class ConfigYaml extends YamlConfiguration {

    @Getter
    private static final ConfigYaml instance = new ConfigYaml();

    ConfigYaml() {
    }

    private final Map<Tool, ToolRegistry> toolsMap = new HashMap<>();
    private final Map<Formula, BreakFormula> formulaMap = new HashMap<>();

    public void load() throws IOException, InvalidConfigurationException {
        GrimGuardian.getInstance().saveResource("config.yml", false);
        load(new File(GrimGuardian.getInstance().getDataFolder(), "config.yml"));
//        InputStream stream = GrimGuardian.getInstance().getResource("config.yml");
//        if (stream == null) return;
//        load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        ConfigurationSection section = getConfigurationSection("block-break-controller");
        if (section == null) return;
        ConfigurationSection toolSection = section.getConfigurationSection("tools");
        loadTools(toolSection);
        ConfigurationSection formula = section.getConfigurationSection("formula");
        loadFormula(formula);
    }

    @Contract("null -> null")
    public @Nullable ToolRegistry getToolRegistry(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        Tool tool = new Tool(itemStack.getType());
        if (toolsMap.containsKey(tool)) {
            return toolsMap.get(tool);
        }
        @Nullable ToolType toolType = ToolType.valueOf(itemStack);
        @Nullable MineralTool mineral = MineralTool.valueOf(itemStack);
        if (mineral != null && toolType != null) {
            tool = new Tool(mineral);
            if (toolsMap.containsKey(tool)) {
                ToolRegistry toolRegistry = toolsMap.get(tool);
                if (toolRegistry instanceof MultiToolRegistry multi) {
                    tool = new Tool(toolType, mineral);
                    if (multi.getToolRegistries().containsKey(tool))
                        return multi.getToolRegistries().get(tool);
                } else
                    return toolRegistry;
                return toolRegistry;
            }
        }
        if (toolType != null) {
            tool = new Tool(toolType);
            if (toolsMap.containsKey(tool)) {
                return toolsMap.get(tool);
            }
        }


        return null;
    }

    public @NotNull BreakFormula getFormula(Formula formula) {
        return formulaMap.get(formula);
    }

    private <T extends Enum<T>> T valueOf(Class<T> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadMineral(@Nullable ConfigurationSection configurationSection) {
        if (configurationSection == null) return;
        for (String key : configurationSection.getKeys(false)) {
            @Nullable MineralTool mineral = valueOf(MineralTool.class, key.toUpperCase()); // 우선순위 2
            if (mineral == null) continue;
            Tool tool = new Tool(mineral);
            ConfigurationSection mineralSection = configurationSection.getConfigurationSection(key);
            if (!configurationSection.isSet(key)) {
                throw new IllegalArgumentException("The <root>.%s key cannot be null.");
            }
            if (mineralSection == null) {
                throw new IllegalArgumentException("%s key's value must always be of type ConfigurationSection.".formatted(key));
            }
            toolsMap.put(tool, MultiToolRegistry.ofMultiTool(tool, mineralSection));

        }
    }

    private void loadMaterials(@Nullable ConfigurationSection configurationSection) {
        if (configurationSection == null) return;
        for (String key : configurationSection.getKeys(false)) {
            @Nullable Material material = Material.matchMaterial(key.toUpperCase()); // 우선순위 1
            if (material != null) {
                Tool tool = new Tool(material);
                ConfigurationSection materialSection = configurationSection.getConfigurationSection(key);
                if (!configurationSection.isSet(key)) {
                    throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted(key));
                }
                if (materialSection == null) {
                    throw new IllegalArgumentException("%s key's value must always be of type ConfigurationSection.".formatted(key));
                }
                toolsMap.put(tool, ToolRegistry.ofTool(tool, DEFAULT_TIER, DEFAULT_MULTIPLIER, materialSection));
            }
        }
    }

    private void loadToolType(@Nullable ConfigurationSection configurationSection) {
        if (configurationSection == null) return;
        for (String key : configurationSection.getKeys(false)) {
            @Nullable ToolType toolType = valueOf(ToolType.class, key.toUpperCase()); // 우선순위 2
            if (toolType != null) {
                Tool tool = new Tool(toolType);
                ConfigurationSection mineralSection = configurationSection.getConfigurationSection(key);
                if (!configurationSection.isSet(key)) {
                    throw new IllegalArgumentException("The <root>.%s key cannot be null.");
                }
                if (mineralSection == null) {
                    throw new IllegalArgumentException("%s key's value must always be of type ConfigurationSection.".formatted(key));
                }
                toolsMap.put(tool, MultiToolRegistry.ofMultiTool(tool, mineralSection));
            }
        }
    }

    private void loadTools(@Nullable ConfigurationSection configurationSection) {
        if (configurationSection == null) return;
        loadMineral(configurationSection.getConfigurationSection("mineral"));
        loadToolType(configurationSection.getConfigurationSection("material-tags"));
        loadMaterials(configurationSection.getConfigurationSection("materials"));
//
//
//        }
//        for (MineralTool mineral : MineralTool.values()) {
//            Tool tool = new Tool(mineral);
//            ConfigurationSection mineralSection = configurationSection.getConfigurationSection(mineral.name().toLowerCase());
//            if (mineralSection == null)
//                throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted(mineral.name()));
//            toolsMap.put(tool, new ToolRegistry(tool, mineralSection));
//        }

//        for (ToolMaterial material : ToolMaterial.values()) {
//            Tool tool = new Tool(toolType, material);
//            ConfigurationSection materialSection = toolTypeSection.getConfigurationSection(material.name().toLowerCase());
//            if (materialSection == null)
//                throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted(material.name()));
//            toolsMap.put(tool, new ToolRegistry(tool, materialSection));
//        }
//        for (ToolType toolType : ToolType.values()) {
//            ConfigurationSection toolTypeSection = configurationSection.getConfigurationSection(toolType.name().toLowerCase());
//            if (toolTypeSection == null)
//                throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted(toolType.name()));
//            if (!toolType.hasMaterial()) {
//                Tool tool = new Tool(toolType);
//                toolsMap.put(tool, new ToolRegistry(tool, toolTypeSection));
//                continue;
//            }
//            for (ToolMaterial material : ToolMaterial.values()) {
//                Tool tool = new Tool(toolType, material);
//                ConfigurationSection materialSection = toolTypeSection.getConfigurationSection(material.name().toLowerCase());
//                if (materialSection == null)
//                    throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted(material.name()));
//                toolsMap.put(tool, new ToolRegistry(tool, materialSection));
//            }
//        }
    }

    private void loadFormula(@Nullable ConfigurationSection configurationSection) {
        if (configurationSection == null) return;
        for (Formula formula : Formula.values()) {
            String key = formula.name().toLowerCase();
            ConfigurationSection formulaSelection = configurationSection.getConfigurationSection(key);
            if (!configurationSection.isSet(key)) {
                throw new IllegalArgumentException("The <root>.%s key cannot be null.");
            }
            if (formulaSelection == null) {
                throw new IllegalArgumentException("The <root>.%s key cannot be null.".formatted(key));
            }
            formulaMap.put(formula, new BreakFormula(formula, formulaSelection));
        }
    }
}
