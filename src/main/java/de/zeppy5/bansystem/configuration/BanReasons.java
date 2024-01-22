package de.zeppy5.bansystem.configuration;

import de.zeppy5.bansystem.BanSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BanReasons {

    private final File file;
    private final FileConfiguration config;

    public BanReasons(BanSystem banSystem, String fileName) {
        this.file = new File(banSystem.getDataFolder(), fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            banSystem.saveResource(fileName, false);
        }

        this.config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<?, ?>> getList() {
        return config.getMapList("reasons");
    }

    public String getReason(int id) {
        return String.valueOf(Objects.requireNonNull(getList().stream().filter(map -> map.get("id").equals(id)).findFirst().orElse(new HashMap<>())).get("reason"));
    }

    public String getLength(int id) {
        return String.valueOf(Objects.requireNonNull(getList().stream().filter(map -> map.get("id").equals(id)).findFirst().orElse(new HashMap<>())).get("length"));
    }

}
