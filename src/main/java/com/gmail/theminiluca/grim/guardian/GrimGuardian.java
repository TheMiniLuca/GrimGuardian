package com.gmail.theminiluca.grim.guardian;

import ac.grim.grimac.api.events.FlagEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.gmail.theminiluca.grim.guardian.module.FastBreak;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.Buffer;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class GrimGuardian extends JavaPlugin implements Listener {


    private static GrimGuardian instance;

    public static GrimGuardian getInstance() {
        return instance;
    }

    private FastBreak fastBreak;

    public FastBreak getFastBreak() {
        return fastBreak;
    }

    @Override
    public void onLoad() {
        fastBreak = new FastBreak();
        PacketEvents.getAPI().getEventManager().registerListener(fastBreak,
                PacketListenerPriority.HIGH);
        PacketEvents.getAPI().getEventManager().registerListener(new FastBreak.BlockBreakSpeedCancelled(),
                PacketListenerPriority.MONITOR);
    }

//    @EventHandler
//    public void onBreakBreak(BlockDamageEvent event) {
//        event.setCancelled(true);
//    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, getInstance());
        getServer().getPluginManager().registerEvents(fastBreak, getInstance());
        new UpdateChecker(119483).getLastVersion(version -> {
            int latest = Integer.parseInt(version.replaceAll("[^0-9]", ""));
            int current  = Integer.parseInt(this.getDescription().getVersion().replaceAll("[^0-9]", ""));
            if (latest > current) {
                Bukkit.getConsoleSender().sendMessage(Component.text("The latest version has been updated! Please download it from the following link.", NamedTextColor.AQUA));
                Bukkit.getConsoleSender().sendMessage(Component.text("https://www.spigotmc.org/resources/grimguardian.119483/"));
            }
        });
        getCommand("debug").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                FastBreak.DISABLE = !FastBreak.DISABLE;
                commandSender.sendMessage("Fast Break Disabled = " + FastBreak.DISABLE);
                return false;
            }
        });
    }
}