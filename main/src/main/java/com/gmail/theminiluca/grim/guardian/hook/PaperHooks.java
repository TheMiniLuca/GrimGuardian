package com.gmail.theminiluca.grim.guardian.hook;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;


public interface PaperHooks {
    ServerPlayer getServerPlayer(@NotNull Player player);

    ServerLevel getServerLevel(@NotNull World world);

    static PaperHooks get() {
        return Holder.INSTANCE;
    }

    @Slf4j
    final class Holder {
        private static final PaperHooks INSTANCE;

        static {
            final PaperHooks hooks;
            String minecraftVersion = minecraftVersionFromServerBuildInfo();
            if (minecraftVersion == null) {

                minecraftVersion = Bukkit.getServer().getMinecraftVersion();
            }
            // You probably want to do something smarter here
            try {
                final String underscored = minecraftVersion.replace(".", "_");
                hooks = (PaperHooks) Class.forName("com.gmail.theminiluca.grim.guardian_" + underscored + ".PaperHooks" + underscored).getDeclaredConstructors()[0].newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Could not create PaperHooks for " + minecraftVersion, e);
            }
            INSTANCE = hooks;
        }

        // For bootstrap-time compatibility on applicable versions
        private static @Nullable String minecraftVersionFromServerBuildInfo() {
            try {
                final Class<?> cls = Class.forName("io.papermc.paper.ServerBuildInfo");
                final Method method = cls.getMethod("minecraftVersionId");
                final Object instance = cls.getMethod("buildInfo").invoke(null);
                return (String) method.invoke(instance);
            } catch (final Throwable e) {
                return null;
            }
        }
    }
}
