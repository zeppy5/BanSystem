package de.zeppy5.bansystem;

import de.zeppy5.bansystem.ban.BanListener;
import de.zeppy5.bansystem.ban.BanManager;
import de.zeppy5.bansystem.commands.BanCommand;
import de.zeppy5.bansystem.commands.ListBansCommand;
import de.zeppy5.bansystem.commands.RemoveBanCommand;
import de.zeppy5.bansystem.commands.UnbanCommand;
import de.zeppy5.bansystem.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BanSystem extends JavaPlugin {

    private static BanSystem instance;

    private MySQL mySQL;

    private BanManager banManager;

    @Override
    public void onEnable() {
        instance = this;

        mySQL = new MySQL("localhost", "ban", "root", "", 3306);

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
}
