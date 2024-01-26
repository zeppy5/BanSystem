package de.zeppy5.bansystem.commands;

import de.zeppy5.bansystem.BanSystem;
import de.zeppy5.bansystem.util.MojangAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        new Thread(() -> {
            if (!sender.hasPermission("ban.unban")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
                return;
            }

            if (args.length != 1) {
                syntax(sender);
                return;
            }

            String uuid = null;

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

            BanSystem.getInstance().getBanManager().unbanPlayer(uuid);

            sender.sendMessage(ChatColor.GREEN + "Unbanned player: " + ChatColor.BLUE + args[0] + " (UUID: " + uuid + ")");
        }).start();

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (!sender.hasPermission("ban.unban")) {
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
        commandSender.sendMessage(ChatColor.RED + "Usage: /unban <Player>");
    }
}
