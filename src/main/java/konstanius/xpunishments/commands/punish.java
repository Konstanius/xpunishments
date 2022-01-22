package konstanius.xpunishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;

import static konstanius.xpunishments.Xpunishments.*;

public class punish implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.must-be-player")).replace("&","§"));
            return true;
        }
        Player mod = (Player) sender;
        UUID moduuid = mod.getUniqueId();
        if(args.length < 2) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.punish.not-enough-arguments")).replace("&","§"));
            return true;
        }
        if(args.length > 2) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.punish.too-many-arguments")).replace("&","§"));
            return true;
        }
        Statement statement = null;
        ResultSet result = null;
        String player = args[0].toLowerCase(Locale.ROOT);
        try {
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT * from xpunishments_playerdata where lastname = \"" + player + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(!(Objects.requireNonNull(result).next())) {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.player-has-never-joined")).replace("&","§"));
                return true;
            }
            else {
                UUID uuid = UUID.fromString(result.getString("uuid"));
                int karma = result.getInt("karma");
                String reason = args[1];
                if(config.getString("punishments." + reason) == null) {
                    sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.reason-does-not-exist")).replace("&","§"));
                    return true;
                }
                if(!(mod.hasPermission("xpunishments.override"))) {
                    if(((Player) Bukkit.getOfflinePlayer(uuid)).hasPermission("xpunishments.exempt")) {
                        sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.player-is-exempt")));
                        return true;
                    }
                }
                else {
                    String reasonstring = config.getString("punishments." + reason + ".reason");
                    int karmaused = 0;
                    for(int i=0; i < karma + 1; i++) {
                        String current = config.getString("punishments." + reason + "." + i);
                        if(current != null) {
                            karmaused = i;
                        }
                    }
                    Calendar now = Calendar.getInstance();
                    TimeZone timeZone = now.getTimeZone();
                    dateformatter.setTimeZone(timeZone);
                    String punishment = config.getString("punishments." + reason + "." + karmaused);
                    String[] split = Objects.requireNonNull(punishment).split(":");
                    switch (split[0]) {
                        case "NONE": {
                            int newkarma = Integer.parseInt(split[2]) + karma;
                            int unix = (int) (System.currentTimeMillis() / 1000);
                            String date = dateformatter.format(unix * 1000L);
                            statement.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"" + newkarma + "\" WHERE uuid = \"" + uuid + "\";");
                            statement.executeUpdate("INSERT INTO xpunishments_infractions (uuid,reason,karma,time,mod_uuid,type) VALUES ('" + uuid + "','" + reasonstring + "','" + newkarma + "','" + date + "','" + moduuid + "','NONE');");
                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                if (p.hasPermission("xpunishments.staff")) {
                                    p.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.none.punishment-announce")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]));
                                }
                            }
                            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.none.punishment")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]));
                            break;
                        }
                        case "WARN": {
                            int newkarma = Integer.parseInt(split[2]) + karma;
                            int unix = (int) (System.currentTimeMillis() / 1000);
                            String date = dateformatter.format(unix * 1000L);
                            statement.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"" + newkarma + "\" WHERE uuid = \"" + uuid + "\";");
                            statement.executeUpdate("INSERT INTO xpunishments_infractions (uuid,reason,karma,time,mod_uuid,type) VALUES ('" + uuid + "','" + reasonstring + "','" + newkarma + "','" + date + "','" + moduuid + "','WARN');");
                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                if (p.hasPermission("xpunishments.staff")) {
                                    p.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.warn.punishment-announce")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]));
                                }
                            }
                            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.warn.punishment")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]));
                            if (Bukkit.getServer().getPlayer(uuid) != null) {
                                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.warn.format")).replace("&", "§").replace("%reason%", reasonstring).replace("%mod%", mod.getName()).replace("%player%", args[0]));
                            }
                            break;
                        }
                        case "MUTE": {
                            int prevmute = result.getInt("muteuntil");
                            int newkarma = Integer.parseInt(split[2]) + karma;
                            int unix = (int) (System.currentTimeMillis() / 1000);
                            int muteuntil = Integer.parseInt(split[1]) + unix;
                            String date = dateformatter.format(unix * 1000L);
                            String dateuntil = dateformatter.format(muteuntil * 1000L);
                            if (split[1].equals("-1")) {
                                muteuntil = 2147483647;
                                dateuntil = "Permanent";
                            } else if (prevmute > muteuntil) {
                                muteuntil = prevmute;
                            }
                            statement.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"" + newkarma + "\", muteuntil = \"" + muteuntil + "\", currentreason = \"" + reasonstring + "\" WHERE uuid = \"" + uuid + "\";");
                            statement.executeUpdate("INSERT INTO xpunishments_infractions (uuid,reason,karma,time,mod_uuid,type) VALUES ('" + uuid + "','" + reasonstring + "','" + newkarma + "','" + date + "','" + moduuid + "','MUTE');");
                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                if (p.hasPermission("xpunishments.staff")) {
                                    p.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.mute.punishment-announce")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]).replace("%date%", dateuntil));
                                }
                            }
                            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.mute.punishment")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]).replace("%date%", dateuntil));
                            if (Bukkit.getServer().getPlayer(uuid) != null) {
                                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.mute.format")).replace("&", "§").replace("%reason%", reasonstring).replace("%mod%", mod.getName()).replace("%player%", args[0]).replace("%date%", dateuntil));
                            }
                            break;
                        }
                        case "BAN": {
                            int prevban = result.getInt("banneduntil");
                            int newkarma = Integer.parseInt(split[2]) + karma;
                            int unix = (int) (System.currentTimeMillis() / 1000);
                            int banuntil = Integer.parseInt(split[1]) + unix;
                            String date = dateformatter.format(unix * 1000L);
                            String dateuntil = dateformatter.format(banuntil * 1000L);
                            if (split[1].equals("-1")) {
                                banuntil = 2147483647;
                                dateuntil = "Permanent";
                            } else if (prevban > banuntil) {
                                banuntil = prevban;
                            }
                            statement.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"" + newkarma + "\", banneduntil = \"" + banuntil + "\", currentreason = \"" + reasonstring + "\" WHERE uuid = \"" + uuid + "\";");
                            statement.executeUpdate("INSERT INTO xpunishments_infractions (uuid,reason,karma,time,mod_uuid,type) VALUES ('" + uuid + "','" + reasonstring + "','" + newkarma + "','" + date + "','" + moduuid + "','BAN');");
                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                if (p.hasPermission("xpunishments.staff")) {
                                    p.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.ban.punishment-announce")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]).replace("%date%", dateuntil));
                                }
                            }
                            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.ban.punishment")).replace("&", "§").replace("%reason%", Objects.requireNonNull(reasonstring)).replace("%mod%", mod.getName()).replace("%player%", args[0]).replace("%date%", dateuntil));
                            if (Bukkit.getServer().getPlayer(uuid) != null) {
                                Objects.requireNonNull(Bukkit.getServer().getPlayer(uuid)).kickPlayer(Objects.requireNonNull(config.getString("messages.ban.format")).replace("&", "§").replace("%reason%", reasonstring).replace("%date%", dateuntil));
                            }
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
