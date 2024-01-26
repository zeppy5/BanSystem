package de.zeppy5.bansystem.ban;

import de.zeppy5.bansystem.BanSystem;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class BanListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        ResultSet rs = BanSystem.getInstance().getBanManager().checkBanned(String.valueOf(event.getPlayer().getUniqueId()));

        if (rs != null) {
            try {
                while (rs.next()) {
                    if (((System.currentTimeMillis() / 1000) > rs.getLong("EXPIRES")) && rs.getLong("EXPIRES") != -1) {
                        BanSystem.getInstance().getBanManager().expireBan(rs.getString("ID"));
                    } else {
                        String r = reason(rs);
                        event.setKickMessage(r);
                        event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                event.getPlayer().kickPlayer(ChatColor.RED + "An internal Error occurred");
            }
        }
    }

    public String reason(ResultSet rs) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            long expires = rs.getLong("EXPIRES");
            String date;
            if (expires == -1) {
                date = "PERMANENT";
            } else {
                date = dateFormat.format(Date.from(Instant.ofEpochSecond(expires)));
            }

            String reason = rs.getString("REASON");

            String banID = rs.getString("ID");
            return ChatColor.RED + "You are banned from this server!\n"
                    + ChatColor.BOLD + "Reason: " + ChatColor.BLUE + reason
                    + ChatColor.RESET + ChatColor.RED + "\nExpires: " + ChatColor.ITALIC + ChatColor.BLUE + date
                    + ChatColor.RESET + ChatColor.GRAY + "\nBan-ID: " + ChatColor.ITALIC + banID;
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
}
