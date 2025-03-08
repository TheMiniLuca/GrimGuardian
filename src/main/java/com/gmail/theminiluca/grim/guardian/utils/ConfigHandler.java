package com.gmail.theminiluca.grim.guardian.utils;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.gmail.theminiluca.grim.guardian.module.BlockBreakController;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import org.bukkit.Registry;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class ConfigHandler {

    public static ExpressionBuilder HASTE_EQUATION = null;
    public static ExpressionBuilder MINING_FATIGUE_EQUATION = null;
    public static ExpressionBuilder INSTANT_EQUATION = null;
    public static ExpressionBuilder EFFICIENCY_EQUATION = null;

    public static final Set<String> VARIABLES = new HashSet<>();
    static {
        VARIABLES.addAll(List.of("eff", "hardness", "tools"));
        List<String> potions = new ArrayList<>();
        for (PotionEffectType type : Registry.EFFECT) {
            potions.add(type.getKey().getKey());
        }
        VARIABLES.addAll(potions);
    }
    public static final List<Function> FUNCTIONS = new ArrayList<>();
    public static ConfigManager.Option HASTE_EQUATION_PATH = new ConfigManager.Option("block-break-controller.haste.equation", String.class);
    public static ConfigManager.Option EFFICIENCY_EQUATION_PATH = new ConfigManager.Option("block-break-controller.efficiency.equation", String.class);
    public static ConfigManager.Option MINING_FATIGUE_EQUATION_PATH = new ConfigManager.Option("block-break-controller.mining_fatigue.equation", String.class);
    public static ConfigManager.Option INSTANT_EQUATION_PATH = new ConfigManager.Option("block-break-controller.instant.condition", String.class);

    public static ConfigManager.Option WOOD_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.wood.multiplier", Double.class);
    public static ConfigManager.Option STONE_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.wood.multiplier", Double.class);
    public static ConfigManager.Option IRON_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.wood.multiplier", Double.class);
    public static ConfigManager.Option DIAMOND_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.wood.multiplier", Double.class);
    public static ConfigManager.Option NETHERITE_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.wood.multiplier", Double.class);
    public static ConfigManager.Option GOLD_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.wood.multiplier", Double.class);
    public static ConfigManager.Option HARVEST_CORRECT = new ConfigManager.Option("block-break-controller.tools.harvest.correct", Double.class);
    public static ConfigManager.Option HARVEST_INCORRECT = new ConfigManager.Option("block-break-controller.tools.harvest.incorrect", Double.class);

    public static ConfigManager.Option SHEARS_COBWEB_OR_LEAVES_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.shears.cobweb_or_leaves.multiplier", Double.class);
    public static ConfigManager.Option SHEARS_COBWEB_OR_LEAVES_CORRECT = new ConfigManager.Option("block-break-controller.tools.shears.cobweb_or_leaves.is_correct_tool", Boolean.class);

    public static ConfigManager.Option SHEARS_WOOL_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.shears.wool.multiplier", Double.class);
    public static ConfigManager.Option SHEARS_WOOL_CORRECT = new ConfigManager.Option("block-break-controller.tools.shears.wool.is_correct_tool", Boolean.class);

    public static ConfigManager.Option SHEARS_VINE_OR_GLOW_LICHEN_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.shears.vine_or_glow_lichen.multiplier", Double.class);
    public static ConfigManager.Option SHEARS_VINE_OR_GLOW_LICHEN_CORRECT = new ConfigManager.Option("block-break-controller.tools.shears.vine_or_glow_lichen.is_correct_tool", Boolean.class);

    public static ConfigManager.Option SWORD_COBWEB_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.sword.cobweb.multiplier", Double.class);
    public static ConfigManager.Option SWORD_COBWEB_CORRECT = new ConfigManager.Option("block-break-controller.tools.sword.cobweb.is_correct_tool", Boolean.class);

    public static ConfigManager.Option SWORD_PUMPKIN_OR_MELON_MULTIPLIER = new ConfigManager.Option("block-break-controller.tools.sword.pumpkin_or_melon.multiplier", Double.class);
    public static ConfigManager.Option SWORD_PUMPKIN_OR_MELON_CORRECT = new ConfigManager.Option("block-break-controller.tools.sword.pumpkin_or_melon.is_correct_tool", Boolean.class);





    private static final Function MIN_FUNCTION = new Function("min", 2) {
        @Override
        public double apply(double... args) {
            return Math.min(args[0], args[1]);
        }
    };

    private static final Function MAX_FUNCTION = new Function("max", 2) {
        @Override
        public double apply(double... args) {
            return Math.max(args[0], args[1]);
        }
    };

    private static final Operator GREATER_EQ = new Operator(">=", 2, true, 4) {

        @Override
        public double apply(double[] values) {
            return (values[0] >= values[1] ? 1d : 0d);
        }
    };
    private static final Operator GREATER = new Operator(">", 2, true, 4) {

        @Override
        public double apply(double[] values) {
            return (values[0] > values[1] ? 1d : 0d);
        }
    };

    private static final Operator LESS_EQ = new Operator("<=", 2, true, 4) {

        @Override
        public double apply(double[] values) {
            return (values[0] <= values[1] ? 1d : 0d);
        }
    };
    private static final Operator LESS = new Operator("<", 2, true, 4) {

        @Override
        public double apply(double[] values) {
            return (values[0] < values[1] ? 1d : 0d);
        }
    };

    static {
        FUNCTIONS.add(MIN_FUNCTION);
        FUNCTIONS.add(MAX_FUNCTION);
    }

    public Function createFunction(String formula, Set<String> variableNames, String name) {
        List<String> list = sort(formula, variableNames);
        return new Function(name, list.size()) {
            @Override
            public double apply(double... doubles) {
                Map<String, Double> map = new HashMap<>();
                for (int i = 0; i < doubles.length; i++) {
                    map.put(list.get(i), doubles[i]);
                }
                return createDefaultBuilder(formula).variables(new HashSet<>(list)).build().setVariables(map).evaluate();
            }
        };
    }

    private List<String> sort(String formula, Set<String> variableNames) {
        List<String> sort = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (char c : formula.toCharArray()) {
            builder.append(c);
            for (String find : variableNames) {
                if (builder.toString().contains(find)) {
                    if (!sort.contains(builder.toString())) {
                        if (!builder.toString().contains("fun_"))
                            sort.add(find);
                        builder = new StringBuilder();
                        break;
                    }
                }
            }
        }
        return sort;
    }

    public static ExpressionBuilder createDefaultBuilder(String formula) {
        return new ExpressionBuilder(formula)
                .functions(FUNCTIONS).operator(GREATER, GREATER_EQ, LESS, LESS_EQ).variables(VARIABLES);
    }

    public static double evaluate(ExpressionBuilder expressionBuilder, GrimPlayer grimPlayer, double hardness) {
        Map<String, Double> map = new HashMap<>();
        map.put("hardness", hardness);
        map.put("tools", BlockBreakController.getAttributeTools(grimPlayer.getInventory().getHeldItem(), true).doubleValue());
        map.put("eff", (double) grimPlayer.getInventory().getHeldItem().getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY, PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        for (PotionType type : PotionTypes.values()) {
            OptionalInt level = grimPlayer.compensatedEntities.getPotionLevelForPlayer(type);
            map.put(type.getName().getKey(), (double) (level.isPresent() ? level.getAsInt() + 1 : 0));
        }
        return expressionBuilder.build().setVariables(map).evaluate();
    }


    public static String getString(ConfigManager.Option option) {
        return ConfigManager.instance.getString(option);
    }

    public ConfigHandler(Plugin plugin) {
        new ConfigManager(plugin).setup();
        FUNCTIONS.add(createFunction(getString(HASTE_EQUATION_PATH), VARIABLES, "fun_haste"));
        FUNCTIONS.add(createFunction(getString(MINING_FATIGUE_EQUATION_PATH), VARIABLES, "fun_mining_fatigue"));
        FUNCTIONS.add(createFunction(getString(INSTANT_EQUATION_PATH), VARIABLES, "fun_instant"));
        FUNCTIONS.add(createFunction(getString(EFFICIENCY_EQUATION_PATH), VARIABLES, "fun_efficiency"));
        HASTE_EQUATION = createDefaultBuilder(getString(
                HASTE_EQUATION_PATH));
        MINING_FATIGUE_EQUATION = createDefaultBuilder(getString(
                HASTE_EQUATION_PATH));
        EFFICIENCY_EQUATION = createDefaultBuilder(
                getString(EFFICIENCY_EQUATION_PATH));
        INSTANT_EQUATION = createDefaultBuilder(
                getString(INSTANT_EQUATION_PATH));
    }
}
