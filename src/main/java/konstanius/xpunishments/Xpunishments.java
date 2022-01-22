package konstanius.xpunishments;

import konstanius.xpunishments.commands.*;
import konstanius.xpunishments.events.chatevent;
import konstanius.xpunishments.events.joinevent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;

public final class Xpunishments extends JavaPlugin {
    public static String host;
    public static String port;
    public static String database;
    public static String username;
    public static String password;
    public static Connection connection;
    public static String messages_prefix = "§3§lXPunishments >> §e";
    public static FileConfiguration config;
    public static long timer;
    public static BukkitScheduler scheduler = Bukkit.getScheduler();
    public static int timertask;
    public static SimpleDateFormat dateformatter;
    @Override
    public void onEnable() {
        System.out.println("Enabling XPunishments by Konstanius");
        this.saveDefaultConfig();
        config = this.getConfig();
        messages_prefix = Objects.requireNonNull(this.getConfig().getString("prefix")).replace("&", "§");
        host = config.getString("MySQL.host");
        port = config.getString("MySQL.port");
        database = config.getString("MySQL.database");
        username = config.getString("MySQL.user");
        password = config.getString("MySQL.password");
        dateformatter = new SimpleDateFormat(Objects.requireNonNull(config.getString("date-format")));
        try {
            this.openConnection();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error when connecting to MySQL of XPunishments");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Objects.requireNonNull(this.getCommand("punish")).setExecutor(new punish());
        Objects.requireNonNull(this.getCommand("infractions")).setExecutor(new infractions());
        Objects.requireNonNull(this.getCommand("setkarma")).setExecutor(new setKarma());
        Objects.requireNonNull(this.getCommand("clearinfractions")).setExecutor(new clearInfractions());
        Objects.requireNonNull(this.getCommand("xpunishments")).setExecutor(new xPunishments(this));
        Objects.requireNonNull(this.getCommand("unpunish")).setExecutor(new unPunish());
        Objects.requireNonNull(this.getCommand("unban")).setExecutor(new unBan());
        Objects.requireNonNull(this.getCommand("unmute")).setExecutor(new unMute());

        getServer().getPluginManager().registerEvents(new joinevent(), this);
        getServer().getPluginManager().registerEvents(new chatevent(), this);

        karmadecrease();

        try {
            Statement createtable = connection.createStatement();
            createtable.executeUpdate("CREATE TABLE IF NOT EXISTS xpunishments_infractions (id int NOT NULL AUTO_INCREMENT,uuid TEXT,reason TEXT,karma INT,time TEXT,mod_uuid TEXT,type TEXT,PRIMARY KEY(id))  ENGINE=INNODB;");
            createtable.executeUpdate("CREATE TABLE IF NOT EXISTS xpunishments_playerdata (id int NOT NULL AUTO_INCREMENT,lastname TEXT,uuid TEXT,karma INT,banneduntil INT,muteuntil int,currentreason TEXT,PRIMARY KEY(id))  ENGINE=INNODB;");
            System.out.println("Startup complete - XPunishments by Konstanius");
        } catch (SQLException e) {
            System.out.println("Error when connecting to MySQL of XPunishments");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        }
    }

    public void karmadecrease() {
        timer = config.getLong("seconds-per-karma-reduction");
        scheduler.runTaskTimer(this, task -> {
            if(timer == -1L) {
                task.cancel();
            }
            timertask = task.getTaskId();
            try {
                Statement statement = connection.createStatement();
                Statement statement2 = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT id,karma FROM xpunishments_playerdata;");
                while (resultSet.next()) {
                    int karma = resultSet.getInt("karma") - 1;
                    if(karma < 0) {
                        karma = 0;
                    }
                    int id = resultSet.getInt("id");
                    statement2.executeUpdate("UPDATE xpunishments_playerdata SET karma = \"" + karma + "\" WHERE id = \"" + id + "\";");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, timer * 20L, timer * 20L);
    }
}
