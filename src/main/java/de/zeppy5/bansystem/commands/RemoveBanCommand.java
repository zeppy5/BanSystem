package de.zeppy5.bansystem.commands;

import de.zeppy5.bansystem.BanSystem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class RemoveBanCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        new Thread(() -> {
            if (!sender.hasPermission("ban.remove")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
                return;
            }

            if (args.length != 1) {
                syntax(sender);
                return;
            }

            BanSystem.getInstance().getBanManager().removeBan(args[0]);

            sender.sendMessage(ChatColor.GREEN + "Tried to remove ban with ID: " + ChatColor.BLUE + args[0]);
        }).start();

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (!sender.hasPermission("ban.unban")) {
            return null;
        }

        return list;
    }

    private void syntax(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.RED + "Usage: /removeban <ban-id>");
    }
}
