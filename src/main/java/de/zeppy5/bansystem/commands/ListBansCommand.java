package de.zeppy5.bansystem.commands;

import de.zeppy5.bansystem.BanSystem;
import de.zeppy5.bansystem.util.MojangAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ListBansCommand implements CommandExecutor, TabCompleter {

    private final int itemsPerPage = 5;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        new Thread(() -> {
            if (!sender.hasPermission("ban.unban")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
                return;
            }

            if (args.length < 1) {
                syntax(sender);
                return;
            }

            Player player = Bukkit.getPlayer(args[0]);
            String uuid = null;

            if (player != null) {
                uuid = String.valueOf(player.getUniqueId());
            }

            int n = 0;
            while (uuid == null && n < 3) {
                uuid = MojangAPI.getUUID(args[0]);
                n++;
                if (uuid == null) {
                    sender.sendMessage(ChatColor.YELLOW + "Failed to fetch player info from Mojang API. Trying again");
                }
            }

            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Failed to fetch player info from Mojang API three times. Giving up");
                return;
            }

            try {
                ResultSet rs = BanSystem.getInstance().getBanManager().getBans(uuid);
                int size = 0;
                if (rs != null)
                {
                    rs.last();
                    size = rs.getRow();
                    rs.beforeFirst();
                }

                if (size == 0) {
                    sender.sendMessage(ChatColor.GREEN + "Player has no bans");
                    return;
                }

                int page = 0;
                if (args.length == 2) {
                    try {
                        page = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException e) {
                        syntax(sender);
                        return;
                    }
                }

                if ((page + 1) > (int) Math.ceil((double)size / itemsPerPage)) {
                    sender.sendMessage(ChatColor.RED + "Invalid page number, there are " + ChatColor.BLUE + (int) Math.ceil((double)size / itemsPerPage) + ChatColor.RED + " pages");
                    return;
                }

                if (page < 0) {
                    sender.sendMessage(ChatColor.RED + "Invalid page number, there are " + ChatColor.BLUE + (int) Math.ceil((double)size / itemsPerPage) + ChatColor.RED + " pages");
                    return;
                }

                printBans(sender, rs, size, page);


            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "An internal Error occurred");
            }
        }).start();

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (!sender.hasPermission("ban.list")) {
            return null;
        }

        if (args.length == 0) {
            return list;
        }

        if (args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getDisplayName()));
        }

        List<String> completerList = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase();
        for (String s : list) {
            String s1 = s.toLowerCase();
            if(s1.startsWith(currentArg)) {
                completerList.add(s);
            }
        }

        return completerList;
    }

    private void syntax(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.RED + "Usage: /listbans <Player> [<Page>]");
    }

    private void printBans(CommandSender sender, ResultSet rs, int size, int page) throws SQLException {

        sender.sendMessage(ChatColor.YELLOW + "--------------------------------------------------");
        sender.sendMessage(ChatColor.GREEN + "Page " + (page + 1) + " / " + (int) Math.ceil((double)size / itemsPerPage));

        rs.beforeFirst();

        for (int i = 0; i < (page * itemsPerPage); i++) {
            rs.next();
        }

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        int s = Math.min((size - (page * itemsPerPage)), itemsPerPage);

        for (int i = 0; i < s; i++) {
            rs.next();
            sender.sendMessage(ChatColor.DARK_PURPLE + "⌈¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯⌉");

            String id = rs.getString("ID");
            String reason = rs.getString("REASON");
            String bannedBy = rs.getString("BANNED_BY");

            String bannedOn = dateFormat.format(Date.from(Instant.ofEpochSecond(rs.getLong("DATE"))));

            long expires = rs.getLong("EXPIRES");
            String date;
            if (expires == -1) {
                date = "PERMANENT";
            } else {
                date = dateFormat.format(Date.from(Instant.ofEpochSecond(expires)));
            }

            String status = BanSystem.getInstance().getBanManager().getStatusFromID(rs.getInt("STATUS"));

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&'
                    , "&bBan-ID: &a" + id
                    + "&b\nReason: &a" + reason
                    + "&b\nBan Date: &a" + bannedOn
                    + "&b\nExpire Date: &a" + date
                    + "&b\nBanned by: &a" + bannedBy
                    + "&b\nStatus: &a" + status));
            sender.sendMessage(ChatColor.DARK_PURPLE + "⌊_________________________⌋");
        }
        sender.sendMessage(ChatColor.YELLOW + "--------------------------------------------------");
    }
}
