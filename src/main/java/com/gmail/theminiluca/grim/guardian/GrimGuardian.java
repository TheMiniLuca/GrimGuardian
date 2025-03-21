package com.gmail.theminiluca.grim.guardian;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.gmail.theminiluca.grim.guardian.command.GrimGuardianCommand;
import com.gmail.theminiluca.grim.guardian.hook.PaperHooks;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.gmail.theminiluca.grim.guardian.controller.AttributeController;
import com.gmail.theminiluca.grim.guardian.controller.BlockBreakController;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@Getter
@Setter
@Slf4j
public class GrimGuardian extends JavaPlugin implements Listener, PaperHooks{


    public static Logger log() {
        return GrimGuardian.getInstance().getLogger();
    }
    @Getter
    private static GrimGuardian instance;


    @Getter
    private BlockBreakController blockBreakController;


    @Override
    public ServerPlayer getServerPlayer(@NotNull Player player) {
        return PaperHooks.get().getServerPlayer(player);
    }

    @Override
    public ServerLevel getServerLevel(@NotNull World world) {
        return PaperHooks.get().getServerLevel(world);
    }


    @Override
    public void onLoad() {
        blockBreakController = new BlockBreakController();
        PacketEvents.getAPI().getEventManager().registerListener(blockBreakController,
                PacketListenerPriority.MONITOR);
        PacketEvents.getAPI().getEventManager().registerListener(new AttributeController(),
                PacketListenerPriority.MONITOR);
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, getInstance());
        getServer().getPluginManager().registerEvents(blockBreakController, getInstance());
//        new ConfigHandler(this);

        try {
            ConfigYaml.getInstance().load();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.WARNING, "{0}", new Object[]{e});
        }
        new UpdateChecker(119483).getLastVersion(version -> {
            int latest = Integer.parseInt(version.replaceAll("[^0-9]", ""));
            int current = Integer.parseInt(this.getDescription().getVersion().replaceAll("[^0-9]", ""));
            if (latest > current) {
                Bukkit.getConsoleSender().sendMessage(Component.text("The latest version has been updated! Please download it from the following link.", NamedTextColor.AQUA));
                Bukkit.getConsoleSender().sendMessage(Component.text("https://www.spigotmc.org/resources/grimguardian.119483/"));
            }
        });
        GrimGuardianCommand grimGuardianCommand = new GrimGuardianCommand();
        Objects.requireNonNull(getCommand("grimguardian")).setExecutor(grimGuardianCommand);
        Objects.requireNonNull(getCommand("grimguardian")).setTabCompleter(grimGuardianCommand);

    }
}
