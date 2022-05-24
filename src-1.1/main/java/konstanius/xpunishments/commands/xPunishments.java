package konstanius.xpunishments.commands;

import konstanius.xpunishments.Xpunishments;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static konstanius.xpunishments.Xpunishments.*;

public class xPunishments implements CommandExecutor {
    private final Xpunishments myPlugin;

    public xPunishments(Xpunishments plugin) {
        this.myPlugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length != 1) {
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.generic.help")).replace("&","§"));
        }
        else if (args[0].equals("reload")){
            if(!(sender instanceof Player)) {
                reload(sender);
            }
            else {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.must-be-console")).replace("&","§"));
            }
        }
        else if (args[0].equals("resetdb")){
            if(!(sender instanceof Player)) {
                resetdb(sender);
            }
            else {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.must-be-console")).replace("&","§"));
            }
        }
        else if (args[0].equals("list")){
            if(sender instanceof Player) {
                listpunishments(sender);
            }
            else {
                sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.must-be-player")).replace("&","§"));
            }
        }
        else {
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.generic.help")).replace("&","§"));
        }
        return true;
    }

    public void reload(CommandSender sender) {
        try {
            connection.close();
            scheduler.cancelTask(timertask);
            myPlugin.reloadConfig();
            config = myPlugin.getConfig();
            messages_prefix = Objects.requireNonNull(config.getString("prefix")).replace("&", "§");
            dateformatter = new SimpleDateFormat(Objects.requireNonNull(config.getString("date-format")));
            myPlugin.karmaDecrease();
            host = config.getString("MySQL.host");
            port = config.getString("MySQL.port");
            database = config.getString("MySQL.database");
            username = config.getString("MySQL.user");
            password = config.getString("MySQL.password");
            try {
                myPlugin.openConnection();
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Error when connecting to MySQL of XPunishments");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(myPlugin);
            }
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS xpunishments_infractions (id int NOT NULL AUTO_INCREMENT,uuid TEXT,reason TEXT,karma INT,time TEXT,mod_uuid TEXT,type TEXT,PRIMARY KEY(id))  ENGINE=INNODB;");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS xpunishments_playerdata (id int NOT NULL AUTO_INCREMENT,lastname TEXT,uuid TEXT,karma INT,banneduntil INT,muteuntil int,currentreason TEXT,PRIMARY KEY(id))  ENGINE=INNODB;");
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.plugin-reloaded")).replace("&","§"));
        } catch (SQLException e) {
            System.out.println("Error when connecting to MySQL of XPunishments");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(myPlugin);
        }
    }

    public void resetdb(CommandSender sender) {
        try {
            Statement statement = connection.createStatement();
            try {
                statement.executeUpdate("DROP TABLE xpunishments_infractions;");
            } catch (Exception ignored) {
            }
            try {
                statement.executeUpdate("DROP TABLE xpunishments_playerdata;");
            } catch (Exception ignored) {
            }
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS xpunishments_infractions (id int NOT NULL AUTO_INCREMENT,uuid TEXT,reason TEXT,karma INT,time TEXT,mod_uuid TEXT,type TEXT,PRIMARY KEY(id))  ENGINE=INNODB;");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS xpunishments_playerdata (id int NOT NULL AUTO_INCREMENT,lastname TEXT,uuid TEXT,karma INT,banneduntil INT,muteuntil int,currentreason TEXT,PRIMARY KEY(id))  ENGINE=INNODB;");
            String player;
            UUID playeruuid;
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                player = p.getName().toLowerCase(Locale.ROOT);
                playeruuid = p.getUniqueId();
                statement.executeUpdate("INSERT INTO xpunishments_playerdata (lastname,uuid,karma,muteuntil,banneduntil,currentreason) VALUES ('" + player + "','" + playeruuid + "','0','0','0','NONE');");
            }
            sender.sendMessage(messages_prefix + Objects.requireNonNull(config.getString("messages.generic.database-reset")).replace("&","§"));
        } catch (SQLException e) {
            System.out.println("Error when connecting to MySQL of XPunishments");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(myPlugin);
        }
    }

    public void listpunishments(CommandSender sender) {
        sender.sendMessage(Objects.requireNonNull(config.getString("messages.list.header")).replace("&","§"));
        for(String string: Objects.requireNonNull(config.getConfigurationSection("punishments")).getKeys(false)) {
            String description = config.getString("punishments." + string + ".description");
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.list.content")).replace("&","§").replace("%reason%", string).replace("%description%", Objects.requireNonNull(description)));
        }
        sender.sendMessage(Objects.requireNonNull(config.getString("messages.list.footer")).replace("&","§"));
    }
}


