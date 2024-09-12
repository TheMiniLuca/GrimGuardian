package com.gmail.theminiluca.grim.guardian;

import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final int resourseId;

    public UpdateChecker(int resourseId) {
        this.resourseId = resourseId;
    }

    public void getLastVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(GrimGuardian.getInstance(), () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourseId).openStream()) {
                Scanner scanner = new Scanner(inputStream);
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}