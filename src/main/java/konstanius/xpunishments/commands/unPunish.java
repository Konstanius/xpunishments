package konstanius.xpunishments.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import static konstanius.xpunishments.Xpunishments.*;
import static konstanius.xpunishments.Xpunishments.config;

public class unPunish implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.unpunish.not-enough-arguments")).replace("&","§"));
            return true;
        }
        if(args.length > 1) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.unpunish.too-many-arguments")).replace("&","§"));
            return true;
        }
        if(!(args[0].matches("[1234567890]+"))) {
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.unpunish.not-int")).replace("&","§"));
            return true;
        }
        int id = Integer.parseInt(args[0]);
        Statement statement = null;
        ResultSet result = null;
        try {
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT id,uuid from xpunishments_infractions where id = \"" + id + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(!(Objects.requireNonNull(result).next())) {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.unpunish.id-not-found")).replace("&","§"));
                return true;
            }
            String uuid = result.getString("uuid");
            ResultSet result2 = statement.executeQuery("SELECT lastname FROM xpunishments_playerdata WHERE uuid = \"" + uuid + "\";");
            result2.next();
            String player = result2.getString("lastname");
            statement.executeUpdate("DELETE FROM xpunishments_infractions WHERE id = \"" + id + "\" ;");
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.unpunish.success")).replace("&","§").replace("%player%", player).replace("%id%", String.valueOf(id)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
