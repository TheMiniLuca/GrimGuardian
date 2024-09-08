package com.gmail.theminiluca.grim.guardian;

import ac.grim.grimac.api.events.FlagEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.gmail.theminiluca.grim.guardian.module.FastBreak;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, getInstance());
    }
}