package com.gmail.theminiluca.grim.guardian.hook;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class ServerPlayer {
    private final Player player;

    public ServerPlayer(@NotNull Player player) {
    this.player = player;
  }

    public abstract boolean canInteractWithBlock(@NotNull Block block, double value);


}
