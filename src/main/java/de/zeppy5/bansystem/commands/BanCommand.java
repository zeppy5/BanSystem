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
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BanCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        new Thread(() -> {
            if (!sender.hasPermission("ban.ban")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
                return;
            }

            if (args.length < 2) {
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

            String bannedBy;
            if (sender instanceof Player) {
                bannedBy = String.valueOf(((Player) sender).getUniqueId());
            } else {
                bannedBy = sender.getName();
            }

            if (args.length >= 3) {

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < args.length - 2; i++) {
                    stringBuilder.append(args[i+2]);
                    stringBuilder.append(' ');
                }

                String reason = stringBuilder.toString();

                if (reason.length() > 200) {
                    sender.sendMessage(ChatColor.RED + "Reason can only be 200 characters long!");
                    return;
                }

                long length;

                try {
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
                        return;
                    }
                } catch (NumberFormatException e) {
                    syntax(sender);
                    return;
                }

                BanSystem.getInstance().getBanManager().banPlayer(uuid, length, reason, bannedBy);

                if (player != null) {
                    Bukkit.getScheduler().runTask(BanSystem.getInstance(), () -> player.kickPlayer(BanSystem.getInstance().getBanManager().reason(reason)));
                }

                sender.sendMessage(ChatColor.GREEN + "Banned player: " + args[0] + " (UUID: " + uuid + ")"
                        + " for " + (args[1].equals("-1") ? "PERMANENT" : args[1]) + ". Reason: " + reason);

            } else if (args.length == 2) {

                int id;

                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    syntax(sender);
                    return;
                }

                String reason = BanSystem.getInstance().getBanReasons().getReason(id);
                String lengthString = BanSystem.getInstance().getBanReasons().getLength(id);

                if (reason == null || lengthString == null) {
                    syntax(sender);
                    return;
                }


                if (reason.length() > 200) {
                    sender.sendMessage(ChatColor.RED + "Reason can only be 200 characters long!");
                    return;
                }

                long length;

                try {
                    if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("s")) {
                        length = Long.parseLong(lengthString.substring(0, lengthString.length() - 1));
                    } else if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("m")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(lengthString.substring(0, lengthString.length() - 1)), TimeUnit.MINUTES);
                    } else if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("h")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(lengthString.substring(0, lengthString.length() - 1)), TimeUnit.HOURS);
                    } else if (lengthString.substring(lengthString.length() - 1).equalsIgnoreCase("d")) {
                        length = TimeUnit.SECONDS.convert(Long.parseLong(lengthString.substring(0, lengthString.length() - 1)), TimeUnit.DAYS);
                    } else if (lengthString.equalsIgnoreCase("-1")) {
                        length = -1;
                    } else {
                        syntax(sender);
                        return;
                    }
                } catch (NumberFormatException e) {
                    syntax(sender);
                    return;
                }

                BanSystem.getInstance().getBanManager().banPlayer(uuid, length, reason, bannedBy);

                if (player != null) {
                    Bukkit.getScheduler().runTask(BanSystem.getInstance(), () -> player.kickPlayer(BanSystem.getInstance().getBanManager().reason(reason)));
                }

                sender.sendMessage(ChatColor.GREEN + "Banned player: " + args[0] + " (UUID: " + uuid + ")"
                        + " for " + (lengthString.equals("-1") ? "PERMANENT" : lengthString) + ". Reason: " + reason);

            } else {
                syntax(sender);
            }

        }).start();

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

        List<Map<?, ?>> reasonList = BanSystem.getInstance().getBanReasons().getList();

        reasonList.forEach(map -> {
            String id = String.valueOf(map.get("id"));
            String reason = String.valueOf(map.get("reason"));
            String length = String.valueOf(map.get("length")).equals("-1") ? "PERMANENT" : String.valueOf(map.get("length"));

            commandSender.sendMessage(ChatColor.RED + "ID: " + ChatColor.DARK_RED + id
                    + ChatColor.RED + " Length: " + ChatColor.DARK_RED + length
                    + ChatColor.RED + " Reason: " + ChatColor.DARK_RED + reason);
        });

        commandSender.sendMessage(ChatColor.RED + "Usage: ");
        commandSender.sendMessage(ChatColor.DARK_RED + "/ban <Player> <Reason-ID>");
        commandSender.sendMessage(ChatColor.DARK_RED + "/ban <Player> <Duration> <Reason>");
        commandSender.sendMessage(ChatColor.RED + "Duration:");
        commandSender.sendMessage(ChatColor.RED + "Use s for Seconds");
        commandSender.sendMessage(ChatColor.RED + "Use m for Minutes");
        commandSender.sendMessage(ChatColor.RED + "Use h for Hours");
        commandSender.sendMessage(ChatColor.RED + "Use d for Days");
        commandSender.sendMessage(ChatColor.RED + "Use -1 for Permanent");
    }
}
