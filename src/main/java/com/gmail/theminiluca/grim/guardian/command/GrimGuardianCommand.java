package com.gmail.theminiluca.grim.guardian.command;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.gmail.theminiluca.grim.guardian.utils.config.model.MultiToolRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.ToolRegistry;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.MineralTool;
import com.gmail.theminiluca.grim.guardian.utils.config.model.tool.ToolType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.gmail.theminiluca.grim.guardian.controller.BlockBreakController.DISABLE;

public class GrimGuardianCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return true;
        }
        if ("reload".equals(args[0])) {
            try {
                ConfigYaml.getInstance().load();
                sender.sendMessage(Component.text("콘피그 로드를 완료했습니다.").color(NamedTextColor.WHITE));
            } catch (IOException | InvalidConfigurationException e) {
                sender.sendMessage(Component.text("콘피그 로드를 실패했습니다.").color(NamedTextColor.RED));
                GrimGuardian.getInstance().getLogger().log(Level.WARNING, "{0}", new Object[]{e});
            }
        }
        if ("disable".equals(args[0])) {
            DISABLE = !DISABLE;
            sender.sendMessage(DISABLE + " 값으로 설정했습니다.");
        }
        if ("debug".equals(args[0])) {
            if (!(sender instanceof Player player)) return false;
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            sender.sendMessage(Component.text("-----"));
            ToolRegistry registry = ConfigYaml.getInstance().getToolRegistry(itemStack);
            player.sendMessage(Component.text((registry == null ? ToolRegistry.DEFAULT_MULTIPLIER + " null" : registry.getMultiplier()) + " multiplier"));
            player.sendMessage(Component.text((registry == null ? ToolRegistry.DEFAULT_TIER + " null" : registry.getTier()) + " tier"));
            @Nullable ToolType toolType = ToolType.valueOf(itemStack);
            @Nullable MineralTool mineral = MineralTool.valueOf(itemStack);
            player.sendMessage(Component.text(mineral + " mineral"));
            player.sendMessage(Component.text(toolType + " toolType"));
            if (registry == null) {
                player.sendMessage("registry is null");
                return true;
            }
            player.sendMessage(registry.getBlockRegistries().size() + " --------");
            registry.getBlockRegistries().values().forEach(key -> {
                player.sendMessage(Component.text((key == null ?  "null multiplier" : key.getMultiplier() + " multiplier")));
                player.sendMessage(Component.text((key == null ?  "null name" : key.getStateSetTag().getName() + " name")));
                player.sendMessage(Component.text((key == null ?  "null tier" : key.getTier() + " tier")));
            });
            if (registry instanceof MultiToolRegistry multiToolRegistry) {
                player.sendMessage(multiToolRegistry.getToolRegistries().size() + " <-------->");
                multiToolRegistry.getToolRegistries().values().forEach(key -> {
                    player.sendMessage(Component.text((key == null ?  "null multiplier" : key.getMultiplier() + " multiplier")));
                    player.sendMessage(Component.text((key == null ?  "null block" : key.getBlockRegistries() + " block")));
                    player.sendMessage(Component.text((key == null ?  "null tier" : key.getTier() + " tier")));
                });
            }
        }
        if ("debug1".equals(args[0])) {
            if (!(sender instanceof Player player)) return false;
            ItemStack itemStack = player.getInventory().getItemInMainHand();
//            ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack
//                    item = SpigotConversionUtil.fromBukkitItemStack(itemStack);
            ToolComponent component = itemStack.getItemMeta().getTool();
            player.sendMessage(component.getDefaultMiningSpeed() + " float");
            player.sendMessage(component.getDamagePerBlock() + " per float");
            component.getRules().forEach(toolRule -> {
                player.sendMessage("-----");
                player.sendMessage(toolRule.getSpeed() + " per speed");
                player.sendMessage(toolRule.getBlocks() + " per blocks");
            });
//            player.sendMessage(component.getRules().forEach(); + " rules float");
//            item.getNBT().toString()
//            @NotNull Optional<ItemTool> component = item.getComponent(ComponentTypes.TOOL);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            return new ArrayList<>();
        }
        return switch (args.length) {
            case 2 -> {
                if ("debug".equals(args[0])) {
                    List<String> list = StateTypes.values().stream().map(StateType::getName).toList();
                    yield completeLastWord(args[1], list);
                }
                yield new ArrayList<>();
            }
            default -> new ArrayList<>();
        };
    }

    private List<String> completeLastWord(String input, final Iterable<String> suggestions) {
        final List<String> list = new ArrayList<>();

        for (final String suggestion : suggestions)
            list.add(suggestion);

        List<String> completions = new ArrayList<>();
        for (String option : list) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                completions.add(option);
            }
        }
        return completions;
    }
}
