package konstanius.xpunishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static konstanius.xpunishments.Xpunishments.*;

public class infractions implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.infractions.not-enough-arguments")).replace("&","§"));
            return true;
        }
        if(args.length > 1) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.infractions.too-many-arguments")).replace("&","§"));
            return true;
        }
        Statement statement = null;
        ResultSet result = null;
        ResultSet result2;
        String player = args[0].toLowerCase(Locale.ROOT);
        try {
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT * from xpunishments_playerdata where lastname = \"" + player + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (Objects.requireNonNull(result).next()) {
                UUID uuid = UUID.fromString(result.getString("uuid"));
                int karma = result.getInt("karma");
                int banneduntil = result.getInt("banneduntil");
                int muteuntil = result.getInt("muteuntil");
                String currentreason = result.getString("currentreason");
                long unix = (int) (System.currentTimeMillis() / 1000);
                Calendar now = Calendar.getInstance();
                TimeZone timeZone = now.getTimeZone();
                dateformatter.setTimeZone(timeZone);
                if(banneduntil > unix) {
                    String date = dateformatter.format(banneduntil * 1000L);
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.header-banned")).replace("&","§").replace("%reason%", currentreason).replace("%date%",date).replace("%player%",player).replace("%karma%",String.valueOf(karma)));
                }
                else if (muteuntil > unix){
                    String date = dateformatter.format(muteuntil * 1000L);
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.header-muted")).replace("&","§").replace("%reason%", currentreason).replace("%date%",date).replace("%player%",player).replace("%karma%",String.valueOf(karma)));
                }
                else {
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.header-free")).replace("&","§").replace("%player%",player).replace("%karma%",String.valueOf(karma)));
                }
                result2 = statement.executeQuery("SELECT * from xpunishments_infractions where uuid = \"" + uuid + "\";");
                while (result2.next()) {
                    int id = result2.getInt("id");
                    String reason = result2.getString("reason");
                    UUID mod_uuid = UUID.fromString(result2.getString("mod_uuid"));
                    String mod;
                    try {
                        mod = Objects.requireNonNull(Bukkit.getPlayer(mod_uuid)).getName();
                    }
                    catch (Exception e) {
                        mod = Bukkit.getOfflinePlayer(mod_uuid).getName();
                    }
                    String time = result2.getString("time");
                    String type = result2.getString("type");
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.content")).replace("&","§").replace("%id%", String.valueOf(id)).replace("%reason%",reason).replace("%mod%", Objects.requireNonNull(mod)).replace("%date%", time).replace("%type%", type));
                }
                if(banneduntil > unix) {
                    String date = dateformatter.format(banneduntil * 1000L);
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.footer-banned")).replace("&","§").replace("%reason%", currentreason).replace("%date%",date).replace("%player%",player).replace("%karma%",String.valueOf(karma)));
                }
                else if (muteuntil > unix){
                    String date = dateformatter.format(muteuntil * 1000L);
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.footer-muted")).replace("&","§").replace("%reason%", currentreason).replace("%date%",date).replace("%player%",player).replace("%karma%",String.valueOf(karma)));
                }
                else {
                    sender.sendMessage(Objects.requireNonNull(config.getString("messages.infractions.footer-free")).replace("&","§").replace("%player%",player).replace("%karma%",String.valueOf(karma)));
                }
            }
            else {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.player-has-never-joined")).replace("&","§"));
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
