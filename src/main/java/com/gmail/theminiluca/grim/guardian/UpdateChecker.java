package com.gmail.theminiluca.grim.guardian;

import org.bukkit.Bukkit;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final int resourceId;

    public UpdateChecker(int resourceId) {
        this.resourceId = resourceId;
    }

    public void getLastVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(GrimGuardian.getInstance(), () -> {

            try {
                URI uri = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId);
                URL url = uri.toURL();

                try (InputStream inputStream = url.openStream();
                     Scanner scanner = new Scanner(inputStream)) {
                    if (scanner.hasNext()) {
                        consumer.accept(scanner.next());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}