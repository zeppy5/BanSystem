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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("ban.ban")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
            return false;
        }

        if (args.length < 3) {
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

        // String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length - 1));

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length - 2; i++) {
            stringBuilder.append(args[i+2]);
            stringBuilder.append(' ');
        }

        String reason = stringBuilder.toString();

        if (reason.length() > 200) {
            sender.sendMessage(ChatColor.RED + "Reason can only be 200 characters long!");
            return false;
        }

        String bannedBy;
        if (sender instanceof Player) {
            bannedBy = String.valueOf(((Player) sender).getUniqueId());
        } else {
            bannedBy = sender.getName();
        }

        long length;

        if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("s")) {
            length = Long.parseLong(args[1].substring(0, args[1].length() - 1));
        } else if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("m")) {
            length = TimeUnit.SECONDS.convert(Long.parseLong(args[1].substring(0, args[1].length() - 1)), TimeUnit.MINUTES);
        } else if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("h")) {
            length = TimeUnit.SECONDS.convert(Long.parseLong(args[1].substring(0, args[1].length() - 1)), TimeUnit.HOURS);
        } else if (args[1].substring(args[1].length() - 1).equalsIgnoreCase("d")) {
            length = TimeUnit.SECONDS.convert(Long.parseLong(args[1].substring(0, args[1].length() - 1)), TimeUnit.DAYS);
        } else if (args[1].equalsIgnoreCase("-1")) {
            length = -1;
        } else {
            syntax(sender);
            return false;
        }

        BanSystem.getInstance().getBanManager().banPlayer(uuid, length, reason, bannedBy);

        if (player != null) {
            player.kickPlayer(BanSystem.getInstance().getBanManager().reason(reason));
        }

        sender.sendMessage(ChatColor.GREEN + "Banned player: " + args[0] + " (UUID: " + uuid + ")");

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (!sender.hasPermission("ban.ban")) {
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
        commandSender.sendMessage(ChatColor.RED + "Usage: /ban <Player> <Duration> <Reason>");
        commandSender.sendMessage(ChatColor.RED + "Duration:");
        commandSender.sendMessage(ChatColor.RED + "Use s for Seconds");
        commandSender.sendMessage(ChatColor.RED + "Use m for Minutes");
        commandSender.sendMessage(ChatColor.RED + "Use h for Hours");
        commandSender.sendMessage(ChatColor.RED + "Use d for Days");
        commandSender.sendMessage(ChatColor.RED + "Use -1 for Permanent");
    }
}
