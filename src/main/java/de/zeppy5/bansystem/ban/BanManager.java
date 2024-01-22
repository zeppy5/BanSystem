package de.zeppy5.bansystem.ban;

import de.zeppy5.bansystem.BanSystem;
import org.bukkit.ChatColor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BanManager {
    /*
    Status Codes:
    0: Banned
    1: Unbanned
    2: Expired
    3: Deleted
     */
    public void banPlayer(String uuid, long length, String reason, String bannedBy) {
        String id = UUID.randomUUID().toString();
        long date = System.currentTimeMillis() / 1000;
        long expires = date + length;
        if (length == -1) {
            expires = -1;
        }
        int status = 0;

        try {
            PreparedStatement st = BanSystem.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO bans VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            st.setString(1, uuid);
            st.setString(2, id);
            st.setLong(3, date);
            st.setLong(4, length);
            st.setLong(5, expires);
            st.setString(6, reason);
            st.setInt(7, status);
            st.setString(8, bannedBy);

            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    public void unbanPlayer(String uuid) {
        try {
            PreparedStatement st = BanSystem.getInstance().getMySQL().getConnection().prepareStatement("UPDATE bans SET STATUS = 1 WHERE UUID = ? AND STATUS = 0");
            st.setString(1, uuid);

            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void expireBan(String uuid) {
        try {
            PreparedStatement st = BanSystem.getInstance().getMySQL().getConnection().prepareStatement("UPDATE bans SET STATUS = 2 WHERE UUID = ? AND STATUS = 0");
            st.setString(1, uuid);

            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void removeBan(String ID) {
        try {
            PreparedStatement st = BanSystem.getInstance().getMySQL().getConnection().prepareStatement("UPDATE bans SET STATUS = 3 WHERE ID = ?");
            st.setString(1, ID);

            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public ResultSet checkBanned(String uuid) {
        try {
            PreparedStatement st = BanSystem.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM bans WHERE UUID = ? AND STATUS = 0");
            st.setString(1, uuid);

            ResultSet rs = st.executeQuery();

            if (!rs.isBeforeFirst()) {
                return null;
            }

            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getBans(String uuid) {
        try {
            PreparedStatement st = BanSystem.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM bans WHERE UUID = ? AND NOT STATUS = 3", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st.setString(1, uuid);

            ResultSet rs = st.executeQuery();

            if (!rs.isBeforeFirst()) {
                return null;
            }

            return rs;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String reason(String reason) {
        return ChatColor.RED + "You are banned from this server!\n"
                + ChatColor.BOLD + "Reason: " + ChatColor.BLUE + reason;
    }

    public String getStatusFromID(int status) {
        if (status == 0) return "Banned";
        if (status == 1) return "Unbanned";
        if (status == 2) return "Expired";
        return "";
    }

}
