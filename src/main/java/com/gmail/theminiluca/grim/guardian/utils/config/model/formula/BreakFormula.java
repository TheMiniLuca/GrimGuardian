package com.gmail.theminiluca.grim.guardian.utils.config.model.formula;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.SpigotConversionUtil;
import ac.grim.grimac.utils.inventory.Inventory;
import ac.grim.grimac.utils.latency.CompensatedInventory;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@EqualsAndHashCode
@Getter
public class BreakFormula {
    public static final List<Function> FUNCTIONS = new ArrayList<>();
    public static final Set<String> VARIABLES = new HashSet<>();

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
        VARIABLES.addAll(List.of("eff", "hardness", "tools"));
        List<String> potions = new ArrayList<>();
        for (PotionEffectType type : Registry.EFFECT) {
            potions.add(type.getKey().getKey());
        }
        VARIABLES.addAll(potions);
        FUNCTIONS.add(MIN_FUNCTION);
        FUNCTIONS.add(MAX_FUNCTION);
    }

    private final Formula formula;
    private final String statement;
    private final ExpressionBuilder expression;

    @Override
    public String toString() {
        return "BreakFormula{" +
                "statement='" + statement + '\'' +
                ", formula=" + formula +
                '}';
    }

    public BreakFormula(Formula formula, @NotNull ConfigurationSection section) {
        this.formula = formula;
        this.statement = section.getString(formula.getStatementType().getPath());
        FUNCTIONS.add(createFunction(statement, VARIABLES, "fun_" + formula.name().toLowerCase()));
        expression = createDefaultBuilder(statement);

    }

    public static ExpressionBuilder createDefaultBuilder(String formula) {
        return new ExpressionBuilder(formula)
                .functions(FUNCTIONS).operator(GREATER, GREATER_EQ, LESS, LESS_EQ).variables(VARIABLES);
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

    public double evaluate(GrimPlayer grimPlayer, double hardness) {
        Map<String, Double> map = new HashMap<>();
        map.put("hardness", hardness);

        CompensatedInventory inventory = grimPlayer.getInventory();
        ItemStack held = inventory.getHeldItem();
//        inventory.getHeldItem().
//        try {
//            Method method = inventory.getClass().getDeclaredMethod("getHeldItem");
//            System.out.println(method.invoke(inventory).getClass());
//            held = (ItemStack) method.invoke(inventory);
//        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(Arrays.stream(inventory.getClass().getDeclaredMethods()).map(Method::getName).toList());
//        System.out.println(Arrays.stream(CompensatedInventory.class.getDeclaredMethods()).map(Method::getName).toList() + " class");

        ToolRegistry toolRegistry = ConfigYaml.getInstance().getToolRegistry(SpigotConversionUtil.toBukkitItemStack(held));
        map.put("tools", toolRegistry == null ? ToolRegistry.DEFAULT_MULTIPLIER : toolRegistry.getMultiplier());
//        map.put("tools", BlockBreakController.getAttributeTools(grimPlayer.getInventory().getHeldItem(), true).doubleValue());
        map.put("eff", (double) held.getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY,
                PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        for (PotionType type : PotionTypes.values()) {
            OptionalInt level = grimPlayer.compensatedEntities.getPotionLevelForPlayer(type);
            map.put(type.getName().getKey(), (double) (level.isPresent() ? level.getAsInt() + 1 : 0));
        }
        return expression.build().setVariables(map).evaluate();
    }


}
