package de.zeppy5.bansystem;

import de.zeppy5.bansystem.ban.BanListener;
import de.zeppy5.bansystem.ban.BanManager;
import de.zeppy5.bansystem.commands.*;
import de.zeppy5.bansystem.configuration.BanReasons;
import de.zeppy5.bansystem.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BanSystem extends JavaPlugin {

    private static BanSystem instance;

    private MySQL mySQL;

    private BanManager banManager;

    private BanReasons banReasons;

    @Override
    public void onEnable() {
        instance = this;

        banReasons = new BanReasons(this, "reasons.yml");

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        String host = config.getString("host");
        String database = config.getString("database");
        String user = config.getString("user");
        String password = config.getString("password");
        int port = config.getInt("port");

        mySQL = new MySQL(host, database, user, password, port);

        banManager = new BanManager();

        Bukkit.getPluginManager().registerEvents(new BanListener(), this);

        Objects.requireNonNull(getCommand("ban")).setExecutor(new BanCommand());
        Objects.requireNonNull(getCommand("unban")).setExecutor(new UnbanCommand());
        Objects.requireNonNull(getCommand("removeban")).setExecutor(new RemoveBanCommand());
        Objects.requireNonNull(getCommand("listbans")).setExecutor(new ListBansCommand());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BanSystem getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public BanReasons getBanReasons() {
        return banReasons;
    }
}
