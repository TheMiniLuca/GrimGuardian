package com.gmail.theminiluca.grim.guardian;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.gmail.theminiluca.grim.guardian.command.GrimGuardianCommand;
import com.gmail.theminiluca.grim.guardian.controller.AttributeController;
import com.gmail.theminiluca.grim.guardian.hook.PaperHooks;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import com.gmail.theminiluca.grim.guardian.listener.BukkitListener;
import com.gmail.theminiluca.grim.guardian.utils.config.ConfigYaml;
import com.google.common.base.Preconditions;
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
import java.util.*;
import java.util.logging.Level;

@Getter
@Setter
@Slf4j
public class GrimGuardian extends JavaPlugin implements Listener, PaperHooks{



    public static boolean ENABLE = true;
    @Getter
    private static GrimGuardian instance;


    @Override
    public ServerPlayer getServerPlayer(@NotNull Player player) {
        return getServerPlayer(player.getUniqueId());
    }
    public ServerPlayer getServerPlayer(@NotNull User user) {
        return getServerPlayer(user.getUUID());
    }

    public ServerPlayer getServerPlayer(@NotNull UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        Preconditions.checkNotNull(player);
        return players.computeIfAbsent(player, k -> PaperHooks.get().getServerPlayer(player));
    }

    private final @NotNull Map<World, ServerLevel> worlds = new WeakHashMap<>();
    private final @NotNull Map<Player, ServerPlayer> players = new WeakHashMap<>();

    @Override
    public ServerLevel getServerLevel(@NotNull World world) {
        return worlds.computeIfAbsent(world, k -> PaperHooks.get().getServerLevel(world));
    }


    @Override
    public void onLoad() {
        PacketEvents.getAPI().getEventManager().registerListener(new AttributeController(),
                PacketListenerPriority.HIGHEST);
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, getInstance());
        getServer().getPluginManager().registerEvents(new BukkitListener(), getInstance());

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
