package com.gmail.theminiluca.grim.guardian;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.gmail.theminiluca.grim.guardian.module.AttributeController;
import com.gmail.theminiluca.grim.guardian.module.BlockBreakController;
import com.gmail.theminiluca.grim.guardian.utils.ConfigHandler;
import com.gmail.theminiluca.grim.guardian.utils.ConfigYaml;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class GrimGuardian extends JavaPlugin implements Listener {


    @Getter
    private static GrimGuardian instance;


    @Getter
    private BlockBreakController blockBreakController;


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
            int current = Integer.parseInt(this.getDescription().getVersion().replaceAll("[^0-9]", ""));
            if (latest > current) {
                Bukkit.getConsoleSender().sendMessage(Component.text("The latest version has been updated! Please download it from the following link.", NamedTextColor.AQUA));
                Bukkit.getConsoleSender().sendMessage(Component.text("https://www.spigotmc.org/resources/grimguardian.119483/"));
            }
        });
        try {
            new ConfigYaml().load();
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        getCommand("debug").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                if (!(commandSender instanceof Player player)) return false;
                Block block = player.getTargetBlockExact(4);
                assert block != null;

                BlockData blockData = Bukkit.createBlockData(Material.CHEST);
                Location loc = player.getLocation();
                if (blockData instanceof Chest chest) {
                    chest.setFacing(getBlockFace(new Location(block.getWorld(), block.getX(), block.getY()
                            , block.getZ(), loc.getYaw(), loc.getPitch())));
                    block.setBlockData(blockData);
                }
//                Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
//                player.sendMessage(block.getBlockData().getAsString());
                return false;
            }
        });

    }

    private @NotNull BlockFace getBlockFace(@NotNull Location location) {
        @Range(to = -180, from = 180) float yaw = location.getYaw();
        BlockFace face;
        if (isAround(yaw, 0)) {
//                face = BlockFace.NORTH;
            face = BlockFace.NORTH;
        } else if (isAround(yaw, 90)) {
//                face = BlockFace.WEST;
            face = BlockFace.EAST;
        } else if (isAround(yaw, 180)) {
//                face = BlockFace.SOUTH;
            face = BlockFace.SOUTH;
        } else if (isAround(yaw, -90)) {
//                face = BlockFace.EAST;
            face = BlockFace.WEST;
        } else {
            throw new NullPointerException();
        }
        return face;
    }

    public boolean isAround(float yaw, float base) {
        return yaw >= base - 45 && yaw <= base + 45;
    }
}