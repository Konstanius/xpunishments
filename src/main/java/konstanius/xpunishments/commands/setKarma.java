package konstanius.xpunishments.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Objects;

import static konstanius.xpunishments.Xpunishments.*;
import static konstanius.xpunishments.Xpunishments.config;

public class setKarma implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 2) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.setkarma.not-enough-arguments")).replace("&","§"));
            return true;
        }
        if(args.length > 2) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.setkarma.too-many-arguments")).replace("&","§"));
            return true;
        }
        if(!(args[1].matches("[1234567890]+"))) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.setkarma.not-int")).replace("&","§"));
            return true;
        }
        Statement statement = null;
        ResultSet result = null;
        String player = args[0].toLowerCase(Locale.ROOT);
        try {
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT id from xpunishments_playerdata where lastname = \"" + player + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(!(Objects.requireNonNull(result).next())) {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.player-has-never-joined")).replace("&","§"));
                return true;
            }
            int id = result.getInt("id");
            int karma = Integer.parseInt(args[1]);
            statement.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"" + karma + "\" WHERE id = \"" + id + "\";");
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.setkarma.success")).replace("&","§").replace("%karma%", String.valueOf(karma)).replace("%player%", args[0]));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
