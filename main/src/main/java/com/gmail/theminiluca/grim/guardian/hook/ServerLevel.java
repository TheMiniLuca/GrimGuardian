package com.gmail.theminiluca.grim.guardian.hook;

import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class ServerLevel {

    private final World world;

    protected ServerLevel(World world) {
        this.world = world;
    }

    public abstract void levelEvent(Block block);

    public abstract void destroyBlockProgress(int breakerId, @NotNull Vector3i pos, int progress);

    public abstract void cancelBlockProgress(int breakerId, @NotNull Vector3i pos);
}
