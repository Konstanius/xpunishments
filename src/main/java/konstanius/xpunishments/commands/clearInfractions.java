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

public class clearInfractions implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.clearinfractions.not-enough-arguments")).replace("&","§"));
            return true;
        }
        if(args.length > 1) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.clearinfractions.too-many-arguments")).replace("&","§"));
            return true;
        }
        Statement statement = null;
        ResultSet result = null;
        String player = args[0].toLowerCase(Locale.ROOT);
        try {
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT id,uuid from xpunishments_playerdata where lastname = \"" + player + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(!(Objects.requireNonNull(result).next())) {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.player-has-never-joined")).replace("&","§"));
                return true;
            }
            int id = result.getInt("id");
            String uuid = result.getString("uuid");
            statement.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"0\", banneduntil = \"0\", muteuntil = \"0\", currentreason = \"NONE\" WHERE id = \"" + id + "\";");
            statement.executeUpdate("DELETE FROM xpunishments_infractions WHERE uuid = \"" + uuid + "\" ;");
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.clearinfractions.success")).replace("&","§").replace("%player%", args[0]));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
