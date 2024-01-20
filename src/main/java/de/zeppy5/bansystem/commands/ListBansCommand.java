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
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (!sender.hasPermission("ban.unban")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
            return false;
        }

        if (args.length > 2) {
            syntax(sender);
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        String uuid;
        if (player != null) {
            uuid = String.valueOf(player.getUniqueId());
        } else {
            uuid = MojangAPI.getUUID(args[0]);
        }

        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "Player does not exist!");
            return false;
        }

        try {
            ResultSet result = BanSystem.getInstance().getBanManager().getBans(uuid);
            int size = 0;
            if (result != null)
            {
                result.last();
                size = result.getRow();
            }

            if (size == 0) {
                sender.sendMessage(ChatColor.GREEN + "Player has no bans");
                return false;
            }

            ResultSet rs = BanSystem.getInstance().getBanManager().getBans(uuid);

            int page = 0;
            if (args.length == 2) {
                page = Integer.parseInt(args[1]);
            }

            if (page > (int) Math.ceil((double)size / 10)) {
                sender.sendMessage(ChatColor.RED + "Invalid page number, there are " + ChatColor.BLUE + (int) Math.ceil((double)size / 10) + ChatColor.RED + " pages");
            }

            printBans(sender, rs, size, page);


        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "An internal Error occurred");
        }

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
        sender.sendMessage(ChatColor.GREEN + "Page " + page + " / " + (int) Math.ceil((double)size / 10));

        for (int i = 0; i < (page * 10); i++) {
            rs.next();
        }

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        for (int i = 0; i < 10; i++) {
            sender.sendMessage("*****");

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


            sender.sendMessage(ChatColor.AQUA + "Ban-id: " + id
                    + "\nReason: " + reason
                    + "\nBan Date: " + bannedOn
                    + "\nExpire Date: " + date
                    + "\nBanned by: " + bannedBy
                    + "\nStatus: " + status);
            rs.next();
            sender.sendMessage("*****");
        }
        sender.sendMessage(ChatColor.YELLOW + "--------------------------------------------------");
    }
}
