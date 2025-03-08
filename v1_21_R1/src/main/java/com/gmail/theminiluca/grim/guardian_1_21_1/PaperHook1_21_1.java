package com.gmail.theminiluca.grim.guardian_1_21_1;

import com.gmail.theminiluca.grim.guardian.hook.PaperHooks;
import com.gmail.theminiluca.grim.guardian.hook.ServerLevel;
import com.gmail.theminiluca.grim.guardian.hook.ServerPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperHook1_21_1 implements PaperHooks {
    @Override
    public ServerPlayer getServerPlayer(@NotNull Player player) {
        return new ServerPlayer1_21_1(player);
    }

    @Override
    public ServerLevel getServerLevel(@NotNull World world) {
        return new ServerLevel1_21_1(world);
    }
}
