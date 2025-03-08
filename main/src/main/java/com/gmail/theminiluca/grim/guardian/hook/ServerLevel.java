package com.gmail.theminiluca.grim.guardian.hook;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;

@Getter
public abstract class ServerLevel {

    private final World world;

    protected ServerLevel(World world) {
        this.world = world;
    }

    public abstract void levelEvent(Block block);
}
