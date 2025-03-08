package com.gmail.theminiluca.grim.guardian.utils;

import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigYaml extends YamlConfiguration {

    public ConfigYaml() {
    }

    public void load() throws IOException, InvalidConfigurationException {
        load(new File(GrimGuardian.getInstance().getConfig().getCurrentPath()));
        for (String key : getKeys(false)) {
            get(key);
        }
    }
}
