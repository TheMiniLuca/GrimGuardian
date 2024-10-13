package com.gmail.theminiluca.grim.guardian;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.gmail.theminiluca.grim.guardian.module.AttributeController;
import com.gmail.theminiluca.grim.guardian.module.BlockBreakController;
import com.gmail.theminiluca.grim.guardian.utils.ConfigHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class GrimGuardian extends JavaPlugin implements Listener {


    private static GrimGuardian instance;

    public static GrimGuardian getInstance() {
        return instance;
    }

    private BlockBreakController blockBreakController;

    public BlockBreakController getBlockBreakController() {
        return blockBreakController;
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
        new ConfigHandler(this);
        new UpdateChecker(119483).getLastVersion(version -> {
            int latest = Integer.parseInt(version.replaceAll("[^0-9]", ""));
            int current  = Integer.parseInt(this.getDescription().getVersion().replaceAll("[^0-9]", ""));
            if (latest > current) {
                Bukkit.getConsoleSender().sendMessage(Component.text("The latest version has been updated! Please download it from the following link.", NamedTextColor.AQUA));
                Bukkit.getConsoleSender().sendMessage(Component.text("https://www.spigotmc.org/resources/grimguardian.119483/"));
            }
        });
    }
}