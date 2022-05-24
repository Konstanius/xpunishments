package konstanius.xpunishments.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static konstanius.xpunishments.Xpunishments.*;
import static konstanius.xpunishments.Xpunishments.config;

public class chatevent implements Listener {
    @EventHandler
    public void chatEvent(AsyncChatEvent chatevent) {
        String player = chatevent.getPlayer().getName().toLowerCase(Locale.ROOT);
        UUID playeruuid = chatevent.getPlayer().getUniqueId();
        Statement statement = null;
        ResultSet result = null;
        try {
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT * from xpunishments_playerdata where uuid = \"" + playeruuid + "\";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(!Objects.requireNonNull(result).next()) {
                statement.executeUpdate("INSERT INTO xpunishments_playerdata (lastname,uuid,karma,muteuntil,muteuntil,currentreason) VALUES ('" + player + "','" + playeruuid + "','0','0','0','NONE');");
            }
            else {
                long unix = (int) (System.currentTimeMillis() / 1000);
                int muteuntil = result.getInt("muteuntil");
                if(muteuntil > unix) {
                    Calendar now = Calendar.getInstance();
                    TimeZone timeZone = now.getTimeZone();
                    String currentreason = result.getString("currentreason");
                    dateformatter.setTimeZone(timeZone);
                    String date = dateformatter.format(muteuntil * 1000L);
                    if(muteuntil == 2147483647) {
                        date = "Permanent";
                    }
                    chatevent.setCancelled(true);
                    chatevent.getPlayer().sendMessage(Objects.requireNonNull(config.getString("messages.mute.format")).replace("&","§").replace("%reason%",currentreason).replace("%date%",date));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
