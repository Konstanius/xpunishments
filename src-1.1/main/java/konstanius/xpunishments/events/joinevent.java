package konstanius.xpunishments.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static konstanius.xpunishments.Xpunishments.*;

public class joinevent implements Listener {
    @EventHandler
    public void joinEvent(AsyncPlayerPreLoginEvent joinevent) {
        String player = joinevent.getName().toLowerCase(Locale.ROOT);
        UUID playeruuid = joinevent.getUniqueId();
        Statement statement = null;
        ResultSet result = null;
        try {
             statement = connection.createStatement();
            result = statement.executeQuery("SELECT * from xpunishments_playerdata where uuid = \"" + playeruuid + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (result != null) {
                if(!result.next()) {
                    statement.executeUpdate("INSERT INTO xpunishments_playerdata (lastname,uuid,karma,muteuntil,banneduntil,currentreason) VALUES ('" + player + "','" + playeruuid + "','0','0','0','NONE');");
                }
                else {
                    long unix = (int) (System.currentTimeMillis() / 1000);
                    int banneduntil = result.getInt("banneduntil");
                    if(banneduntil > unix) {
                        Calendar now = Calendar.getInstance();
                        TimeZone timeZone = now.getTimeZone();
                        String currentreason = result.getString("currentreason");
                        dateformatter.setTimeZone(timeZone);
                        String date = dateformatter.format(banneduntil * 1000L);
                        if(banneduntil == 2147483647) {
                            date = "Permanent";
                        }
                        joinevent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Objects.requireNonNull(config.getString("messages.ban.format")).replace("&", "§").replace("%reason%", currentreason).replace("%date%", date));
                        System.out.println(messages_prefix + Objects.requireNonNull(config.getString("messages.ban.tried-to-join")).replace("&","§").replace("%player%",player).replace("%reason%",currentreason).replace("%date%",date));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
